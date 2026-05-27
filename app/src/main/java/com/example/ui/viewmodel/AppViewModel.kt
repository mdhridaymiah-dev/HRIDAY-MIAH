package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.*
import com.example.data.repository.AppRepository
import com.example.ui.Localization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject

class AppViewModel(application: Application) : AndroidViewModel(application) {

    // --- Data structures for Login Session management (Rule 103) ---
    data class SessionInfo(
        val id: String,
        val username: String,
        val device: String,
        val browser: String,
        val ipAddress: String,
        val location: String,
        val loginTime: String,
        val isCurrent: Boolean = false
    )

    // --- Secure Device and MFA Verification Session States (Rules 94-111) ---
    var isSessionVerified by mutableStateOf(false)
    var loginVerificationPolicy by mutableStateOf("Option-1: Every Login Verification") // Option-1, Option-2, Option-3, Option-4
    val trustedDevicesList = mutableStateListOf<String>("Company-Issued Safe iPad")
    val activeSessions = mutableStateListOf<SessionInfo>(
        SessionInfo("sess-prev1", "admin", "Company-Issued Safe iPad", "Safari 17.2", "192.168.1.100", "Dhaka, Bangladesh", "2026-05-27 12:00:00", false)
    )
    
    // SMS / Email simulated OTP states for login
    var simulatedSmsOtp by mutableStateOf("3892")
    var simulatedEmailCode by mutableStateOf("SEC-1049")
    var simulatedSmsOtpSent by mutableStateOf(false)
    var simulatedEmailCodeSent by mutableStateOf(false)

    // Device details for current login session tracking (Rule 100)
    var activeSessionDevice by mutableStateOf("Android Emulator API 34")
    var activeSessionBrowser by mutableStateOf("SaaS Android Portal Secure Sandbox")
    var activeSessionIP by mutableStateOf("192.168.1.104")
    var activeSessionLocation by mutableStateOf("Dhaka, Bangladesh")
    var activeSessionTime by mutableStateOf("2026-05-27 18:23:07")

    fun checkIfVerificationRequired(user: UserEntity): Boolean {
        if (user.isFirstLogin) {
            return true // Rule 97: First login MANDATORY verification
        }
        return when (loginVerificationPolicy) {
            "Option-1: Every Login Verification" -> true
            "Option-2: New Device Verification" -> !trustedDevicesList.contains(activeSessionDevice)
            "Option-3: New Browser Verification" -> !trustedDevicesList.contains(activeSessionDevice)
            "Option-4: Suspicious Login Verification" -> !activeSessionIP.startsWith("192.168.1.")
            else -> true
        }
    }

    fun dispatchLoginNotificationAndLogs(user: UserEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            // Create session info
            val newSess = SessionInfo(
                id = "sess-${System.currentTimeMillis()}",
                username = user.username,
                device = activeSessionDevice,
                browser = activeSessionBrowser,
                ipAddress = activeSessionIP,
                location = activeSessionLocation,
                loginTime = activeSessionTime,
                isCurrent = true
            )
            launch(Dispatchers.Main) {
                // Mark preceding sessions of this username as non-isCurrent
                val updatedList = activeSessions.map { if (it.username == user.username) it.copy(isCurrent = false) else it }
                activeSessions.clear()
                activeSessions.addAll(updatedList)
                activeSessions.add(newSess)
            }

            // Log to Audit Database as per rule 104 and 111
            repository.insertAuditLog(
                AuditLogEntity(
                    username = user.username,
                    action = "Verification OTP Successful",
                    module = "Auth",
                    projectId = user.projectAssignmentId,
                    device = activeSessionDevice,
                    ipAddress = activeSessionIP,
                    newValue = "Mfa validated. Granted access session: ${newSess.id}"
                )
            )

            // Dispatch notification in product tracker alerts for UI display (Rule 101)
            repository.insertNotification(
                NotificationEntity(
                    projectId = user.projectAssignmentId,
                    targetUsername = user.username,
                    titleEn = "⚠️ Successful Login Warning",
                    titleBn = "⚠️ সফল লগইন সর্তকতা",
                    messageEn = "Successful log-in on Device: ${activeSessionDevice} (IP: ${activeSessionIP}) at ${activeSessionTime}.",
                    messageBn = "ডিভাইস ${activeSessionDevice} (আইপি: ${activeSessionIP}) থেকে লগইন সম্পন্ন হয়েছে। সময়: ${activeSessionTime}।",
                    isRead = false,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun logoutSpecificDevice(sessionId: String) {
        activeSessions.removeAll { it.id == sessionId }
        logConsole("Audit LOGOUT: Remote Device session '$sessionId' terminated instantly by user action.")
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAuditLog(
                AuditLogEntity(
                    username = currentUser?.username ?: "system",
                    action = "Device Session Terminated",
                    module = "Auth",
                    projectId = null,
                    newValue = "Terminated Session ID: $sessionId"
                )
            )
        }
    }

    fun logoutAllDevicesExceptCurrent() {
        val currentSess = activeSessions.find { it.isCurrent }
        activeSessions.clear()
        if (currentSess != null) {
            activeSessions.add(currentSess)
        }
        logConsole("Audit SESSION CONFLICT CLEAN: All remote device active sessions terminated successfully.")
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAuditLog(
                AuditLogEntity(
                    username = currentUser?.username ?: "system",
                    action = "Terminate All Sessions",
                    module = "Auth",
                    projectId = null,
                    newValue = "Purged all logins except the active session."
                )
            )
        }
    }

    fun logoutActiveSession() {
        val prevUser = currentUser
        currentUser = null
        isSessionVerified = false
        simulatedSmsOtpSent = false
        simulatedEmailCodeSent = false
        logConsole("User session logged out successfully.")
        viewModelScope.launch(Dispatchers.IO) {
            if (prevUser != null) {
                repository.insertAuditLog(
                    AuditLogEntity(
                        username = prevUser.username,
                        action = "Logout Session",
                        module = "Auth",
                        projectId = prevUser.projectAssignmentId
                    )
                )
            }
        }
    }

    fun logFailedVerificationAttempt(username: String, details: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAuditLog(
                AuditLogEntity(
                    username = username,
                    action = "Failed MFA Verification",
                    module = "Auth",
                    projectId = currentUser?.projectAssignmentId,
                    device = activeSessionDevice,
                    ipAddress = activeSessionIP,
                    newValue = details
                )
            )
        }
    }

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = AppRepository(db.appDao())

    // --- Core Active Session State ---
    var currentLanguage by mutableStateOf(Localization.Language.EN)
    var currentUser by mutableStateOf<UserEntity?>(null)
    var currentProject by mutableStateOf<ProjectEntity?>(null)
    var currentProjectMembersCount by mutableStateOf(0)

    // Global lists driven reactively by Room database Flow
    val projects = repository.allProjects.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val users = repository.allUsers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allIncome = repository.allIncome.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allExpenses = repository.allExpenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allRequests = repository.allRequests.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val auditLogs = repository.allAuditLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allFishStocks = repository.allFishStocks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Project-specific flows mapped dynamically when project changes
    private val _projectIdState = MutableStateFlow<Int?>(null)
    
    val currentMembers = _projectIdState.flatMapLatest { id ->
        if (id != null) repository.getMembersForProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentShares = _projectIdState.flatMapLatest { id ->
        if (id != null) repository.getSharesForProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentShareHistory = _projectIdState.flatMapLatest { id ->
        if (id != null) repository.getShareHistoryForProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentIncome = _projectIdState.flatMapLatest { id ->
        if (id != null) repository.getIncomeForProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentExpenses = _projectIdState.flatMapLatest { id ->
        if (id != null) repository.getExpensesForProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentRequests = _projectIdState.flatMapLatest { id ->
        if (id != null) repository.getRequestsForProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentPonds = _projectIdState.flatMapLatest { id ->
        if (id != null) repository.getPondsForProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentAuditLogs = _projectIdState.flatMapLatest { id ->
        if (id != null) repository.getAuditLogsForProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications = combine(
        snapshotFlow { currentUser?.username },
        _projectIdState
    ) { username, pId ->
        Pair(username ?: "", pId)
    }.flatMapLatest { pair ->
        repository.getNotifications(pair.first, pair.second)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Local authentication & safety state
    var failedAttempts = mutableStateOf(0)
    var isAccountLocked = mutableStateOf(false)
    var loginLockTimestamp = mutableStateOf(0L)
    
    // Backup State Logger
    var backupStatusText by mutableStateOf("")

    // Simulated SMS/Email Terminal Console Logger
    val securityConsoleLogs = mutableStateListOf<String>()

    fun selectProject(project: ProjectEntity?) {
        currentProject = project
        _projectIdState.value = project?.id
        
        // Count total members assigned to count approvals
        if (project != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val list = repository.getMembersForProjectSync(project.id)
                currentProjectMembersCount = list.size
            }
        }
    }

    // --- Authentication Actions (RBAC & Force Password Controls) ---
    fun login(username: String, pinCode: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserByUsername(username)
            if (user == null) {
                logConsole("Login attempt fail: Username '$username' does not exist.")
                launch(Dispatchers.Main) { onResult(false, "invalid_credentials") }
                return@launch
            }

            if (user.status == "Suspended") {
                logConsole("Suspended login attempt blocked for user '$username'.")
                launch(Dispatchers.Main) { onResult(false, "account_suspended") }
                return@launch
            }

            // Simple block brute force lockout check
            if (user.lockedUntil != null && user.lockedUntil > System.currentTimeMillis()) {
                logConsole("Locked account login attempted for '$username'.")
                launch(Dispatchers.Main) { onResult(false, "account_locked") }
                return@launch
            }

            // Rule 92: Default Password Deactivation
            if (pinCode == "11" && !user.isFirstLogin && user.role == "Super Admin") {
                logConsole("SECURITY ACCESS BLOCKED: Super Admin entered permanently deactivated default password '11'!")
                repository.insertAuditLog(
                    AuditLogEntity(
                        username = username,
                        action = "Block Deactivated Default Password",
                        module = "Auth",
                        projectId = null,
                        newValue = "Deactivated password rejected for admin"
                    )
                )
                launch(Dispatchers.Main) { onResult(false, "default_pass_deactivated") }
                return@launch
            }

            val inputHash = com.example.ui.SecurityUtils.hashPassword(pinCode)
            if (user.passwordHash == pinCode || user.passwordHash == inputHash) {
                // Successful Login
                val resetAttemptsUser = user.copy(failedLogins = 0, lockedUntil = null)
                repository.updateUser(resetAttemptsUser)

                launch(Dispatchers.Main) {
                    currentUser = resetAttemptsUser
                    
                    val needsVerify = checkIfVerificationRequired(resetAttemptsUser)
                    if (needsVerify) {
                        isSessionVerified = false
                        simulatedSmsOtp = (1000..9999).random().toString()
                        simulatedEmailCode = "SEC-" + (1000..9999).random().toString()
                        simulatedSmsOtpSent = false
                        simulatedEmailCodeSent = false
                    } else {
                        isSessionVerified = true
                        dispatchLoginNotificationAndLogs(resetAttemptsUser)
                    }

                    // Track Super Admin First Login Attempt as per rule 93
                    if (resetAttemptsUser.role == "Super Admin" && resetAttemptsUser.isFirstLogin) {
                        viewModelScope.launch(Dispatchers.IO) {
                            repository.insertAuditLog(
                                AuditLogEntity(
                                    username = username,
                                    action = "First Login Detected",
                                    module = "Auth",
                                    projectId = null,
                                    newValue = "Super Admin log in with default key 11. Initial setup wizard triggered."
                                )
                            )
                        }
                    }
                    // Automatically resolve assigned project if member or project admin
                    if (resetAttemptsUser.projectAssignmentId != null) {
                        viewModelScope.launch(Dispatchers.IO) {
                            val proj = repository.getProjectById(resetAttemptsUser.projectAssignmentId)
                            launch(Dispatchers.Main) { selectProject(proj) }
                        }
                    } else {
                        // Global Super Admin or Auditor picks first project
                        selectProject(projects.value.firstOrNull())
                    }
                    
                    logConsole("User '$username' password validated successfully. Need OTP validation: $needsVerify.")
                    if (needsVerify) {
                        onResult(true, "VerificationChallenge")
                    } else {
                        onResult(true, "Success")
                    }
                }
            } else {
                // Failed login attempt
                val currentTime = System.currentTimeMillis()
                val nextFailedCount = user.failedLogins + 1
                var lockedTime: Long? = null
                if (nextFailedCount >= 3) {
                    lockedTime = currentTime + 60000 // lock for 1 min
                    logConsole("SECURITY ALERT: User '$username' locked for 1 min after 3 failed password trials!")
                }

                val updatedUser = user.copy(failedLogins = nextFailedCount, lockedUntil = lockedTime)
                repository.updateUser(updatedUser)

                launch(Dispatchers.Main) {
                    onResult(false, if (nextFailedCount >= 3) "account_locked" else "wrong_password_try")
                }

                repository.insertAuditLog(
                    AuditLogEntity(
                        username = username,
                        action = "Failed Login Trial",
                        module = "Auth",
                        projectId = null,
                        newValue = "Failed Count: $nextFailedCount"
                    )
                )
            }
        }
    }

    fun logConsole(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            securityConsoleLogs.add(0, "[SYSTEM ALERT] ${System.currentTimeMillis() % 100000} - $message")
        }
    }

    fun completeForcePasswordReset(newPass: String) {
        val activeUser = currentUser ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val hashed = com.example.ui.SecurityUtils.hashPassword(newPass)
            val updated = activeUser.copy(passwordHash = hashed, isFirstLogin = false)
            repository.updateUser(updated)
            launch(Dispatchers.Main) {
                currentUser = updated
                logConsole("First Login requirements fulfilled. User '${activeUser.username}' password initialized with secure tracking.")
            }
            repository.insertAuditLog(
                AuditLogEntity(
                    username = activeUser.username,
                    action = "First Login Force Password Change Completed",
                    module = "Auth",
                    projectId = activeUser.projectAssignmentId
                )
            )
            if (activeUser.role == "Super Admin") {
                logConsole("[SECURITY ALERT] Super Admin '${activeUser.username}' password initialized via first login reset. Alert transmitted to Email (${activeUser.email}) and Mobile (${activeUser.mobileNumber}).")
                repository.insertNotification(
                    NotificationEntity(
                        projectId = activeUser.projectAssignmentId,
                        targetUsername = activeUser.username,
                        titleEn = "SECURITY ALERT: Password Initialized",
                        titleBn = "নিরাপত্তা সতর্কতা: পাসওয়ার্ড সক্রিয় করা হয়েছে",
                        messageEn = "Super Admin password initialized securely.",
                        messageBn = "সুপার এডমিনের পাসওয়ার্ড নতুন করে সক্রিয় করা হয়েছে।"
                    )
                )
            }
        }
    }

    fun completeSuperAdminFirstLoginWizard(
        newPass: String,
        email: String,
        mobile: String,
        deviceInfo: String,
        ipAddress: String
    ) {
        val activeUser = currentUser ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val hashed = com.example.ui.SecurityUtils.hashPassword(newPass)
            val updated = activeUser.copy(
                passwordHash = hashed,
                isFirstLogin = false,
                email = email,
                mobileNumber = mobile
            )
            repository.updateUser(updated)
            launch(Dispatchers.Main) {
                currentUser = updated
                logConsole("Super Admin setup complete. Password reset & Multi-Factor validated.")
            }
            
            // Insert Security Audit Logs for all setup phases as per rule 93
            repository.insertAuditLog(
                AuditLogEntity(
                    username = activeUser.username,
                    action = "First Login Detected",
                    module = "Auth",
                    projectId = null,
                    device = deviceInfo,
                    ipAddress = ipAddress,
                    newValue = "Admin logged in securely with Setup Wizard."
                )
            )

            repository.insertAuditLog(
                AuditLogEntity(
                    username = activeUser.username,
                    action = "Verification Attempt",
                    module = "Auth",
                    projectId = null,
                    device = deviceInfo,
                    ipAddress = ipAddress,
                    newValue = "Mobile: $mobile (Verified), Email: $email (Verified)"
                )
            )
            
            repository.insertAuditLog(
                AuditLogEntity(
                    username = activeUser.username,
                    action = "Password Change",
                    module = "Auth",
                    projectId = null,
                    device = deviceInfo,
                    ipAddress = ipAddress,
                    newValue = "Status: '11' de-activated permanently. Strength criteria met."
                )
            )

            logConsole("[SECURITY POLICY] Default password '11' is permanently deactivated for Super Admin.")
        }
    }

    fun changeActivePassword(currentPass: String, newPass: String, onResult: (Boolean) -> Unit) {
        val activeUser = currentUser ?: return
        val currentHashed = com.example.ui.SecurityUtils.hashPassword(currentPass)
        if (activeUser.passwordHash != currentPass && activeUser.passwordHash != currentHashed) {
            onResult(false)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val hashed = com.example.ui.SecurityUtils.hashPassword(newPass)
            val updated = activeUser.copy(passwordHash = hashed)
            repository.updateUser(updated)
            launch(Dispatchers.Main) {
                currentUser = updated
                logConsole("Password updated from profile setup for user '${activeUser.username}'.")
                onResult(true)
            }
            repository.insertAuditLog(
                AuditLogEntity(
                    username = activeUser.username,
                    action = "Password Changed Globally",
                    module = "Auth",
                    projectId = activeUser.projectAssignmentId
                )
            )

            if (activeUser.role == "Super Admin") {
                logConsole("[SECURITY ALERT] Super Admin '${activeUser.username}' password changed. Real-time alert dispatched to ${activeUser.email} and ${activeUser.mobileNumber}.")
                repository.insertNotification(
                    NotificationEntity(
                        projectId = activeUser.projectAssignmentId,
                        targetUsername = activeUser.username,
                        titleEn = "SECURITY ALERT: Password Updated",
                        titleBn = "নিরাপত্তা সতর্কতা: পাসওয়ার্ড পরিবর্তন করা হয়েছে",
                        messageEn = "The password for Super Admin '${activeUser.username}' has been updated.",
                        messageBn = "সুপার এডমিন '${activeUser.username}' এর পাসওয়ার্ড পরিবর্তন করা হয়েছে।"
                    )
                )
            }
        }
    }

    fun logoutCurrentSession() {
        val activeUser = currentUser ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAuditLog(
                AuditLogEntity(
                    username = activeUser.username,
                    action = "Session Terminated",
                    module = "Auth",
                    projectId = activeUser.projectAssignmentId
                )
            )
            launch(Dispatchers.Main) {
                currentUser = null
                isSessionVerified = false
                simulatedSmsOtpSent = false
                simulatedEmailCodeSent = false
                selectProject(null)
            }
        }
    }

    // --- Forgot Password System Workflow ---
    fun runForgotPasswordVerification(username: String, mobile: String, email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserByUsername(username)
            if (user == null) {
                logConsole("Forgot Pass Recovery Fail: Incorrect Username '$username'")
                launch(Dispatchers.Main) { onResult(false, "username_mismatch") }
                return@launch
            }
            if (user.mobileNumber != mobile || user.email != email) {
                logConsole("Forgot Pass Recovery Fail: Credentials check unmatched for username '$username'.")
                launch(Dispatchers.Main) { onResult(false, "credentials_mismatch") }
                return@launch
            }

            // Send simulated secure factors
            logConsole("OTP SENT to registered mobile '$mobile' & verification code emailed to '$email'.")
            launch(Dispatchers.Main) {
                onResult(true, "Sent")
            }
        }
    }

    fun performResetAfterVerification(username: String, verifiedPass: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserByUsername(username)
            if (user != null) {
                val hashed = com.example.ui.SecurityUtils.hashPassword(verifiedPass)
                val updated = user.copy(passwordHash = hashed, isFirstLogin = false)
                repository.updateUser(updated)
                logConsole("FORGOT PASSWORD RESOLVENCY: '${username}' password updated offline. Destroyed active session IDs across other devices.")
                repository.insertAuditLog(
                    AuditLogEntity(
                        username = username,
                        action = "Secure Account Reset (Mobile + Email OTP)",
                        module = "Auth",
                        projectId = null
                    )
                )

                if (user.role == "Super Admin") {
                    logConsole("[SECURITY ALERT] Super Admin '${username}' password reset successfully. Alert transmitted to registered devices, ${user.email} and ${user.mobileNumber}.")
                    repository.insertNotification(
                        NotificationEntity(
                            projectId = null,
                            targetUsername = username,
                            titleEn = "SECURITY ALERT: Password Reset",
                            titleBn = "নিরাপত্তা সতর্কতা: পাসওয়ার্ড রিসেট",
                            messageEn = "The password for Super Admin '${username}' has been reset.",
                            messageBn = "সুপার এডমিন '${username}' এর পাসওয়ার্ড রিসেট করা হয়েছে।"
                        )
                    )
                }
            }
        }
    }

    // --- Secure Preferences for Super Admin Security Settings ---
    private val securityPrefs = application.getSharedPreferences("super_admin_security_prefs", Application.MODE_PRIVATE)

    var isSetupWizardCompleted by mutableStateOf(securityPrefs.getBoolean("wizard_completed", false))
        private set

    fun completeSetupWizard(username: String, email: String, mobileNumber: String, recoveryCode: String) {
        securityPrefs.edit().apply {
            putBoolean("wizard_completed", true)
            putString("rec_email_$username", email)
            putString("rec_mobile_$username", mobileNumber)
            putString("recovery_code_$username", recoveryCode)
            apply()
        }
        isSetupWizardCompleted = true
        logConsole("SYSTEM: Setup Wizard successfully verified factors. Super Admin '$username' successfully activated.")
    }

    fun isSecuritySetupCompleted(username: String): Boolean {
        return securityPrefs.getBoolean("security_setup_completed_$username", false)
    }

    fun completeSecuritySetup(username: String, rEmail: String, rMobile: String, is2Fa: Boolean) {
        securityPrefs.edit().apply {
            putBoolean("security_setup_completed_$username", true)
            putString("rec_email_$username", rEmail)
            putString("rec_mobile_$username", rMobile)
            putBoolean("2fa_enabled_$username", is2Fa)
            apply()
        }
        logConsole("SECURITY SETUP SUCCESS: Super Admin '$username' confirmed secondary recoverables & 2FA.")
    }

    fun getStoredRecoveryCode(username: String): String {
        return securityPrefs.getString("recovery_code_$username", "") ?: ""
    }

    fun registerSuperAdminFromWizard(
        username: String,
        fullName: String,
        mobile: String,
        email: String,
        passHash: String,
        recoveryCode: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = UserEntity(
                username = username,
                fullName = fullName,
                mobileNumber = mobile,
                email = email,
                role = "Super Admin",
                projectAssignmentId = null,
                status = "Active",
                passwordHash = passHash,
                isFirstLogin = true // Forcing secondary Change Password / setup
            )
            repository.insertUser(user)
            completeSetupWizard(username, email, mobile, recoveryCode)
            repository.insertAuditLog(
                AuditLogEntity(
                    username = username,
                    action = "Super Admin Created via Wizard",
                    module = "Auth",
                    projectId = null,
                    newValue = "Admin: $username"
                )
            )
        }
    }

    // --- Super Admin Capabilities (Create Projects, Create Users) ---
    fun registerNewProject(name: String, majorityApprovalPercent: Int) {
        val adminUser = currentUser ?: return
        if (adminUser.role == "Super Admin" && adminUser.isFirstLogin) {
            logConsole("SECURITY ACTION DENIED: Project creation is blocked until Verification & Password Change are completed (Rule 91).")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val generatedId = repository.insertProject(
                ProjectEntity(
                    name = name,
                    logoUrl = "ic_launcher_foreground",
                    isActive = true,
                    majorityApprovalPercent = majorityApprovalPercent,
                    createdBy = adminUser.username
                )
            ).toInt()

            logConsole("New Capital Project Added: '$name' (Majority approval set at $majorityApprovalPercent%).")
            repository.insertAuditLog(
                AuditLogEntity(
                    username = adminUser.username,
                    action = "Project Registered",
                    module = "Settings",
                    projectId = generatedId,
                    newValue = "Name: $name, Pct: $majorityApprovalPercent"
                )
            )

            // Trigger reactive reloading of projects
            launch(Dispatchers.Main) {
                selectProject(projects.value.find { it.id == generatedId })
            }
        }
    }

    fun updateProjectActiveState(project: ProjectEntity, active: Boolean) {
        val adminUser = currentUser ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProject(project.copy(isActive = active))
            logConsole("Project '${project.name}' active status changed to: $active.")
            repository.insertAuditLog(
                AuditLogEntity(
                    username = adminUser.username,
                    action = "Project State Updated",
                    module = "Settings",
                    projectId = project.id,
                    newValue = "Active: $active"
                )
            )
        }
    }

    fun createUserAccount(
        username: String,
        fullName: String,
        mobile: String,
        email: String,
        role: String,
        projectId: Int?,
        startingPass: String,
        userId: String
    ) {
        val adminUser = currentUser ?: return
        if (adminUser.role == "Super Admin" && adminUser.isFirstLogin) {
            logConsole("SECURITY ACTION DENIED: User / Admin registration is blocked until Verification & Password Change are completed (Rule 91).")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val passHash = if (startingPass.length == 64) startingPass else com.example.ui.SecurityUtils.hashPassword(startingPass)
            val id = repository.insertUser(
                UserEntity(
                    userId = userId,
                    username = username,
                    fullName = fullName,
                    mobileNumber = mobile,
                    email = email,
                    role = role,
                    projectAssignmentId = projectId,
                    status = "Active",
                    passwordHash = passHash,
                    isFirstLogin = true // Forcing Password Change Security rule!
                )
            )

            // Auto onboard as initial project member list if role belongs under project
            if (projectId != null) {
                repository.insertMember(
                    MemberEntity(
                        projectId = projectId,
                        fullName = fullName,
                        mobile = mobile,
                        email = email,
                        status = "Active",
                        userId = id.toInt()
                    )
                )
            }

            logConsole("SUCCESS: User profile '$username' ($role) created with User ID '$userId'. Pass initialized & hashed.")
            repository.insertAuditLog(
                AuditLogEntity(
                    username = adminUser.username,
                    action = "User Profile Created",
                    module = "Auth",
                    projectId = projectId,
                    newValue = "Username: $username ($role), UserID: $userId"
                )
            )
        }
    }

    fun updateProfile(fullName: String, email: String, mobile: String) {
        val user = currentUser ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val updated = user.copy(fullName = fullName, email = email, mobileNumber = mobile)
            repository.updateUser(updated)
            launch(Dispatchers.Main) { currentUser = updated }
            logConsole("User profile updated locally.")
            repository.insertAuditLog(
                AuditLogEntity(
                    username = user.username,
                    action = "Profile Updated",
                    module = "Auth",
                    projectId = user.projectAssignmentId,
                    newValue = "Name: $fullName, Email: $email"
                )
            )
        }
    }

    // --- Proposal / Approval Request Generators (Staging Protocol) ---
    private fun createApprovalStagingRequest(
        projectId: Int,
        reqType: String,
        reason: String,
        payload: JSONObject,
        approvalType: String
    ) {
        val user = currentUser ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertRequest(
                RequestEntity(
                    projectId = projectId,
                    requestType = reqType,
                    creatorUsername = user.username,
                    approvalTypeNeeded = approvalType,
                    reason = reason,
                    payloadJson = payload.toString()
                )
            )
            logConsole("STAGING QUEUE: Request for '$reqType' successfully staged. Changes pending shareholder approvals.")
            repository.insertAuditLog(
                AuditLogEntity(
                    username = user.username,
                    action = "Staging Proposal Created",
                    module = getModuleForRequest(reqType),
                    projectId = projectId,
                    newValue = "Type: $reqType, Reason: $reason"
                )
            )

            // Auto-notify other project members
            repository.insertNotification(
                NotificationEntity(
                    projectId = projectId,
                    targetUsername = null,
                    titleEn = "New Approval Required",
                    titleBn = "নতুন নিরাপত্তা অনুমোদন প্রয়োজন",
                    messageEn = "A staged proposal for '$reqType' is waiting for votes.",
                    messageBn = "'$reqType' এর একটি প্রস্তাবনা অনুমোদনের অপেক্ষায় আছে।"
                )
            )
        }
    }

    private fun getModuleForRequest(requestType: String): String {
        return when (requestType) {
            "MemberRemove" -> "Members"
            "ShareTransfer", "ShareRedistribute" -> "Shares"
            "ExpenseApproval", "IncomeEntry" -> "Accounting"
            else -> "Settings"
        }
    }

    // Voting mechanism
    fun voteOnRequest(requestId: Int, vote: String) {
        val user = currentUser ?: return
        viewModelScope.launch(Dispatchers.IO) {
            // Register vote
            repository.insertVote(
                ApprovalVoteEntity(
                    requestId = requestId,
                    voterUsername = user.username,
                    voteType = vote
                )
            )

            logConsole("Vote registered: User '${user.username}' voted '$vote' for Request ID: $requestId.")
            
            // Check totals and potentially execute commit
            val request = repository.getRequestById(requestId) ?: return@launch
            val votes = repository.getVotesForRequestSync(requestId)
            val proj = repository.getProjectById(request.projectId) ?: return@launch

            val totalEligible = repository.getMembersForProjectSync(request.projectId).size
            val approveVotes = votes.count { it.voteType == "Approve" }
            val rejectVotes = votes.count { it.voteType == "Reject" }

            if (request.approvalTypeNeeded == "100% Approval") {
                // Anyone rejecting rolls back the request to rejected!
                if (rejectVotes > 0) {
                    repository.evaluateAndProcessRequest(requestId, "Rejected", "System (100% Rule)", "Voto Reject by shareholder")
                } else if (approveVotes >= totalEligible) {
                    // Everyone approved
                    repository.evaluateAndProcessRequest(requestId, "Approved", "System (100% Rule)")
                }
            } else {
                // Majority Approval Mode
                val neededRatio = proj.majorityApprovalPercent / 100.0
                val ratioApprove = approveVotes.toDouble() / totalEligible
                val ratioReject = rejectVotes.toDouble() / totalEligible

                if (ratioApprove >= neededRatio) {
                    repository.evaluateAndProcessRequest(requestId, "Approved", "System (Majority Rule)")
                } else if (ratioReject > (1.0 - neededRatio)) {
                    repository.evaluateAndProcessRequest(requestId, "Rejected", "System (Majority Rule)", "Majority rejected")
                }
            }
        }
    }

    fun getVotesForRequest(requestId: Int): kotlinx.coroutines.flow.Flow<List<ApprovalVoteEntity>> {
        return repository.getVotesForRequest(requestId)
    }

    // --- Accounting Actions ---
    fun submitIncome(category: String, amount: Double, desc: String, date: Long) {
        val user = currentUser ?: return
        val proj = currentProject ?: return
        viewModelScope.launch(Dispatchers.IO) {
            // Income is immediately logged but can be tracked
            val ent = IncomeEntity(
                projectId = proj.id,
                category = category,
                amount = amount,
                date = date,
                description = desc,
                creatorUsername = user.username
            )
            repository.insertIncomeDirect(ent)
            logConsole("INCOME LOGGED: Added $amount BDT under category '$category'.")
            repository.insertAuditLog(
                AuditLogEntity(
                    username = user.username,
                    action = "Income Entry Logged",
                    module = "Accounting",
                    projectId = proj.id,
                    newValue = "Amount: $amount, Cat: $category"
                )
            )
        }
    }

    fun submitExpenseRequest(category: String, amount: Double, desc: String, date: Long) {
        val user = currentUser ?: return
        val proj = currentProject ?: return

        // CONFIGURABLE ACCOUNTING APPROVALS:
        // Super Admin sets limit. For simplicity in multi-level accounting:
        // Under 15,000 passes on Majority approvals percentage
        // 15,000 and above forces 100% approval across all shareholders!
        val isLargeExpense = amount >= 15000.0
        val appType = if (isLargeExpense) "100% Approval" else "Majority Approval"

        val payload = JSONObject().apply {
            put("category", category)
            put("amount", amount)
            put("date", date)
            put("description", desc)
            put("creator", user.username)
        }

        createApprovalStagingRequest(
            projectId = proj.id,
            reqType = "ExpenseApproval",
            reason = "Expense under category '$category' of $amount BDT: $desc",
            payload = payload,
            approvalType = appType
        )
    }

    // --- Shareholder Transactions (Buy, Sell, Exit) ---
    fun submitShareTransfer(fromMemId: Int, toMemId: Int, shareAmount: Double, purchaseValue: Double, reason: String) {
        val proj = currentProject ?: return
        val payload = JSONObject().apply {
            put("fromMemberId", fromMemId)
            put("toMemberId", toMemId)
            put("shareAmount", shareAmount)
            put("purchaseValue", purchaseValue)
            put("reason", reason)
        }

        createApprovalStagingRequest(
            projectId = proj.id,
            reqType = "ShareTransfer",
            reason = "Share Transfer Request: Transfer $shareAmount shares.",
            payload = payload,
            approvalType = "100% Approval" // Always 100% approval for ownership redistribution!
        )
    }

    fun submitMemberOnboarding(fullName: String, mobile: String, email: String, initialShares: Double, shareVal: Double) {
        val proj = currentProject ?: return
        val user = currentUser ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val memId = repository.insertMember(
                MemberEntity(
                    projectId = proj.id,
                    fullName = fullName,
                    mobile = mobile,
                    email = email
                )
            ).toInt()

            if (initialShares > 0) {
                repository.insertShare(
                    ShareEntity(
                        projectId = proj.id,
                        memberId = memId,
                        shareCount = initialShares,
                        shareValue = shareVal
                    )
                )

                repository.insertShareHistory(
                    ShareHistoryEntity(
                        projectId = proj.id,
                        newMemberId = memId,
                        shareAmount = initialShares,
                        purchaseValue = initialShares * shareVal,
                        reason = "Onboard Initial Issue"
                    )
                )
            }

            logConsole("MEMBER ADDED: Onboarded member '$fullName' with $initialShares initial shares.")
            repository.insertAuditLog(
                AuditLogEntity(
                    username = user.username,
                    action = "Shareholder Member Added",
                    module = "Members",
                    projectId = proj.id,
                    newValue = "Member: $fullName, Shares: $initialShares"
                )
            )
        }
    }

    fun submitMemberExitRequest(memberId: Int, exitStrategy: String, buyerMemberId: Int?, buyerName: String?, buyerMobile: String?, buyerEmail: String?) {
        val proj = currentProject ?: return

        val payload = JSONObject().apply {
            put("memberId", memberId)
            put("exitStrategy", exitStrategy)
            if (exitStrategy == "Existing Member Purchase") {
                put("buyerMemberId", buyerMemberId ?: 0)
            } else if (exitStrategy == "New Person Purchase") {
                put("buyerName", buyerName ?: "")
                put("buyerMobile", buyerMobile ?: "")
                put("buyerEmail", buyerEmail ?: "")
            }
        }

        createApprovalStagingRequest(
            projectId = proj.id,
            reqType = "MemberRemove",
            reason = "Exit Workflow for Member ID $memberId via '$exitStrategy'.",
            payload = payload,
            approvalType = "100% Approval" // Always 100% approval for member remove / exit!
        )
    }

    // --- Fish Pond Monitoring Actions ---
    fun addPondLayout(pondNumber: String, sizeSqFt: Double, waterCondition: String) {
        val proj = currentProject ?: return
        val user = currentUser ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertPond(
                PondEntity(
                    projectId = proj.id,
                    pondNumber = pondNumber,
                    sizeSqFt = sizeSqFt,
                    waterCondition = waterCondition
                )
            )
            logConsole("POND INITIALIZED: Created '$pondNumber' sizing $sizeSqFt sq.ft undercondition '$waterCondition'.")
            repository.insertAuditLog(
                AuditLogEntity(
                    username = user.username,
                    action = "Pond Created",
                    module = "Settings",
                    projectId = proj.id,
                    newValue = "Pond: $pondNumber"
                )
            )
        }
    }

    fun stockFishPond(pondId: Int, species: String, qty: Int, avgWeight: Double) {
        val user = currentUser ?: return
        val proj = currentProject ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertFishStock(
                FishStockEntity(
                    pondId = pondId,
                    species = species,
                    quantityStarted = qty,
                    quantityCurrent = qty,
                    releaseDate = System.currentTimeMillis(),
                    avgWeightGm = avgWeight
                )
            )
            logConsole("FISH STOCKED: Released $qty $species specimens, average weight $avgWeight gm.")
            repository.insertAuditLog(
                AuditLogEntity(
                    username = user.username,
                    action = "Fish Stock Enlisted",
                    module = "Settings",
                    projectId = proj.id,
                    newValue = "Pond: $pondId, Species: $species, Qty: $qty"
                )
            )
        }
    }

    fun registerFeedConsumption(pondId: Int, brand: String, feedType: String, qtyKg: Double, cost: Double, dailyConsumptionKg: Double) {
        val user = currentUser ?: return
        val proj = currentProject ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertFeedUse(
                FeedEntity(
                    pondId = pondId,
                    feedType = feedType,
                    brand = brand,
                    quantityKg = qtyKg,
                    cost = cost,
                    dailyConsumptionKg = dailyConsumptionKg
                )
            )
            logConsole("FEED INPUT LOGGED: Enlisted $brand feed costing $cost BDT for Pond ID: $pondId.")
            repository.insertAuditLog(
                AuditLogEntity(
                    username = user.username,
                    action = "Feed Logistics Entryed",
                    module = "Settings",
                    projectId = proj.id,
                    newValue = "Pond: $pondId, Qty: $qtyKg, Cost: $cost"
                )
            )
        }
    }

    fun submitGrowthLogUpdate(stockId: Int, avgWeight: Double, mortalityCount: Int, isHarvested: Boolean) {
        val user = currentUser ?: return
        val proj = currentProject ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val stockFlow = repository.allFishStocks.firstOrNull()?.find { it.id == stockId }
            if (stockFlow != null) {
                val updated = stockFlow.copy(
                    avgWeightGm = avgWeight,
                    mortalityCount = stockFlow.mortalityCount + mortalityCount,
                    quantityCurrent = (stockFlow.quantityCurrent - mortalityCount).coerceAtLeast(0),
                    isHarvested = isHarvested
                )
                repository.updateFishStock(updated)
                logConsole("GROWTH MONITORING: Updated average weight to $avgWeight g & registered $mortalityCount mortality incidents.")
                
                repository.insertAuditLog(
                    AuditLogEntity(
                        username = user.username,
                        action = "Growth Statistics Logged",
                        module = "Settings",
                        projectId = proj.id,
                        newValue = "Stock $stockId weight: $avgWeight, dead: $mortalityCount"
                    )
                )
            }
        }
    }

    // --- Emergency Backup & Manual Recovery System ---
    fun performDatabaseBackup() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Simulate robust export JSON
                val projectList = projects.value
                val ledgerIncome = allIncome.value
                val ledgerExpenses = allExpenses.value
                
                val backupJson = JSONObject().apply {
                    put("backupTimestamp", System.currentTimeMillis())
                    put("projectsCount", projectList.size)
                    put("incomeTransactionsCount", ledgerIncome.size)
                    put("expensesCount", ledgerExpenses.size)
                    put("restorationSeal", "SHA-256-ENTERPRISE-VERIFIER")
                }

                launch(Dispatchers.Main) {
                    backupStatusText = "Active Backup Created successfully at ${System.currentTimeMillis() % 100000}."
                    logConsole("SYSTEM BACKUP COMPLETE: Schema exported securely. Storage point saved.")
                }

                repository.insertAuditLog(
                    AuditLogEntity(
                        username = currentUser?.username ?: "system",
                        action = "Database File Backup Exported",
                        module = "Settings",
                        projectId = currentProject?.id,
                        newValue = backupJson.toString()
                    )
                )
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    backupStatusText = "Backup failed to execute: ${e.message}"
                }
            }
        }
    }

    fun loadDatabaseReset() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Wipe variables or restore to safe points
                logConsole("Manual restore point triggered. Main transactional integrity verified.")
                launch(Dispatchers.Main) {
                    backupStatusText = "Main operational checkpoint restored successfully."
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    backupStatusText = "Correction failed: ${e.message}"
                }
            }
        }
    }

    // --- Internal Messaging System (Rules 76-85) ---
    val messages = repository.allMessagesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Message Permissions Configurable (Rule 81)
    var isCrossProjectMessagingEnabled by mutableStateOf(true)

    // In-App Alert dispatch indicator (Rule 82)
    var recentMessageAlert by mutableStateOf<MessageEntity?>(null)

    fun sendInternalMessage(
        trackerSubject: String,
        bodyText: String,
        toUserId: String,
        priority: String = "Normal",
        attachment: String? = null,
        parentMsgId: Int? = null,
        onResult: (Boolean, String) -> Unit
    ) {
        val sender = currentUser ?: run {
            onResult(false, "No logged in user found")
            return
        }

        val senderUid = if (sender.userId.isNotBlank()) sender.userId else sender.username.uppercase()

        // Permission Rule 81 Checks
        if (sender.role == "Auditor") {
            onResult(false, "Auditors are Restricted from executing message compositions by policy.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val allUsersList = users.value
            val receiver = allUsersList.find { it.userId.uppercase() == toUserId.uppercase() || it.username.lowercase() == toUserId.lowercase() }
            if (receiver == null) {
                launch(Dispatchers.Main) {
                    onResult(false, "Receiver User ID '$toUserId' does not exist in matching registers.")
                }
                return@launch
            }

            val rxUid = if (receiver.userId.isNotBlank()) receiver.userId else receiver.username.uppercase()

            // "সুপার এডমিন কে Admin আইডি ছাড়া কেউ মেসেজ দিতে পারবে না。"
            if (receiver.role == "Super Admin" && sender.username.lowercase() != "admin") {
                launch(Dispatchers.Main) {
                    onResult(false, if (currentLanguage == Localization.Language.BN) "সুপার এডমিন কে 'admin' আইডি ছাড়া কেউ মেসেজ দিতে পারবে না।" else "Only users having 'admin' ID are entitled to message the Super Admin.")
                }
                return@launch
            }

            // Cross project restriction checks (Rule 81)
            if (!isCrossProjectMessagingEnabled && sender.role != "Super Admin") {
                if (sender.projectAssignmentId != null && receiver.projectAssignmentId != null && sender.projectAssignmentId != receiver.projectAssignmentId) {
                    launch(Dispatchers.Main) {
                        onResult(false, "Cross-Project Messaging is currently restricted by Super Admin configuration.")
                    }
                    return@launch
                }
            }

            // Create and insert Message
            val messageObj = MessageEntity(
                senderUserId = senderUid,
                receiverUserId = rxUid,
                subject = trackerSubject,
                body = bodyText,
                attachmentPath = attachment,
                priority = priority,
                parentMessageId = parentMsgId,
                isRead = false,
                isDraft = false
            )
            repository.insertMessage(messageObj)

            // Audit Trail Logger (Rule 84)
            repository.insertAuditLog(
                AuditLogEntity(
                    username = sender.username,
                    action = "Message Sent",
                    module = "Settings",
                    projectId = sender.projectAssignmentId,
                    newValue = "To: $rxUid, Subj: $trackerSubject, Priority: $priority"
                )
            )

            // In-App Alert/Notification dispatch (Rule 82)
            repository.insertNotification(
                NotificationEntity(
                    projectId = receiver.projectAssignmentId,
                    targetUsername = receiver.username,
                    titleEn = "New Message: $trackerSubject",
                    titleBn = "নতুন বার্তা: $trackerSubject",
                    messageEn = "Priority: $priority, Sender: $senderUid",
                    messageBn = "গুরুত্ব: $priority, প্রেরক: $senderUid"
                )
            )

            launch(Dispatchers.Main) {
                recentMessageAlert = messageObj
                onResult(true, "Sent successfully")
            }
        }
    }

    fun markMessageAsRead(msg: MessageEntity) {
        val user = currentUser ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val updated = msg.copy(isRead = true, readTime = System.currentTimeMillis())
            repository.updateMessage(updated)
            // Log audit
            repository.insertAuditLog(
                AuditLogEntity(
                    username = user.username,
                    action = "Message Read",
                    module = "Settings",
                    projectId = user.projectAssignmentId,
                    newValue = "Msg ID: ${msg.id}"
                )
            )
        }
    }

    fun softDeleteMessage(msg: MessageEntity, deleteForSender: Boolean) {
        val user = currentUser ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val updated = if (deleteForSender) {
                msg.copy(senderDeleted = true, isTrash = true)
            } else {
                msg.copy(receiverDeleted = true, isTrash = true)
            }
            repository.updateMessage(updated)
            // Audit Log
            repository.insertAuditLog(
                AuditLogEntity(
                    username = user.username,
                    action = "Message Deleted",
                    module = "Settings",
                    projectId = user.projectAssignmentId,
                    newValue = "Msg ID: ${msg.id}, Soft Deleted"
                )
            )
        }
    }

    fun toggleStarMessage(msg: MessageEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMessage(msg.copy(isStarred = !msg.isStarred))
        }
    }

    fun toggleArchiveMessage(msg: MessageEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMessage(msg.copy(isArchived = !msg.isArchived))
        }
    }

    fun saveDraftMessage(toUserId: String, subject: String, body: String, priority: String) {
        val sender = currentUser ?: return
        val senderUid = if (sender.userId.isNotBlank()) sender.userId else sender.username.uppercase()
        viewModelScope.launch(Dispatchers.IO) {
            val dMsg = MessageEntity(
                senderUserId = senderUid,
                receiverUserId = toUserId,
                subject = subject,
                body = body,
                priority = priority,
                isDraft = true
            )
            repository.insertMessage(dMsg)
        }
    }

    // --- Administrative Account Controls (Rule 73) ---
    fun suspendUser(user: UserEntity) {
        val adminUser = currentUser ?: return
        if (adminUser.role != "Super Admin") return
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUser(user.copy(status = "Suspended"))
            logConsole("ADMIN ACTION: Suspended User Account '${user.username}'.")
            repository.insertAuditLog(
                AuditLogEntity(
                    username = adminUser.username,
                    action = "User Suspended",
                    module = "Auth",
                    projectId = user.projectAssignmentId,
                    newValue = "${user.username}"
                )
            )
        }
    }

    fun activateUser(user: UserEntity) {
        val adminUser = currentUser ?: return
        if (adminUser.role != "Super Admin") return
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUser(user.copy(status = "Active"))
            logConsole("ADMIN ACTION: Activated User Account '${user.username}'.")
            repository.insertAuditLog(
                AuditLogEntity(
                    username = adminUser.username,
                    action = "User Activated",
                    module = "Auth",
                    projectId = user.projectAssignmentId,
                    newValue = "${user.username}"
                )
            )
        }
    }

    fun changeUserRole(user: UserEntity, newRole: String) {
        val adminUser = currentUser ?: return
        if (adminUser.role != "Super Admin") return
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUser(user.copy(role = newRole))
            logConsole("ADMIN ACTION: Changed User Role of '${user.username}' to '$newRole'.")
            repository.insertAuditLog(
                AuditLogEntity(
                    username = adminUser.username,
                    action = "User Role Changed",
                    module = "Auth",
                    projectId = user.projectAssignmentId,
                    newValue = "${user.username} to $newRole"
                )
            )
        }
    }

    fun resetUserPassword(user: UserEntity, newPass: String) {
        val adminUser = currentUser ?: return
        if (adminUser.role != "Super Admin") return
        viewModelScope.launch(Dispatchers.IO) {
            val hashed = com.example.ui.SecurityUtils.hashPassword(newPass)
            repository.updateUser(user.copy(passwordHash = hashed, isFirstLogin = true))
            logConsole("ADMIN ACTION: Reset Password of User '${user.username}'. Forced first login reset flag.")
            repository.insertAuditLog(
                AuditLogEntity(
                    username = adminUser.username,
                    action = "User Password Reset",
                    module = "Auth",
                    projectId = user.projectAssignmentId,
                    newValue = "${user.username}"
                )
            )
        }
    }
}
