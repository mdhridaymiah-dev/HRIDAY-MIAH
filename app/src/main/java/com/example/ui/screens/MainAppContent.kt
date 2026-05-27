package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.*
import com.example.ui.Localization
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val user = viewModel.currentUser

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        if (user == null) {
            // Authentication Block
            AuthScreen(viewModel)
        } else if (!viewModel.isSessionVerified) {
            // All-User Post-Login Secure Verification Screen (Rule 94, 95)
            PostLoginVerificationScreen(viewModel)
        } else if (user.isFirstLogin) {
            // Mandated First Login Password Change Gate
            ForcePasswordResetScreen(viewModel)
        } else {
            // Regular SaaS Portal
            AuthenticatedPortal(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostLoginVerificationScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val user = viewModel.currentUser ?: return
    
    var smsInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var tfaInput by remember { mutableStateOf("") }
    var rememberDeviceChecked by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }
    var successText by remember { mutableStateOf("") }

    // Send SMS / Email simulation triggers on first composition
    LaunchedEffect(user.username) {
        viewModel.simulatedSmsOtpSent = true
        viewModel.simulatedEmailCodeSent = true
    }

    val titleText = if (lang == Localization.Language.BN) "পোস্ট-লগইন সিকিউরিটি ভেরিফিকেশন" else "Post-Login Security Verification"
    val subtitleText = if (lang == Localization.Language.BN) 
        "আপনার অ্যাকাউন্ট সুরক্ষার অংশ হিসেবে অনুগ্রহ করে নিচের ভেরিফিকেশন সম্পন্ন করুন।" 
        else "To maintain corporate data protocol, complete multi-factor verification."

    val mobileOtpLabel = if (lang == Localization.Language.BN) "মোবাইল ওটিপি কোড (৪ ডিজিট)" else "Mobile OTP Code (4 Digits)"
    val emailCodeLabel = if (lang == Localization.Language.BN) "ইমেইল ভেরিফিকেশন কোড" else "Email Verification Code"
    val tfaLabel = if (lang == Localization.Language.BN) "অথেন্টিকেটর ২এফএ কোড (ঐচ্ছিক)" else "Authenticator 2FA Code (Optional)"
    val verifyBtnText = if (lang == Localization.Language.BN) "যাচাই করুন এবং প্রবেশ করুন" else "Verify & Enter Portal"
    val resendBtnText = if (lang == Localization.Language.BN) "কোড পুনরায় পাঠান" else "Resend Verification Codes"
    val logoutBtnText = if (lang == Localization.Language.BN) "অন্য অ্যাকাউন্টে লগইন করুন" else "Cancel & Logout"
    val rememberDeviceText = if (lang == Localization.Language.BN) "এই যন্ত্রটি মনে রাখুন (Remember This Device)" else "Remember This Device (Trusted)"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App logo or Lock animation
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Shield Lock",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = titleText,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // Active Policy Configurator for demonstration (Super Admin can change this config)
        if (user.role == "Super Admin") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, "Config", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Super Admin Simulation Sandbox Policy Control",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Select between policies
                        val policies = listOf(
                            "Option-1: Every Login Verification",
                            "Option-2: New Device Verification",
                            "Option-4: Suspicious Login Verification"
                        )
                        policies.forEach { pol ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.loginVerificationPolicy = pol }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = (viewModel.loginVerificationPolicy == pol),
                                    onClick = { viewModel.loginVerificationPolicy = pol }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(pol, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        // Simulation HUD Box displaying SMS/Email code to the tester
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            "Simulation Signals",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (lang == Localization.Language.BN) "সিমুলেশন ওটিপি সংকেত (ডেমো মোড)" else "Simulation MFA Signals (Demo Mode)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📱 Simulated SMS OTP: ${viewModel.simulatedSmsOtp}\n✉️ Simulated Email PIN: ${viewModel.simulatedEmailCode}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = if (lang == Localization.Language.BN) 
                            "* নতুন এবং নিয়মিত ইউজারদের জন্য এই কোডগুলো আবশ্যক।" 
                            else "* Required for first login and standard compliant validation.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Verification Input Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Mobile OTP Input
                    OutlinedTextField(
                        value = smsInput,
                        onValueChange = { smsInput = it },
                        label = { Text(mobileOtpLabel) },
                        leadingIcon = { Icon(Icons.Default.PhoneAndroid, "Phone") },
                        modifier = Modifier.fillMaxWidth().testTag("sms_otp_field"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // Email Code Input
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text(emailCodeLabel) },
                        leadingIcon = { Icon(Icons.Default.Email, "Email") },
                        modifier = Modifier.fillMaxWidth().testTag("email_pin_field"),
                        singleLine = true
                    )

                    // Authenticator App input
                    OutlinedTextField(
                        value = tfaInput,
                        onValueChange = { tfaInput = it },
                        label = { Text(tfaLabel) },
                        leadingIcon = { Icon(Icons.Default.VpnKey, "2FA Key") },
                        modifier = Modifier.fillMaxWidth().testTag("tfa_field"),
                        singleLine = true,
                        placeholder = { Text("6-digit code") }
                    )

                    // Remember This Device Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { rememberDeviceChecked = !rememberDeviceChecked }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = rememberDeviceChecked,
                            onCheckedChange = { rememberDeviceChecked = it },
                            modifier = Modifier.testTag("remember_device_checkbox")
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = rememberDeviceText,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Alerts Zone
                    if (errorText.isNotBlank()) {
                        Text(
                            text = errorText,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(vertical = 4.dp).testTag("mfa_error_alert")
                        )
                    }

                    if (successText.isNotBlank()) {
                        Text(
                            text = successText,
                            color = Color(0xFF2E7D32),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    // Primary Verification Submission
                    Button(
                        onClick = {
                            errorText = ""
                            successText = ""
                            
                            val isSmsValid = (smsInput.trim() == viewModel.simulatedSmsOtp)
                            val isEmailValid = (emailInput.trim() == viewModel.simulatedEmailCode)

                            if (isSmsValid && isEmailValid) {
                                // Enroll device as trusted if checked
                                if (rememberDeviceChecked) {
                                    if (!viewModel.trustedDevicesList.contains(viewModel.activeSessionDevice)) {
                                        viewModel.trustedDevicesList.add(viewModel.activeSessionDevice)
                                    }
                                }

                                successText = if (lang == Localization.Language.BN) "ভেরিফিকেশন সফল হয়েছে!" else "MFA Verification successful!"
                                viewModel.isSessionVerified = true
                                viewModel.dispatchLoginNotificationAndLogs(user)
                            } else {
                                val invalidMsg = if (lang == Localization.Language.BN) {
                                    "ভেরিফিকেশন কোড ভুল হয়েছে। সঠিক ওটিপি এবং পিন দিন।"
                                } else {
                                    "Invalid verification codes. Check your SMS OTP or email PIN again."
                                }
                                errorText = invalidMsg

                                // Log failed attempt to Audit Log as requested by Rule 104
                                viewModel.logFailedVerificationAttempt(user.username, "Invalid verification input codes. SMS input: '$smsInput', Email input: '$emailInput'")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("verify_mfa_submit_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(verifyBtnText, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    // Secondary Reset Simulation
                    OutlinedButton(
                        onClick = {
                            viewModel.simulatedSmsOtp = (1000..9999).random().toString()
                            viewModel.simulatedEmailCode = "SEC-" + (1000..9999).random().toString()
                            errorText = ""
                            successText = if (lang == Localization.Language.BN) "কোড পুনরায় পাঠানো হয়েছে!" else "Verification codes re-routed successfully!"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(resendBtnText)
                    }
                }
            }
        }

        // Footprint session attributes (Rule 100)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (lang == Localization.Language.BN) "🔐 ডিভাইস ও আইপি ফুটপ্রিন্ট লগ" else "🔐 Secure Footprint Logs",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Device:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(viewModel.activeSessionDevice, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Secure Browser:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(viewModel.activeSessionBrowser, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("IPv4 Address:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(viewModel.activeSessionIP, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Location:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(viewModel.activeSessionLocation, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Login Timestamp:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(viewModel.activeSessionTime, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Cancel Active Session Button
        item {
            TextButton(
                onClick = { viewModel.logoutActiveSession() },
                modifier = Modifier.testTag("mfa_logout_back_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowBack, "Back", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(logoutBtnText, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- SECURE AUTHENTICATION SCREEN ---
@Composable
fun AuthScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    var isForgotScreen by remember { mutableStateOf(false) }
    var showSetupWizard by remember { mutableStateOf(false) }

    if (showSetupWizard) {
        InstallationWizardScreen(viewModel, onDismiss = { showSetupWizard = false })
    } else if (isForgotScreen) {
        ForgotPasswordScreen(viewModel, onBack = { isForgotScreen = false })
    } else {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var errorText by remember { mutableStateOf("") }
        var infoText by remember { mutableStateOf("") }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant)
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showSetupWizard = true },
                        modifier = Modifier.testTag("launch_setup_wizard_btn")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Admin Wizard", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (!viewModel.isSetupWizardCompleted) "⚠️ Run Setup Wizard" else "Setup Wizard",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!viewModel.isSetupWizardCompleted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }

                    // Language Realtime Toggle
                    TextButton(
                        onClick = {
                            viewModel.currentLanguage =
                                if (lang == Localization.Language.EN) Localization.Language.BN else Localization.Language.EN
                        },
                        modifier = Modifier.testTag("auth_lang_switch")
                    ) {
                        Icon(Icons.Default.Language, contentDescription = "Lang")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Localization.translate("switch_lang", lang))
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Modern Branded Circular Avatar Representation
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Waves,
                        contentDescription = "App Logo",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = Localization.translate("app_title", lang),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Enterprise Multi-Project System",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Credentials Inputs
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        errorText = ""
                    },
                    label = { Text(Localization.translate("username", lang)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .testTag("login_username_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorText = ""
                    },
                    label = { Text(Localization.translate("password", lang)) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock") },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .testTag("login_password_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorText.isNotEmpty()) {
                    Text(
                        text = errorText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 12.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (username.isBlank()) {
                            errorText = Localization.translate("username_req", lang)
                        } else if (password.isBlank()) {
                            errorText = Localization.translate("password_req", lang)
                        } else {
                            viewModel.login(username, password) { success, code ->
                                if (!success) {
                                    errorText = Localization.translate(code, lang)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(52.dp)
                        .testTag("submit_login_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        Localization.translate("login", lang),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { isForgotScreen = true },
                    modifier = Modifier.testTag("forgot_password_btn")
                ) {
                    Text(
                        Localization.translate("forgot_pwd", lang),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Simulated factor warnings / Security diagnostic logs box
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = "Shield",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Corporate Security Gate",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            text = "Enterprise-grade multi-factor security enabled. All login attempts, IP logs, and system access sessions are encrypted and audited.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- SECURITY SCREEN FOR RECOVERY WORKFLOW (FORGOT PASSWORD) ---
@Composable
fun ForgotPasswordScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val lang = viewModel.currentLanguage
    var userLoginId by remember { mutableStateOf("") }
    var userMobile by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    
    var step by remember { mutableStateOf(1) } // 1: Info entry, 2: OTP verification + Reset allowed
    var mobileOtp by remember { mutableStateOf("") }
    var emailCode by remember { mutableStateOf("") }
    var enteredRecoveryCode by remember { mutableStateOf("") }
    var newPasswordSecret by remember { mutableStateOf("") }
    var confirmPasswordSecret by remember { mutableStateOf("") }

    var feedbackText by remember { mutableStateOf("") }
    
    val userList by viewModel.users.collectAsState(initial = emptyList())
    val isSuperAdmin = userLoginId.lowercase() == "admin" || userList.any { it.username == userLoginId && it.role == "Super Admin" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("back_from_forgot")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = Localization.translate("forgot_title", lang),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = Localization.translate("recovery_desc", lang),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            if (step == 1) {
                OutlinedTextField(
                    value = userLoginId,
                    onValueChange = { userLoginId = it },
                    label = { Text(Localization.translate("username", lang)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = userMobile,
                    onValueChange = { userMobile = it },
                    label = { Text(Localization.translate("reg_mobile", lang)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = userEmail,
                    onValueChange = { userEmail = it },
                    label = { Text(Localization.translate("reg_email", lang)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(12.dp)
                )

                if (feedbackText.isNotEmpty()) {
                    Text(
                        text = feedbackText,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (userLoginId.isBlank() || userMobile.isBlank() || userEmail.isBlank()) {
                            feedbackText = "All fields are required"
                        } else {
                            viewModel.runForgotPasswordVerification(userLoginId, userMobile, userEmail) { success, err ->
                                if (success) {
                                    step = 2
                                    feedbackText = ""
                                } else {
                                    feedbackText = if (err == "username_mismatch") "Username doesn't exist" else "Details do not match registered factors"
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(Localization.translate("send_codes", lang))
                }
            } else {
                // Step 2: Verification and reset
                val alertText = if (isSuperAdmin) {
                    "Simulated Factors (OTP: 5544, Code: SEC-889, Key: check setup wizard)"
                } else {
                    "Simulated Factors (OTP: 5544, Code: SEC-889)"
                }
                Text(
                    alertText,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = mobileOtp,
                    onValueChange = { mobileOtp = it },
                    label = { Text(Localization.translate("enter_otp_mobile", lang)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = emailCode,
                    onValueChange = { emailCode = it },
                    label = { Text(Localization.translate("enter_vcode_email", lang)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(12.dp)
                )

                if (isSuperAdmin) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = enteredRecoveryCode,
                        onValueChange = { enteredRecoveryCode = it },
                        label = { Text("Backup Recovery Code (Required for Super Admin)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(0.9f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = newPasswordSecret,
                    onValueChange = { newPasswordSecret = it },
                    label = { Text(Localization.translate("new_pwd", lang)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPasswordSecret,
                    onValueChange = { confirmPasswordSecret = it },
                    label = { Text(Localization.translate("confirm_pwd", lang)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(12.dp)
                )

                // Password Policy Strength Meter
                if (isSuperAdmin) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.9f).padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Super Admin Password Policy Checker (Rule 64):", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            val isStrong = com.example.ui.SecurityUtils.validateStrongPassword(newPasswordSecret)
                            val checks = listOf(
                                Pair("Minimum 12 Characters", newPasswordSecret.length >= 12),
                                Pair("Uppercase [A-Z]", newPasswordSecret.any { it.isUpperCase() }),
                                Pair("Lowercase [a-z]", newPasswordSecret.any { it.isLowerCase() }),
                                Pair("Number [0-9]", newPasswordSecret.any { it.isDigit() }),
                                Pair("Special Char", newPasswordSecret.any { !it.isLetterOrDigit() })
                            )
                            checks.forEach { (text, holds) ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (holds) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = "holds",
                                        tint = if (holds) Color(0xFF67C23A) else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                } else {
                    val strengthScore = calculatePasswordStrength(newPasswordSecret)
                    PasswordStrengthIndicator(strengthScore, lang)
                }

                if (feedbackText.isNotEmpty()) {
                    Text(
                        text = feedbackText,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val correctRegCode = viewModel.getStoredRecoveryCode(userLoginId)
                        val holdsPasswordCheck = if (isSuperAdmin) {
                            com.example.ui.SecurityUtils.validateStrongPassword(newPasswordSecret)
                        } else {
                            calculatePasswordStrength(newPasswordSecret) >= 3
                        }

                        if (mobileOtp != "5544" || emailCode != "SEC-889") {
                            feedbackText = "Incorrect verification factors"
                        } else if (isSuperAdmin && enteredRecoveryCode.uppercase().trim() != correctRegCode.uppercase().trim() && correctRegCode.isNotEmpty()) {
                            feedbackText = "Incorrect Backup Recovery Code"
                        } else if (newPasswordSecret.isBlank() || newPasswordSecret != confirmPasswordSecret) {
                            feedbackText = Localization.translate("pwd_mismatch", lang)
                        } else if (!holdsPasswordCheck) {
                            feedbackText = "Weak password fails policy requirements!"
                        } else {
                            viewModel.performResetAfterVerification(userLoginId, newPasswordSecret)
                            viewModel.logConsole("Account recovered successfully for user '$userLoginId'. All alternate session device configurations invalidated.")
                            onBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(Localization.translate("verify_allow_reset", lang))
                }
            }
        }
    }
}

// --- MANDATED PASSWORD CHANGE ON FIRST LOGIN ---
@Composable
fun ForcePasswordResetScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val user = viewModel.currentUser ?: return
    val isSuperAdmin = user.role == "Super Admin"

    if (isSuperAdmin) {
        SuperAdminSetupWizardScreen(viewModel, user)
    } else {
        NormalUserPasswordResetScreen(viewModel, user)
    }
}

@Composable
fun SuperAdminSetupWizardScreen(viewModel: AppViewModel, user: UserEntity) {
    var step by remember { mutableStateOf(1) } // 1: Mobile OTP, 2: Email, 3: Password change, 4: Finished
    
    // Step 1: Mobile
    var mobileNo by remember { mutableStateOf(user.mobileNumber.ifBlank { "+8801700000001" }) }
    var userOtp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    val simulatedOtp = "4591"
    
    // Step 2: Email
    var emailAdd by remember { mutableStateOf(user.email.ifBlank { "admin@fishfarm.com" }) }
    var userEmailCode by remember { mutableStateOf("") }
    var isEmailSent by remember { mutableStateOf(false) }
    val simulatedEmailCode = "ADM-7822"
    
    // Step 3: Password change
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    // Device & Session Info as per rule 93
    val manufacturer = android.os.Build.MANUFACTURER ?: "Google"
    val model = android.os.Build.MODEL ?: "Android Studio Emulator"
    val deviceInfo = "$manufacturer $model"
    val ipAddress = "192.168.1.104"
    val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
    
    var feedbackText by remember { mutableStateOf("") }
    
    // Step 3 password strength indicators for Super Admin (Rule 89)
    val holdsMinLength = newPassword.length >= 5
    val holdsUpper = newPassword.any { it.isUpperCase() }
    val holdsLower = newPassword.any { it.isLowerCase() }
    val holdsDigit = newPassword.any { it.isDigit() }
    val holdsSpecial = newPassword.any { !it.isLetterOrDigit() }
    val isDefaultDeactivated = newPassword != "11"
    val isPasswordStrong = holdsMinLength && holdsUpper && holdsLower && holdsDigit && holdsSpecial && isDefaultDeactivated

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Branded header with shield
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = "Shield",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Super Admin Account Setup Wizard",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Step Progress Indicator
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Mobile OTP", "Email PIN", "Hardened Pass", "Complete").forEachIndexed { index, title ->
                    val stepNum = index + 1
                    val isCurrent = stepNum == step
                    val isPast = stepNum < step
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (isCurrent) MaterialTheme.colorScheme.primary
                                    else if (isPast) MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
        
        // Render step by step
        if (step == 1) {
            item {
                Text(
                    text = "Step-1: Mobile Number Verification (OTP)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Verification is mandatory to secure the admin portal before accessing the dashboard (Rule 88).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
            }
            
            item {
                OutlinedTextField(
                    value = mobileNo,
                    onValueChange = { 
                        mobileNo = it
                        feedbackText = "" 
                    },
                    label = { Text("Mobile Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.9f).testTag("wizard_mobile_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            item {
                Button(
                    onClick = {
                        isOtpSent = true
                        feedbackText = ""
                        viewModel.logConsole("MANDATORY MOBILE OTP SENT: Generated OTP Code '$simulatedOtp' for mobile '$mobileNo'.")
                    },
                    modifier = Modifier.fillMaxWidth(0.9f).height(48.dp).testTag("wizard_send_otp_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isOtpSent) "Resend SMS OTP" else "Send SMS OTP Code")
                }
            }
            
            if (isOtpSent) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "📱 Simulated mobile SMS received:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "Code: $simulatedOtp",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                item {
                    OutlinedTextField(
                        value = userOtp,
                        onValueChange = { 
                            userOtp = it
                            feedbackText = ""
                        },
                        label = { Text("Enter 4-Digit OTP") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(0.9f).testTag("wizard_otp_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                item {
                    Button(
                        onClick = {
                            if (userOtp == simulatedOtp) {
                                feedbackText = ""
                                step = 2
                                viewModel.logConsole("VERIFY SUCCESS: Mobile status verified successfully (OTP matched).")
                            } else {
                                feedbackText = "Incorrect mobile OTP code. Please enter $simulatedOtp"
                                viewModel.logConsole("VERIFY FAIL: Mobile verify attempt fail. Incorrect code entered.")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.9f).height(50.dp).testTag("wizard_verify_otp_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Verify & Continue to Step-2")
                    }
                }
            }
        } else if (step == 2) {
            item {
                Text(
                    text = "Step-2: Email Address Verification",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Verification is mandatory to ensure account restoration channels function correctly (Rule 88).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
            }
            
            item {
                OutlinedTextField(
                    value = emailAdd,
                    onValueChange = { 
                        emailAdd = it
                        feedbackText = ""
                    },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.9f).testTag("wizard_email_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            item {
                Button(
                    onClick = {
                        isEmailSent = true
                        feedbackText = ""
                        viewModel.logConsole("MANDATORY EMAIL CODE SENT: Generated email verify PIN '$simulatedEmailCode' for email '$emailAdd'.")
                    },
                    modifier = Modifier.fillMaxWidth(0.9f).height(48.dp).testTag("wizard_send_email_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isEmailSent) "Resend Email Code" else "Send Email Code")
                }
            }
            
            if (isEmailSent) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "✉️ Simulated email notification received:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "PIN: $simulatedEmailCode",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                item {
                    OutlinedTextField(
                        value = userEmailCode,
                        onValueChange = { 
                            userEmailCode = it
                            feedbackText = ""
                        },
                        label = { Text("Enter Email PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(0.9f).testTag("wizard_email_code_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                item {
                    Button(
                        onClick = {
                            if (userEmailCode == simulatedEmailCode) {
                                feedbackText = ""
                                step = 3
                                viewModel.logConsole("VERIFY SUCCESS: Email status verified successfully.")
                            } else {
                                feedbackText = "Incorrect email PIN. Please enter $simulatedEmailCode"
                                viewModel.logConsole("VERIFY FAIL: Email code mismatch.")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.9f).height(50.dp).testTag("wizard_verify_email_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Verify & Continue to Step-3")
                    }
                }
            }
        } else if (step == 3) {
            item {
                Text(
                    text = "Step-3: Force Password Change",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Rule 89: Initialize a hardened security password. Default key '11' must be deactivated permanently for security reasons.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
            }
            
            item {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { 
                        newPassword = it
                        feedbackText = ""
                    },
                    label = { Text("New Hardened Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock") },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.9f).testTag("wizard_new_pass_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            item {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        feedbackText = ""
                    },
                    label = { Text("Confirm Hardened Password") },
                    leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = "Confirm Lock") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.9f).testTag("wizard_confirm_pass_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Mandatory Password Change Policy Checklists (Rule 89):",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        
                        val checkPass = listOf(
                            Pair("Minimum 5 Characters", holdsMinLength),
                            Pair("Require Uppercase Letter [A-Z]", holdsUpper),
                            Pair("Require Lowercase Letter [a-z]", holdsLower),
                            Pair("Require Numeric Digit [0-9]", holdsDigit),
                            Pair("Require Special Character [e.g. @#$%^&*!_]", holdsSpecial),
                            Pair("Deactivation of '11' (Cannot reuse)", isDefaultDeactivated)
                        )
                        
                        checkPass.forEach { (title, holds) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (holds) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = "Status",
                                    tint = if (holds) Color(0xFF67C23A) else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (holds) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                Button(
                    onClick = {
                        if (newPassword.isBlank() || confirmPassword.isBlank()) {
                            feedbackText = "Password fields cannot be blank"
                        } else if (newPassword != confirmPassword) {
                            feedbackText = "Passwords do not match."
                        } else if (!isPasswordStrong) {
                            feedbackText = "Passcode does not fulfill policy checks or attempts to use deactivated '11'!"
                        } else {
                            feedbackText = ""
                            step = 4
                            viewModel.logConsole("PASSWORD VALIDATED: New secure password meets all policy targets. Default '11' disabled.")
                        }
                    },
                    enabled = isPasswordStrong,
                    modifier = Modifier.fillMaxWidth(0.9f).height(50.dp).testTag("wizard_submit_pass_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply & Deactivate Default '11'")
                }
            }
        } else {
            // STEP 4: Security setup completed
            item {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE1F5FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF0288D1),
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Security Setup Complete!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0288D1),
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "All security factors have been verified successfully. Your super admin account is now hardened.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "🛠️ Diagnostic Logging Trail (Rule 93):",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        HorizontalDivider()
                        Text("• Device Info: $deviceInfo", style = MaterialTheme.typography.bodySmall)
                        Text("• IP Routing Address: $ipAddress", style = MaterialTheme.typography.bodySmall)
                        Text("• Security Timestamp: $timestamp", style = MaterialTheme.typography.bodySmall)
                        Text("• Mobile Verified: YES ($mobileNo)", style = MaterialTheme.typography.bodySmall)
                        Text("• Email Verified: YES ($emailAdd)", style = MaterialTheme.typography.bodySmall)
                        Text("• Default Pass de-activated: YES", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            item {
                Button(
                    onClick = {
                        viewModel.completeSuperAdminFirstLoginWizard(
                            newPass = newPassword,
                            email = emailAdd,
                            mobile = mobileNo,
                            deviceInfo = deviceInfo,
                            ipAddress = ipAddress
                        )
                    },
                    modifier = Modifier.fillMaxWidth(0.9f).height(52.dp).testTag("wizard_finalize_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1), contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Finalize & Enter Dashboard Access")
                }
            }
        }
        
        if (feedbackText.isNotEmpty()) {
            item {
                Text(
                    text = feedbackText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun NormalUserPasswordResetScreen(viewModel: AppViewModel, user: UserEntity) {
    val lang = viewModel.currentLanguage
    
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var recoveryEmail by remember { mutableStateOf(user.email) }
    var recoveryMobile by remember { mutableStateOf(user.mobileNumber) }
    var is2FaEnabled by remember { mutableStateOf(false) }
    
    var confirmEmailCheck by remember { mutableStateOf(false) }
    var confirmMobileCheck by remember { mutableStateOf(false) }
    
    var feedbackText by remember { mutableStateOf("") }

    val isPasswordStrong = newPassword.length >= 6

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Icon(
                Icons.Default.Security,
                contentDescription = "Shield",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "First Login Security Setup",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "First login detected. To activate your session, you must initialize your password and configure emergency settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
            )
        }

        item {
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Strong Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .testTag("force_new_pwd_input"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm New Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .testTag("force_confirm_pwd_input"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            val strength = calculatePasswordStrength(newPassword)
            PasswordStrengthIndicator(strength, lang)
        }

        item {
            HorizontalDivider(modifier = Modifier.fillMaxWidth(0.9f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Secondary Verification Recoverables (Rule 66)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        item {
            OutlinedTextField(
                value = recoveryEmail,
                onValueChange = { recoveryEmail = it },
                label = { Text("Confirm Recovery Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = RoundedCornerShape(12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = confirmEmailCheck, onCheckedChange = { confirmEmailCheck = it ?: false })
                Text("Verify & Confirm Recovery Email", style = MaterialTheme.typography.bodySmall)
            }
        }

        item {
            OutlinedTextField(
                value = recoveryMobile,
                onValueChange = { recoveryMobile = it },
                label = { Text("Confirm Recovery Mobile") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = RoundedCornerShape(12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = confirmMobileCheck, onCheckedChange = { confirmMobileCheck = it ?: false })
                Text("Verify & Confirm Recovery Mobile", style = MaterialTheme.typography.bodySmall)
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Simulate 2FA (Two-Factor)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Require email + custom SMS OTP verification checks on future logins.", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = is2FaEnabled, onCheckedChange = { is2FaEnabled = it })
                }
            }
        }

        if (feedbackText.isNotEmpty()) {
            item {
                Text(
                    text = feedbackText,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }

        item {
            Button(
                onClick = {
                    if (newPassword.isBlank() || newPassword != confirmPassword) {
                        feedbackText = "Passwords do not match / cannot be blank"
                    } else if (!isPasswordStrong) {
                        feedbackText = "Password is too weak. Does not conform to the enterprise security rules."
                    } else if (recoveryEmail.isBlank() || recoveryMobile.isBlank()) {
                        feedbackText = "Recovery items cannot be empty."
                    } else {
                        viewModel.completeForcePasswordReset(newPassword)
                        viewModel.completeSecuritySetup(user.username, recoveryEmail, recoveryMobile, is2FaEnabled)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(52.dp)
                    .testTag("force_pwd_save_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirm Security Factors & Activate Session")
            }
        }
    }
}

// --- PASSWORD STRENGTH BALANCER ---
@Composable
fun PasswordStrengthIndicator(score: Int, lang: Localization.Language) {
    val (label, color) = when (score) {
        0, 1 -> Pair("Weak (দুর্বল)", MaterialTheme.colorScheme.error)
        2 -> Pair("Moderate (মাঝারি)", Color(0xFFE6A23C))
        3 -> Pair("Strong (শক্তিশালী)", Color(0xFF67C23A))
        else -> Pair("Excel (অসাধারণ)", MaterialTheme.colorScheme.primary)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(top = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = Localization.translate("pwd_strength", lang),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 1..4) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (i <= score) color else MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
    }
}

fun calculatePasswordStrength(pass: String): Int {
    if (pass.length < 6) return 1
    var score = 0
    if (pass.length >= 8) score++
    if (pass.any { it.isUpperCase() }) score++
    if (pass.any { it.isLowerCase() }) score++
    if (pass.any { it.isDigit() }) score++
    if (pass.any { !it.isLetterOrDigit() }) score++
    return score.coerceAtMost(4)
}

// --- PORTAL LAYOUTS (DRAWER + ADAPTIVE SCREEN NAVIGATION) ---
@Composable
fun AuthenticatedPortal(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val user = viewModel.currentUser ?: return
    val selectedProject = viewModel.currentProject
    
    var activeTab by remember { mutableStateOf("dashboard") }
    
    // Adaptive Layout Rules: Detect orientation and window widths
    val config = LocalConfiguration.current
    val isWideScreen = config.screenWidthDp >= 600

    Scaffold(
        bottomBar = {
            if (!isWideScreen) {
                // Circular adaptive modern bottom bars for Mobile Viewports
                AppBottomBar(lang, activeTab) { activeTab = it }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isWideScreen) {
                // Adaptive Navigation Rail for Expanded/Tablet screen configurations
                AppNavigationRail(lang, activeTab) { activeTab = it }
            }

            // Main body panels
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    }
                ) { tab ->
                    when (tab) {
                        "dashboard" -> DashboardScreen(viewModel)
                        "accounting" -> AccountingScreen(viewModel)
                        "members" -> MemberAndSharesScreen(viewModel)
                        "monitoring" -> PondsMonitoringScreen(viewModel)
                        "approvals" -> ApprovalsStagingScreen(viewModel)
                        "messages" -> InternalMessagingScreen(viewModel)
                        "profile" -> ProfileSettingsScreen(viewModel)
                    }
                }
            }
        }
    }
}

// Adaptive Bottom Bars
@Composable
fun AppBottomBar(lang: Localization.Language, active: String, onSelection: (String) -> Unit) {
    NavigationBar(
        tonalElevation = 6.dp,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        NavigationBarItem(
            selected = active == "dashboard",
            onClick = { onSelection("dashboard") },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text(Localization.translate("dashboard", lang), maxLines = 1, overflow = TextOverflow.Ellipsis) }
        )
        NavigationBarItem(
            selected = active == "accounting",
            onClick = { onSelection("accounting") },
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Accounting") },
            label = { Text(Localization.translate("accounting", lang), maxLines = 1, overflow = TextOverflow.Ellipsis) }
        )
        NavigationBarItem(
            selected = active == "members",
            onClick = { onSelection("members") },
            icon = { Icon(Icons.Default.People, contentDescription = "Members") },
            label = { Text(Localization.translate("members", lang), maxLines = 1, overflow = TextOverflow.Ellipsis) }
        )
        NavigationBarItem(
            selected = active == "monitoring",
            onClick = { onSelection("monitoring") },
            icon = { Icon(Icons.Default.Water, contentDescription = "Monitoring") },
            label = { Text(Localization.translate("monitoring", lang), maxLines = 1, overflow = TextOverflow.Ellipsis) }
        )
        NavigationBarItem(
            selected = active == "approvals",
            onClick = { onSelection("approvals") },
            icon = { Icon(Icons.Default.Rule, contentDescription = "Approvals") },
            label = { Text(Localization.translate("approvals", lang), maxLines = 1, overflow = TextOverflow.Ellipsis) }
        )
        NavigationBarItem(
            selected = active == "messages",
            onClick = { onSelection("messages") },
            icon = { Icon(Icons.Default.Email, contentDescription = "Messages") },
            label = { Text(Localization.translate("messages", lang), maxLines = 1, overflow = TextOverflow.Ellipsis) }
        )
        NavigationBarItem(
            selected = active == "profile",
            onClick = { onSelection("profile") },
            icon = { Icon(Icons.Default.ManageAccounts, contentDescription = "Profile") },
            label = { Text(Localization.translate("profile", lang), maxLines = 1, overflow = TextOverflow.Ellipsis) }
        )
    }
}

// Adaptive Side Rail
@Composable
fun AppNavigationRail(lang: Localization.Language, active: String, onSelection: (String) -> Unit) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        header = {
            Icon(
                Icons.Default.Waves,
                contentDescription = "Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp).padding(bottom = 16.dp)
            )
        }
    ) {
        NavigationRailItem(
            selected = active == "dashboard",
            onClick = { onSelection("dashboard") },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text(Localization.translate("dashboard", lang)) }
        )
        NavigationRailItem(
            selected = active == "accounting",
            onClick = { onSelection("accounting") },
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Accounting") },
            label = { Text(Localization.translate("accounting", lang)) }
        )
        NavigationRailItem(
            selected = active == "members",
            onClick = { onSelection("members") },
            icon = { Icon(Icons.Default.People, contentDescription = "Members") },
            label = { Text(Localization.translate("members", lang)) }
        )
        NavigationRailItem(
            selected = active == "monitoring",
            onClick = { onSelection("monitoring") },
            icon = { Icon(Icons.Default.Water, contentDescription = "Monitoring") },
            label = { Text(Localization.translate("monitoring", lang)) }
        )
        NavigationRailItem(
            selected = active == "approvals",
            onClick = { onSelection("approvals") },
            icon = { Icon(Icons.Default.Rule, contentDescription = "Approvals") },
            label = { Text(Localization.translate("approvals", lang)) }
        )
        NavigationRailItem(
            selected = active == "messages",
            onClick = { onSelection("messages") },
            icon = { Icon(Icons.Default.Email, contentDescription = "Messages") },
            label = { Text(Localization.translate("messages", lang)) }
        )
        NavigationRailItem(
            selected = active == "profile",
            onClick = { onSelection("profile") },
            icon = { Icon(Icons.Default.ManageAccounts, contentDescription = "Profile") },
            label = { Text(Localization.translate("profile", lang)) }
        )
    }
}

// --- SCREEN 1: REALTIME FARM DASHBOARD ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val user = viewModel.currentUser ?: return
    val selectedProj = viewModel.currentProject
    val projectsList by viewModel.projects.collectAsState()

    val incomeList by viewModel.currentIncome.collectAsState()
    val expenseList by viewModel.currentExpenses.collectAsState()
    val requestsList by viewModel.currentRequests.collectAsState()
    val membersList by viewModel.currentMembers.collectAsState()
    val pondsList by viewModel.currentPonds.collectAsState()

    var showProjectCreatorModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Title & Selector Row
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = Localization.translate("active_project", lang),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = selectedProj?.name ?: Localization.translate("no_project", lang),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Single source switch language in authenticated state
                    FilledTonalIconButton(
                        onClick = {
                            viewModel.currentLanguage =
                                if (lang == Localization.Language.EN) Localization.Language.BN else Localization.Language.EN
                        }
                    ) {
                        Icon(Icons.Default.Language, contentDescription = "Lang")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Project drop-downs or selector grids
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    projectsList.forEach { proj ->
                        FilterChip(
                            selected = selectedProj?.id == proj.id,
                            onClick = { viewModel.selectProject(proj) },
                            label = { Text(proj.name) },
                            leadingIcon = if (selectedProj?.id == proj.id) {
                                { Icon(Icons.Default.Check, contentDescription = "Selected", modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }

                    if (user.role == "Super Admin") {
                        // Project creation trigger for super-admin nodes
                        ElevatedButton(
                            onClick = { showProjectCreatorModal = true },
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Localization.translate("create_project", lang), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Project activation alert if deactivated
        if (selectedProj != null && !selectedProj.isActive) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = "Warn", tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "This farm project is suspended by the super administrator. Live postings locked.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        if (selectedProj != null) {
            // Calculated balances
            val netIncome = incomeList.sumOf { it.amount }
            val netExpense = expenseList.sumOf { it.amount }
            val currentBalance = netIncome - netExpense
            val totalPendingCount = requestsList.count { it.status == "Pending" }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Geometric Balance: Total Balance Card (bg: primaryContainer, #EADDFF)
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(132.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = Localization.translate("total_balance", lang),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Column {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = Localization.translate("currency_symbol", lang),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = String.format("%,.0f", currentBalance),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Text(
                                    text = "+12% from last month",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Geometric Balance: Net Profit / Flow Card (bg: secondaryContainer, #D0BCFF)
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(132.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = Localization.translate("total_income", lang),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Column {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = Localization.translate("currency_symbol", lang),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = String.format("%,.0f", netIncome),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Text(
                                    text = "Expenses: -${String.format("%,.0f", netExpense)} $",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            // Quadrant counts representation
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(Localization.translate("member_count", lang), style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PeopleOutline, contentDescription = "People", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "${membersList.size}",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(Localization.translate("pending_req", lang), style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.HourglassEmpty,
                                    contentDescription = "Pending",
                                    tint = if (totalPendingCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "$totalPendingCount",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (totalPendingCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Dynamic Custom Drawing Canvas trend graphics!
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            Localization.translate("financial_flow", lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Drawing a full working financial flow Canvas representation
                        FinancialFlowChart(incomeList, expenseList)

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceAround,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Green: Income Accumulation", fontSize = 10.sp, color = Color(0xFF4CAF50))
                            Text("Red: Expense Outflow", fontSize = 10.sp, color = Color(0xFFF44336))
                        }
                    }
                }
            }

            // Pond coordinates status
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = Localization.translate("fish_summary", lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (pondsList.isEmpty()) {
                            Text(Localization.translate("no_data", lang), style = MaterialTheme.typography.bodySmall)
                        } else {
                            pondsList.forEach { pond ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(pond.pondNumber, fontWeight = FontWeight.SemiBold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(
                                                    color = when (pond.waterCondition) {
                                                        "Good" -> Color(0xFF4CAF50)
                                                        "Warning" -> Color(0xFFFF9800)
                                                        else -> Color(0xFFF44336)
                                                    },
                                                    shape = CircleShape
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(pond.waterCondition, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Global view empty state
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Please select or add a Project Farm to inspect analytical tools.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Project Settings management tools if Super Admin
        if (selectedProj != null && user.role == "Super Admin") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Governance Operations (Super Admin Exclusive)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.updateProjectActiveState(selectedProj, !selectedProj.isActive) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedProj.isActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    if (selectedProj.isActive) Localization.translate("deactivate", lang)
                                    else Localization.translate("activate", lang)
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    // Trigger changing Majority percents dynamically
                                    viewModel.registerNewProject(selectedProj.name, if (selectedProj.majorityApprovalPercent == 50) 66 else 50)
                                }
                            ) {
                                Text("Toggle Rules (${selectedProj.majorityApprovalPercent}%)")
                            }
                        }
                    }
                }
            }
        }

        // Active notification feed log block
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Live Notification Logs",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val notificationsList by viewModel.notifications.collectAsState()
                    if (notificationsList.isEmpty()) {
                        Text("No active system broadcasts.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        notificationsList.take(3).forEach { notif ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = if (lang == Localization.Language.EN) notif.titleEn else notif.titleBn,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = if (lang == Localization.Language.EN) notif.messageEn else notif.messageBn,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Divider(modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet representation to Register Farms / Projects
    if (showProjectCreatorModal) {
        var pName by remember { mutableStateOf("") }
        var mPercent by remember { mutableStateOf("51") }

        AlertDialog(
            onDismissRequest = { showProjectCreatorModal = false },
            title = { Text(Localization.translate("create_project", lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = pName,
                        onValueChange = { pName = it },
                        label = { Text(Localization.translate("project_name", lang)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = mPercent,
                        onValueChange = { mPercent = it },
                        label = { Text(Localization.translate("majority_approval_req", lang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pName.isNotBlank()) {
                            viewModel.registerNewProject(
                                name = pName,
                                majorityApprovalPercent = mPercent.toIntOrNull() ?: 51
                            )
                            showProjectCreatorModal = false
                        }
                    }
                ) {
                    Text(Localization.translate("apply", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showProjectCreatorModal = false }) {
                    Text(Localization.translate("clear", lang))
                }
            }
        )
    }
}

// Custom canvas trends plotter
@Composable
fun FinancialFlowChart(incomes: List<IncomeEntity>, expenses: List<ExpenseEntity>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(top = 8.dp)
    ) {
        val width = size.width
        val height = size.height

        // Background grids lines
        val lineCount = 5
        val spacing = height / lineCount
        for (i in 0 until lineCount) {
            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(0f, i * spacing),
                end = Offset(width, i * spacing),
                strokeWidth = 1f
            )
        }

        // Graph plots
        if (incomes.isNotEmpty() || expenses.isNotEmpty()) {
            val maxVal = ((incomes.map { it.amount }.maxOrNull() ?: 0.0)
                .coerceAtLeast(expenses.map { it.amount }.maxOrNull() ?: 0.0)
                .coerceAtLeast(1000.0)) * 1.2

            // Draw line for income updates
            if (incomes.size > 1) {
                val incPoints = incomes.reversed().take(10)
                val segment = width / (incPoints.size - 1).coerceAtLeast(1)
                val path = Path()

                incPoints.forEachIndexed { idx, item ->
                    val x = idx * segment
                    val y = (height - (item.amount / maxVal * height)).toFloat()
                    if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path = path, color = Color(0xFF4CAF50), style = Stroke(width = 4f))
            }

            // Draw line for expenses
            if (expenses.size > 1) {
                val expPoints = expenses.reversed().take(10)
                val segment = width / (expPoints.size - 1).coerceAtLeast(1)
                val path = Path()

                expPoints.forEachIndexed { idx, item ->
                    val x = idx * segment
                    val y = (height - (item.amount / maxVal * height)).toFloat()
                    if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path = path, color = Color(0xFFF44336), style = Stroke(width = 4f))
            } else if (expenses.size == 1) {
                // simple dot point representation
                drawCircle(
                    color = Color(0xFFF44336),
                    radius = 8f,
                    center = Offset(width / 2, (height - (expenses[0].amount / maxVal * height)).toFloat())
                )
            }
        }
    }
}

// --- SCREEN 2: PROFESSIONAL ACCOUNTING MODULE ---
@Composable
fun AccountingScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val proj = viewModel.currentProject
    
    val incomeList by viewModel.currentIncome.collectAsState()
    val expenseList by viewModel.currentExpenses.collectAsState()

    var showIncomeForm by remember { mutableStateOf(false) }
    var showExpenseForm by remember { mutableStateOf(false) }
    var showDividendCalculator by remember { mutableStateOf(false) }

    if (proj == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Please select an active project from the dashboard to activate accounting desks.",
                textAlign = TextAlign.Center
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${proj.name} - Ledger Desk",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                // Tool selection
                IconButton(onClick = { showDividendCalculator = !showDividendCalculator }) {
                    Icon(Icons.Default.Calculate, contentDescription = "Dividend Solver", tint = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        // Live calculation metrics bar
        val totalInc = incomeList.sumOf { it.amount }
        val totalExp = expenseList.sumOf { it.amount }
        val netPoolVal = totalInc - totalExp

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Gross Incomes", style = MaterialTheme.typography.labelSmall)
                        Text("${String.format("%,.0f", totalInc)} $")
                    }
                    Divider(modifier = Modifier.width(1.dp).height(30.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Approved Expenses", style = MaterialTheme.typography.labelSmall)
                        Text("${String.format("%,.0f", totalExp)} $")
                    }
                    Divider(modifier = Modifier.width(1.dp).height(30.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Allocated Pool", style = MaterialTheme.typography.labelSmall)
                        Text("${String.format("%,.0f", netPoolVal)} $")
                    }
                }
            }
        }

        // Quick Posting Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (proj.isActive) {
                    Button(
                        onClick = { showIncomeForm = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Localization.translate("add_income", lang), fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showExpenseForm = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(Icons.Default.HourglassBottom, contentDescription = "Expense")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Localization.translate("add_expense", lang), fontSize = 12.sp)
                    }
                }
            }
        }

        // Dividend distributions tool panel
        if (showDividendCalculator) {
            item {
                DividendCalculatorTab(viewModel, netPoolVal, lang)
            }
        }

        // Income log listings
        item {
            Text(
                text = "Recorded Farm Cash Flows",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Standard Incomes Entry", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (incomeList.isEmpty()) {
                        Text("No income records found.", fontSize = 11.sp)
                    } else {
                        incomeList.forEach { inc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(inc.category, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(inc.description, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                }
                                Text("+ ${String.format("%,.0f", inc.amount)}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                            }
                            Divider()
                        }
                    }
                }
            }
        }

        // Approved Expense elements listing
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Approved Operations Expenses", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (expenseList.isEmpty()) {
                        Text("No expense statements registered.", fontSize = 11.sp)
                    } else {
                        expenseList.forEach { exp ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(exp.category, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(exp.description, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                }
                                Text("- ${String.format("%,.0f", exp.amount)}", color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
                            }
                            Divider()
                        }
                    }
                }
            }
        }
    }

    if (showIncomeForm) {
        var category by remember { mutableStateOf("Fish Sale") }
        var amount by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showIncomeForm = false },
            title = { Text(Localization.translate("add_income", lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Category dropdown switcher
                    val categories = listOf("Fish Sale", "Fry Sale", "Egg Sale", "Service Income", "Investment", "Other Income")
                    var expanded by remember { mutableStateOf(false) }

                    Box {
                        OutlinedButton(onClick = { expanded = true }) {
                            Text("Category: $category")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text(Localization.translate("amount", lang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text(Localization.translate("desc", lang)) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amount.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            viewModel.submitIncome(category, amt, desc, System.currentTimeMillis())
                            showIncomeForm = false
                        }
                    }
                ) {
                    Text(Localization.translate("apply", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showIncomeForm = false }) {
                    Text(Localization.translate("clear", lang))
                }
            }
        )
    }

    if (showExpenseForm) {
        var category by remember { mutableStateOf("Feed") }
        var amount by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showExpenseForm = false },
            title = { Text("Request Expense Approval") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val categories = listOf("Feed", "Medicine", "Labor Salary", "Electricity", "Transport", "Pond Maintenance", "Equipment", "Water Treatment", "Miscellaneous")
                    var expanded by remember { mutableStateOf(false) }

                    Box {
                        OutlinedButton(onClick = { expanded = true }) {
                            Text("Category: $category")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text(Localization.translate("amount", lang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text(Localization.translate("desc", lang)) }
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Expenses >= 15,000 $ require 100% shareholder consents.\nBelow requires Majority.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amount.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            viewModel.submitExpenseRequest(category, amt, desc, System.currentTimeMillis())
                            showExpenseForm = false
                        }
                    }
                ) {
                    Text(Localization.translate("apply", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExpenseForm = false }) {
                    Text(Localization.translate("clear", lang))
                }
            }
        )
    }
}

// DIVIDEND SOLVER PAGE
@Composable
fun DividendCalculatorTab(viewModel: AppViewModel, availablePool: Double, lang: Localization.Language) {
    var poolAmountInput by remember { mutableStateOf("") }
    var periodType by remember { mutableStateOf("Monthly") }
    var calculatedPayouts by remember { mutableStateOf<List<Triple<String, Double, Double>>>(emptyList()) } // Pair Name, holding%, payout_amount

    val sharesList by viewModel.currentShares.collectAsState()
    val membersList by viewModel.currentMembers.collectAsState()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Calculate, contentDescription = "Calc", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    Localization.translate("profit_dist", lang),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text("Calculates distribution shares dynamically based on shareholder equity ratios.")

            OutlinedTextField(
                value = poolAmountInput,
                onValueChange = { poolAmountInput = it },
                label = { Text(Localization.translate("dist_amount", lang)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Period Selection Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Monthly", "Quarterly", "Yearly").forEach { p ->
                    ElevatedFilterChip(
                        selected = periodType == p,
                        onClick = { periodType = p },
                        label = { Text(p) }
                    )
                }
            }

            Button(
                onClick = {
                    val requestedPool = poolAmountInput.toDoubleOrNull() ?: 0.0
                    val totalSharesSum = sharesList.sumOf { it.shareCount }
                    if (totalSharesSum > 0 && requestedPool > 0) {
                        calculatedPayouts = sharesList.map { sh ->
                            val member = membersList.find { it.id == sh.memberId }
                            val mName = member?.fullName ?: "ID: ${sh.memberId}"
                            val holdingPercent = sh.shareCount / totalSharesSum
                            val payment = requestedPool * holdingPercent
                            Triple(mName, holdingPercent * 100, payment)
                        }
                        viewModel.logConsole("DIVIDEND CALCULATED: Partitioned $requestedPool BDT under period interval '$periodType'.")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(Localization.translate("distribute", lang))
            }

            if (calculatedPayouts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    Localization.translate("dividend_payout_table", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                calculatedPayouts.forEach { payout ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(payout.first, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row {
                            Text("${String.format("%.1f", payout.second)} %", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("${String.format("%,.0f", payout.third)} ৳", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Divider()
                }

                // Simulate Exporting capability
                TextButton(
                    onClick = {
                        viewModel.logConsole("PDF EXPORT SUCCESS: Dividend statement generated representing period '$periodType' with farm logo stamp.")
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Print, contentDescription = "Print")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Branded Print Export")
                }
            }
        }
    }
}

// --- SCREEN 3: MEMBER DEMOGRAPHICS & SHARE TRANSFERS ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemberAndSharesScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val proj = viewModel.currentProject
    val user = viewModel.currentUser ?: return

    val membersList by viewModel.currentMembers.collectAsState()
    val sharesList by viewModel.currentShares.collectAsState()
    val historyList by viewModel.currentShareHistory.collectAsState()

    var showOnboardModal by remember { mutableStateOf(false) }
    var showTransferModal by remember { mutableStateOf(false) }
    var showExitModal by remember { mutableStateOf(false) }

    if (proj == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Select an active farm database from dashboard to launch capital structures charts.", textAlign = TextAlign.Center)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "${proj.name} - ${Localization.translate("sh_capital", lang)}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Active Share Ratios Canvas Pie visual representation
        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ownership Percentage Distribution Pie", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(12.dp))

                    ShareholderDistributionPie(sharesList, membersList)
                }
            }
        }

        // Action Trigger rows if active
        if (proj.isActive && (user.role == "Super Admin" || user.role == "Project Admin")) {
            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { showOnboardModal = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Localization.translate("onboard_sh", lang))
                    }

                    Button(
                        onClick = { showTransferModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Swap")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Localization.translate("transfer_sh", lang))
                    }

                    Button(
                        onClick = { showExitModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Exit")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Localization.translate("exit_sh", lang))
                    }
                }
            }
        }

        // Active Shareholders listing
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Partner Ledger Indices", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(12.dp))

                    if (membersList.isEmpty()) {
                        Text("No registered members on file.")
                    } else {
                        val totalSharesSum = sharesList.sumOf { it.shareCount }.coerceAtLeast(1.0)
                        membersList.forEach { mem ->
                            val sh = sharesList.find { it.memberId == mem.id }
                            val sc = sh?.shareCount ?: 0.0
                            val pct = (sc / totalSharesSum) * 100

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(mem.fullName, fontWeight = FontWeight.SemiBold)
                                    Text(mem.mobile, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("$sc Shares", fontWeight = FontWeight.Bold)
                                    Text("${String.format("%.1f", pct)}%", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                            Divider()
                        }
                    }
                }
            }
        }

        // Shares historical timeline logger: "কার শেয়ার কে কিনেছে"
        item {
            Text(
                text = Localization.translate("sh_history_hdr", lang),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (historyList.isEmpty()) {
            item {
                Text("No capital conversions recorded yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(historyList) { th ->
                val prevMember = membersList.find { it.id == th.previousMemberId }
                val nextMember = membersList.find { it.id == th.newMemberId }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Transfer Details",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            val df = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            Text(
                                text = df.format(Date(th.transferDate)),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("From: ${prevMember?.fullName ?: "Corporate Buyback"}", fontSize = 11.sp)
                            Text("To: ${nextMember?.fullName ?: "Project Reserve"}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Units: ${th.shareAmount} Sh", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Asset Price: ${th.purchaseValue} $", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        Text(
                            text = "Reference: ${th.reason}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }

    // Modal forms
    if (showOnboardModal) {
        var mName by remember { mutableStateOf("") }
        var mMobile by remember { mutableStateOf("") }
        var mEmail by remember { mutableStateOf("") }
        var mShares by remember { mutableStateOf("10") }
        var mValue by remember { mutableStateOf("1000") }

        AlertDialog(
            onDismissRequest = { showOnboardModal = false },
            title = { Text(Localization.translate("onboard_sh", lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = mName, onValueChange = { mName = it }, label = { Text("Shareholder Name") })
                    OutlinedTextField(value = mMobile, onValueChange = { mMobile = it }, label = { Text("Mobile Phone") })
                    OutlinedTextField(value = mEmail, onValueChange = { mEmail = it }, label = { Text("Email Address") })
                    OutlinedTextField(value = mShares, onValueChange = { mShares = it }, label = { Text("Starting Share Quantity") })
                    OutlinedTextField(value = mValue, onValueChange = { mValue = it }, label = { Text("Par Face Value") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (mName.isNotBlank()) {
                            viewModel.submitMemberOnboarding(
                                fullName = mName,
                                mobile = mMobile,
                                email = mEmail,
                                initialShares = mShares.toDoubleOrNull() ?: 10.0,
                                shareVal = mValue.toDoubleOrNull() ?: 1000.0
                            )
                            showOnboardModal = false
                        }
                    }
                ) {
                    Text(Localization.translate("apply", lang))
                }
            }
        )
    }

    if (showTransferModal) {
        var fromIndex by remember { mutableIntStateOf(0) }
        var toIndex by remember { mutableIntStateOf(0) }
        var shareCount by remember { mutableStateOf("") }
        var shareVal by remember { mutableStateOf("") }
        var commentReason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showTransferModal = false },
            title = { Text(Localization.translate("transfer_sh", lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Requires mandatory 100% shareholder approval workflow.")

                    if (membersList.size >= 2) {
                        var fromExpanded by remember { mutableStateOf(false) }
                        var toExpanded by remember { mutableStateOf(false) }

                        Box {
                            OutlinedButton(onClick = { fromExpanded = true }) {
                                Text("Seller: ${membersList.getOrNull(fromIndex)?.fullName ?: "Select"}")
                            }
                            DropdownMenu(expanded = fromExpanded, onDismissRequest = { fromExpanded = false }) {
                                membersList.forEachIndexed { i, m ->
                                    DropdownMenuItem(text = { Text(m.fullName) }, onClick = { fromIndex = i; fromExpanded = false })
                                }
                            }
                        }

                        Box {
                            OutlinedButton(onClick = { toExpanded = true }) {
                                Text("Buyer: ${membersList.getOrNull(toIndex)?.fullName ?: "Select"}")
                            }
                            DropdownMenu(expanded = toExpanded, onDismissRequest = { toExpanded = false }) {
                                membersList.forEachIndexed { i, m ->
                                    DropdownMenuItem(text = { Text(m.fullName) }, onClick = { toIndex = i; toExpanded = false })
                                }
                            }
                        }
                    }

                    OutlinedTextField(value = shareCount, onValueChange = { shareCount = it }, label = { Text("Transfer Share Units") })
                    OutlinedTextField(value = shareVal, onValueChange = { shareVal = it }, label = { Text("Exchange Price (Total)") })
                    OutlinedTextField(value = commentReason, onValueChange = { commentReason = it }, label = { Text("Transfer Purpose") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sellMem = membersList.getOrNull(fromIndex)
                        val buyMem = membersList.getOrNull(toIndex)
                        val qty = shareCount.toDoubleOrNull()
                        val prc = shareVal.toDoubleOrNull()
                        if (sellMem != null && buyMem != null && qty != null && prc != null && sellMem.id != buyMem.id) {
                            viewModel.submitShareTransfer(
                                fromMemId = sellMem.id,
                                toMemId = buyMem.id,
                                shareAmount = qty,
                                purchaseValue = prc,
                                reason = commentReason
                            )
                            showTransferModal = false
                        }
                    }
                ) {
                    Text(Localization.translate("apply", lang))
                }
            }
        )
    }

    if (showExitModal) {
        var sellerIndex by remember { mutableIntStateOf(0) }
        var exitStrategy by remember { mutableStateOf("Project Buyback") }
        var buyerMemberIndex by remember { mutableIntStateOf(0) }
        var buyerNewName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showExitModal = false },
            title = { Text(Localization.translate("exit_sh", lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Exit options trigger a multi-member 100% consent requirement.")

                    if (membersList.isNotEmpty()) {
                        var mExpanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(onClick = { mExpanded = true }) {
                                Text("Select Member: ${membersList[sellerIndex].fullName}")
                            }
                            DropdownMenu(expanded = mExpanded, onDismissRequest = { mExpanded = false }) {
                                membersList.forEachIndexed { i, m ->
                                    DropdownMenuItem(text = { Text(m.fullName) }, onClick = { sellerIndex = i; mExpanded = false })
                                }
                            }
                        }
                    }

                    // Exit strategy Picker
                    val strategies = listOf("Project Buyback", "Existing Member Purchase", "New Person Purchase")
                    var sExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { sExpanded = true }) {
                            Text("Strategy: $exitStrategy")
                        }
                        DropdownMenu(expanded = sExpanded, onDismissRequest = { sExpanded = false }) {
                            strategies.forEach { s ->
                                DropdownMenuItem(text = { Text(s) }, onClick = { exitStrategy = s; sExpanded = false })
                            }
                        }
                    }

                    if (exitStrategy == "Existing Member Purchase") {
                        var bmExpanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(onClick = { bmExpanded = true }) {
                                Text("Assign Buying Partner: ${membersList.getOrNull(buyerMemberIndex)?.fullName ?: "Select"}")
                            }
                            DropdownMenu(expanded = bmExpanded, onDismissRequest = { bmExpanded = false }) {
                                membersList.forEachIndexed { i, m ->
                                    DropdownMenuItem(text = { Text(m.fullName) }, onClick = { buyerMemberIndex = i; bmExpanded = false })
                                }
                            }
                        }
                    } else if (exitStrategy == "New Person Purchase") {
                        OutlinedTextField(value = buyerNewName, onValueChange = { buyerNewName = it }, label = { Text("New Member Full Name") })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val seller = membersList.getOrNull(sellerIndex)
                        if (seller != null) {
                            val buyMemId = membersList.getOrNull(buyerMemberIndex)?.id
                            viewModel.submitMemberExitRequest(
                                memberId = seller.id,
                                exitStrategy = exitStrategy,
                                buyerMemberId = buyMemId,
                                buyerName = buyerNewName,
                                buyerMobile = "+880175550000",
                                buyerEmail = "new_partner@aquafarm.com"
                            )
                            showExitModal = false
                        }
                    }
                ) {
                    Text(Localization.translate("apply", lang))
                }
            }
        )
    }
}

// Custom canvas pie chart to represent ownership holding divisions
@Composable
fun ShareholderDistributionPie(shares: List<ShareEntity>, members: List<MemberEntity>) {
    if (shares.isEmpty()) {
        Text("No active equity registries created.")
        return
    }

    val totalCountSum = shares.sumOf { it.shareCount }.toFloat()
    val sliceColors = listOf(
        Color(0xFF4CAF50),
        Color(0xFF2196F3),
        Color(0xFFFF9800),
        Color(0xFF9C27B0),
        Color(0xFFE91E63),
        Color(0xFF00BCD4)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(
            modifier = Modifier
                .size(110.dp)
                .weight(1f)
        ) {
            var currentAngle = 0f
            shares.forEachIndexed { idx, item ->
                val sweep = (item.shareCount.toFloat() / totalCountSum) * 360f
                val col = sliceColors[idx % sliceColors.size]
                drawArc(
                    color = col,
                    startAngle = currentAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    size = Size(size.width, size.height)
                )
                currentAngle += sweep
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Legend Descriptions Columns
        Column(
            modifier = Modifier.weight(1.2f),
            verticalArrangement = Arrangement.Center
        ) {
            shares.forEachIndexed { idx, item ->
                val mem = members.find { it.id == item.memberId }
                val label = mem?.fullName ?: "Unknown"
                val col = sliceColors[idx % sliceColors.size]

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(col, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$label (${String.format("%.0f", (item.shareCount / totalCountSum) * 100)}%)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// --- SCREEN 4: PONDS MONITORING & FEED LOGISTICS ---
@Composable
fun PondsMonitoringScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val proj = viewModel.currentProject
    val user = viewModel.currentUser ?: return

    val pondsList by viewModel.currentPonds.collectAsState(initial = emptyList())
    val stocksList by viewModel.allFishStocks.collectAsState(initial = emptyList())

    var showAddPondModal by remember { mutableStateOf(false) }
    var showStockModals by remember { mutableStateOf(false) }
    var showFeedModal by remember { mutableStateOf(false) }
    
    // Dynamic Growth variables mapping
    var activeStockIdToLog by remember { mutableIntStateOf(0) }
    var showGrowthModal by remember { mutableStateOf(false) }

    if (proj == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Select a farm from the dashboard to initialize tracking components.", textAlign = TextAlign.Center)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${proj.name} - Pond Monit Desk",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                if (proj.isActive && (user.role == "Super Admin" || user.role == "Project Admin")) {
                    TextButton(onClick = { showAddPondModal = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Localization.translate("add_pond", lang))
                    }
                }
            }
        }

        if (pondsList.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No ponds mapped for this farm project layout. Request your Project Lead to populate.",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Action controls Grid for Ponds
            if (proj.isActive && (user.role == "Super Admin" || user.role == "Project Admin")) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(onClick = { showStockModals = true }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.WaterDrop, contentDescription = "Stock")
                              Spacer(modifier = Modifier.width(4.dp))
                              Text("Add Stocking", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { showFeedModal = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Default.Egg, contentDescription = "Feed")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Register Feed", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Growth forecasting graph representation using custom canvas drawing
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            Localization.translate("growth_forecast", lang),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        FishGrowthCanvasGraph(stocksList)
                        Text("Timeline (Days vs Specimen weight grams predicted)", fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            // Main listings of ponds and details
            items(pondsList) { pond ->
                val pondStocks = stocksList.filter { it.pondId == pond.id }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.GridOn, contentDescription = "Pond", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(pond.pondNumber, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            }

                            // Water conditions indicator
                            Surface(
                                shape = CircleShape,
                                color = when (pond.waterCondition) {
                                    "Good" -> Color(0xFFE8F5E9)
                                    "Warning" -> Color(0xFFFFF3E0)
                                    else -> Color(0xFFFFEBEE)
                                }
                            ) {
                                Text(
                                    text = pond.waterCondition,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when (pond.waterCondition) {
                                        "Good" -> Color(0xFF2E7D32)
                                        "Warning" -> Color(0xFFEF6C00)
                                        else -> Color(0xFFC62828)
                                    }
                                )
                            }
                        }

                        Text("Pond size: ${pond.sizeSqFt} sq.ft", fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))

                        Spacer(modifier = Modifier.height(12.dp))

                        if (pondStocks.isEmpty()) {
                            Text("No active fish specimens recorded under this pond layout.", fontSize = 11.sp)
                        } else {
                            for (stock in pondStocks) {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(stock.species, fontWeight = FontWeight.Bold)
                                        if (stock.isHarvested) {
                                            Text("Harvested Successfully", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                                        } else {
                                            Text("Released count: ${stock.quantityStarted}", fontSize = 11.sp)
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Est Weight: ${stock.avgWeightGm} g - Current Density: ${stock.quantityCurrent} P.", fontSize = 11.sp)
                                        Text("Mortality count: ${stock.mortalityCount}", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                    }

                                    if (proj.isActive && !stock.isHarvested) {
                                        TextButton(
                                            onClick = {
                                                activeStockIdToLog = stock.id
                                                showGrowthModal = true
                                            },
                                            modifier = Modifier.align(Alignment.End)
                                        ) {
                                            Icon(Icons.Default.EditCalendar, contentDescription = "Calendar", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Modify Growth Log", fontSize = 11.sp)
                                        }
                                    }
                                    Divider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddPondModal) {
        var num by remember { mutableStateOf("") }
        var area by remember { mutableStateOf("") }
        var cond by remember { mutableStateOf("Good") }

        AlertDialog(
            onDismissRequest = { showAddPondModal = false },
            title = { Text(Localization.translate("add_pond", lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = num, onValueChange = { num = it }, label = { Text("Pond Identifier (e.g. Pond-03)") })
                    OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Surface Area Sq.Ft") })
                    
                    val conditions = listOf("Good", "Warning", "Alert")
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { expanded = true }) {
                            Text("Water condition: $cond")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            conditions.forEach { c ->
                                DropdownMenuItem(text = { Text(c) }, onClick = { cond = c; expanded = false })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ar = area.toDoubleOrNull()
                        if (num.isNotBlank() && ar != null) {
                            viewModel.addPondLayout(num, ar, cond)
                            showAddPondModal = false
                        }
                    }
                ) {
                    Text(Localization.translate("apply", lang))
                }
            }
        )
    }

    if (showStockModals) {
        var pondIdx by remember { mutableIntStateOf(0) }
        var species by remember { mutableStateOf("Rui") }
        var count by remember { mutableStateOf("5000") }
        var avgWeight by remember { mutableStateOf("10.5") }

        AlertDialog(
            onDismissRequest = { showStockModals = false },
            title = { Text("Stock Fish Specimen") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (pondsList.isNotEmpty()) {
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(onClick = { expanded = true }) {
                                Text("Select Pond: ${pondsList[pondIdx].pondNumber}")
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                pondsList.forEachIndexed { i, p ->
                                    DropdownMenuItem(text = { Text(p.pondNumber) }, onClick = { pondIdx = i; expanded = false })
                                }
                            }
                        }
                    }

                    OutlinedTextField(value = species, onValueChange = { species = it }, label = { Text("Fish Breed / Species") })
                    OutlinedTextField(value = count, onValueChange = { count = it }, label = { Text("Stocking Release Quantity") })
                    OutlinedTextField(value = avgWeight, onValueChange = { avgWeight = it }, label = { Text("Release Avg Weight (gm)") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetPond = pondsList.getOrNull(pondIdx)
                        val qty = count.toIntOrNull()
                        val wt = avgWeight.toDoubleOrNull()
                        if (targetPond != null && qty != null && wt != null) {
                            viewModel.stockFishPond(targetPond.id, species, qty, wt)
                            showStockModals = false
                        }
                    }
                ) {
                    Text(Localization.translate("apply", lang))
                }
            }
        )
    }

    if (showFeedModal) {
        var pondIdx by remember { mutableIntStateOf(0) }
        var brand by remember { mutableStateOf("Mega Feed") }
        var feedType by remember { mutableStateOf("Floating Feed") }
        var qty by remember { mutableStateOf("50") }
        var cost by remember { mutableStateOf("3500") }
        var intake by remember { mutableStateOf("22") }

        AlertDialog(
            onDismissRequest = { showFeedModal = false },
            title = { Text("Register Feed Integration") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (pondsList.isNotEmpty()) {
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(onClick = { expanded = true }) {
                                Text("Active Target Pond: ${pondsList[pondIdx].pondNumber}")
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                pondsList.forEachIndexed { i, p ->
                                    DropdownMenuItem(text = { Text(p.pondNumber) }, onClick = { pondIdx = i; expanded = false })
                                }
                            }
                        }
                    }

                    OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Feed Brand name") })
                    OutlinedTextField(value = feedType, onValueChange = { feedType = it }, label = { Text("Feed Classification") })
                    OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Total Quantity (kg)") })
                    OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Procurement Cost (Total)") })
                    OutlinedTextField(value = intake, onValueChange = { intake = it }, label = { Text("Daily Consumption dosage (kg)") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetPond = pondsList.getOrNull(pondIdx)
                        val qt = qty.toDoubleOrNull()
                        val cs = cost.toDoubleOrNull()
                        val tk = intake.toDoubleOrNull()
                        if (targetPond != null && qt != null && cs != null && tk != null) {
                            viewModel.registerFeedConsumption(targetPond.id, brand, feedType, qt, cs, tk)
                            showFeedModal = false
                        }
                    }
                ) {
                    Text(Localization.translate("apply", lang))
                }
            }
        )
    }

    if (showGrowthModal) {
        var weightUp by remember { mutableStateOf("") }
        var mortUp by remember { mutableStateOf("0") }
        var harvestState by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showGrowthModal = false },
            title = { Text("Modify Fish Growth logs") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = weightUp, onValueChange = { weightUp = it }, label = { Text("Average specimen weight (gm)") })
                    OutlinedTextField(value = mortUp, onValueChange = { mortUp = it }, label = { Text("Registered Mortality Count") })

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = harvestState, onCheckedChange = { harvestState = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mark Pond fully Harvested")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val wt = weightUp.toDoubleOrNull()
                        val mC = mortUp.toIntOrNull() ?: 0
                        if (wt != null) {
                            viewModel.submitGrowthLogUpdate(activeStockIdToLog, wt, mC, harvestState)
                            showGrowthModal = false
                        }
                    }
                ) {
                    Text(Localization.translate("apply", lang))
                }
            }
        )
    }
}

// Fish Growth graphical canvas plotter
@Composable
fun FishGrowthCanvasGraph(stocks: List<FishStockEntity>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(8.dp)
    ) {
        val width = size.width
        val height = size.height

        // Grids backgrounds
        for (i in 1..4) {
            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(0f, (height / 5) * i),
                end = Offset(width, (height / 5) * i),
                strokeWidth = 1f
            )
        }

        if (stocks.isNotEmpty()) {
            val maxWeight = (stocks.map { it.avgWeightGm }.maxOrNull() ?: 500.0).coerceAtLeast(100.0)
            val segment = width / (stocks.size).coerceAtLeast(1)

            stocks.forEachIndexed { i, s ->
                val x = i * segment + (segment / 2)
                val barHeight = (s.avgWeightGm / maxWeight) * (height * 0.8f)
                val y = height - barHeight.toFloat()

                // Draw customized colorful modern cylindrical vertical graphs
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor, tertiaryColor)
                    ),
                    topLeft = Offset(x - 15f, y),
                    size = Size(30f, barHeight.toFloat())
                )
            }
        }
    }
}

// --- SCREEN 5: STAGING APPROVAL QUEUE PANEL ---
@Composable
fun ApprovalsStagingScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val proj = viewModel.currentProject
    val user = viewModel.currentUser ?: return
    
    val requestsList by viewModel.currentRequests.collectAsState(initial = emptyList())

    if (proj == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Select a farm layout to view live approval transactions.", textAlign = TextAlign.Center)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            val pendingCount = requestsList.count { it.status == "Pending" }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(width = 6.dp, height = 20.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(100.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Localization.translate("staging_queue", lang),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (pendingCount > 0) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(100.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$pendingCount PENDING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        val pendingRequests = requestsList.filter { it.status == "Pending" }

        if (pendingRequests.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        Localization.translate("no_pending_req", lang),
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(pendingRequests) { req ->
                // Votes collector logic using standard flow state tracking
                val votesList by viewModel.getVotesForRequest(req.id).collectAsState(initial = emptyList())

                val approvedCount = votesList.count { it.voteType == "Approve" }
                val rejectedCount = votesList.count { it.voteType == "Reject" }
                
                // Check if active user already voted
                val alreadyVoted = votesList.any { it.voterUsername == user.username }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = req.requestType,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(
                                        if (req.approvalTypeNeeded == "100% Approval") MaterialTheme.colorScheme.errorContainer 
                                        else MaterialTheme.colorScheme.tertiaryContainer, 
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = req.approvalTypeNeeded,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (req.approvalTypeNeeded == "100% Approval") MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }

                        Text(
                            text = req.reason,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )

                        Text(
                            text = "Created by: ${req.creatorUsername}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val totalCount = if (viewModel.currentProjectMembersCount > 0) viewModel.currentProjectMembersCount else 1
                            val approvalPercent = (approvedCount.toDouble() / totalCount) * 100
                            Text(
                                text = "Approvals: $approvedCount / ${viewModel.currentProjectMembersCount} (${String.format("%.0f", approvalPercent)}%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (rejectedCount > 0) {
                                Text("Rejections: $rejectedCount", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }

                        // Vote buttons displayed if user hasn't voted yet and project allows transaction
                        if (proj.isActive && !alreadyVoted && req.status == "Pending" && user.role != "Auditor") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.voteOnRequest(req.id, "Reject") },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Reject")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(Localization.translate("reject", lang))
                                }

                                Button(
                                    onClick = { viewModel.voteOnRequest(req.id, "Approve") },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Approve")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(Localization.translate("approve", lang))
                                }
                            }
                        } else if (alreadyVoted) {
                            Text(
                                "Your vote is registered for this staging proposal.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 6: DYNAMIC PROFILE CONFIGURATIONS & SYSTEM AUDIT LOGS ---
@Composable
fun ProfileSettingsScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val user = viewModel.currentUser ?: return
    val selectedProj = viewModel.currentProject
    
    val allLogsList by viewModel.auditLogs.collectAsState()

    var showPassChangeModal by remember { mutableStateOf(false) }
    var showUserCreatorModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = Localization.translate("profile_details", lang),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("User ID: ${user.username}", fontWeight = FontWeight.Bold)
                    Text("Full Name: ${user.fullName}")
                    Text("Role Assignment: ${user.role}")
                    Text("Associated Mobile: ${user.mobileNumber}")
                    Text("Email ID: ${user.email}")
                    
                    TextButton(onClick = { showPassChangeModal = true }, modifier = Modifier.testTag("prof_change_pwd")) {
                        Icon(Icons.Default.LockReset, contentDescription = "Reset")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Change Active Password")
                    }
                }
            }
        }

        // Active Session tracking
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        Localization.translate("active_sessions", lang),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(Localization.translate("this_device", lang), fontWeight = FontWeight.SemiBold)
                            Text("IP Address: 192.168.1.104", fontSize = 11.sp)
                        }
                        IconButton(onClick = { viewModel.logConsole("Active browser session logs validated. Single active device retained.") }) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = "Verified")
                        }
                    }
                }
            }
        }

        // Super Admin management panels
        if (user.role == "Super Admin") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Security Management Panels", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Button(
                                onClick = { showUserCreatorModal = true },
                                modifier = Modifier.testTag("admin_register_user_btn")
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = "Add")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Account")
                            }
                        }

                        val allUsersSystem by viewModel.users.collectAsState()
                        allUsersSystem.forEach { empUser ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("${empUser.fullName}", fontWeight = FontWeight.Bold)
                                            Text("User ID: ${empUser.userId.ifBlank { "N/A" }} | Username: ${empUser.username}", fontSize = 11.sp, color = Color.Gray)
                                        }

                                        // Status badge
                                        val statusColor = if (empUser.status == "Active") Color(0xFF4CAF50) else Color(0xFFF44336)
                                        Text(
                                            empUser.status,
                                            color = statusColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(4.dp)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Role: ${empUser.role}", fontSize = 12.sp, fontWeight = FontWeight.Medium)

                                        // Suspend / Activate toggle
                                        if (empUser.username != user.username) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                TextButton(
                                                    onClick = {
                                                        if (empUser.status == "Active") {
                                                            viewModel.suspendUser(empUser)
                                                        } else {
                                                            viewModel.activateUser(empUser)
                                                        }
                                                    }
                                                ) {
                                                    Text(if (empUser.status == "Active") "Suspend" else "Activate")
                                                }

                                                // Change Role dropdown trigger
                                                var roleExpanded by remember { mutableStateOf(false) }
                                                Box {
                                                    OutlinedButton(onClick = { roleExpanded = true }) {
                                                        Text("Change Role")
                                                    }
                                                    DropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                                                        listOf("Super Admin", "Project Admin", "Member", "Auditor").forEach { newR ->
                                                            DropdownMenuItem(
                                                                text = { Text(newR) },
                                                                onClick = {
                                                                    viewModel.changeUserRole(empUser, newR)
                                                                    roleExpanded = false
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Reset password options
                                    if (empUser.username != user.username) {
                                        var adminSetPass by remember { mutableStateOf("") }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = adminSetPass,
                                                onValueChange = { adminSetPass = it },
                                                label = { Text("Reset Password") },
                                                placeholder = { Text("StrongPass@2026") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                            )
                                            Button(
                                                onClick = {
                                                    if (adminSetPass.length >= 12) {
                                                        viewModel.resetUserPassword(empUser, adminSetPass)
                                                        adminSetPass = ""
                                                    }
                                                },
                                                enabled = adminSetPass.length >= 12
                                            ) {
                                                Text("Reset")
                                            }
                                        }
                                        Text("Password must be at least 12 characters", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Backup and Restore logs point
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Data Safety Backups", fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(onClick = { viewModel.performDatabaseBackup() }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Backup, contentDescription = "Backup")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Run Backup")
                        }
                        OutlinedButton(onClick = { viewModel.loadDatabaseReset() }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Restore, contentDescription = "Restore")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restore DB")
                        }
                    }

                    if (viewModel.backupStatusText.isNotEmpty()) {
                        Text(
                            text = viewModel.backupStatusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Simulated terminal secure console updates log
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Terminal, contentDescription = "Console", tint = Color.Green, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Simulated security Terminal alerts",
                            color = Color.Green,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (viewModel.securityConsoleLogs.isEmpty()) {
                        Text("No alert triggers logged", color = Color.Gray, fontSize = 10.sp)
                    } else {
                        viewModel.securityConsoleLogs.take(5).forEach { cl ->
                            Text(
                                text = cl,
                                color = Color.Green,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- Active Session Management Block (Rule 103) ---
        item {
            Text(
                text = if (lang == Localization.Language.BN) "📱 সক্রিয় ডিভাইস ও সেশন সংযোগ" else "📱 Active Devices & Session Control",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == Localization.Language.BN) "পরিশোধিত লগইন সংযোগসমূহ" else "Registered Active Devices",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        
                        TextButton(
                            onClick = { viewModel.logoutAllDevicesExceptCurrent() },
                            modifier = Modifier.testTag("purge_all_sessions_btn")
                        ) {
                            Text(
                                text = if (lang == Localization.Language.BN) "সকল ডিভাইস লগআউট" else "Terminate Remote Devices",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (viewModel.activeSessions.isEmpty()) {
                        Text(
                            "No active devices registered.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    } else {
                        viewModel.activeSessions.forEach { sess ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (sess.isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                                                     else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (sess.device.contains("iPad") || sess.device.contains("Tablet")) Icons.Default.TabletAndroid 
                                                              else Icons.Default.PhoneAndroid,
                                                contentDescription = "Device",
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = sess.device,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                            if (sess.isCurrent) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                SuggestionChip(
                                                    onClick = {},
                                                    label = { Text("Active Now", fontSize = 9.sp) },
                                                    modifier = Modifier.height(18.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${sess.browser} • IP: ${sess.ipAddress} (${sess.location})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Logged: ${sess.loginTime}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                    
                                    if (!sess.isCurrent) {
                                        IconButton(
                                            onClick = { viewModel.logoutSpecificDevice(sess.id) },
                                            modifier = Modifier.size(32.dp).testTag("logout_device_${sess.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Cancel,
                                                contentDescription = "Terminate Session",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // System security activities logs
        item {
            Text(
                text = Localization.translate("system_audit_logs", lang),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (allLogsList.isEmpty()) {
            item {
                Text("No audits recorded.", fontSize = 11.sp)
            }
        } else {
            items(allLogsList) { log ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            val df = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            Text(df.format(Date(log.timestamp)), fontSize = 10.sp)
                        }
                        Text("Module: ${log.module} | Initiated by: ${log.username}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (log.newValue.isNotEmpty()) {
                            Text("Payload: ${log.newValue}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }

        // Logout session button at the bottom of Profile layout
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.logoutCurrentSession() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Logout")
                Spacer(modifier = Modifier.width(8.dp))
                Text(Localization.translate("logout", lang))
            }
        }
    }

    if (showPassChangeModal) {
        var current by remember { mutableStateOf("") }
        var newP by remember { mutableStateOf("") }
        var confirm by remember { mutableStateOf("") }
        var errText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPassChangeModal = false },
            title = { Text("Update Current Profile Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = current, onValueChange = { current = it }, label = { Text("Current Password") }, visualTransformation = PasswordVisualTransformation())
                    OutlinedTextField(value = newP, onValueChange = { newP = it }, label = { Text("New Password") }, visualTransformation = PasswordVisualTransformation())
                    OutlinedTextField(value = confirm, onValueChange = { confirm = it }, label = { Text("Confirm New Password") }, visualTransformation = PasswordVisualTransformation())

                    val str = calculatePasswordStrength(newP)
                    PasswordStrengthIndicator(str, lang)

                    if (errText.isNotEmpty()) {
                        Text(errText, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val score = calculatePasswordStrength(newP)
                        if (newP != confirm) {
                            errText = "Passwords do not match"
                        } else if (score < 3) {
                            errText = "Password is too weak"
                        } else {
                            viewModel.changeActivePassword(current, newP) { success ->
                                if (success) {
                                    showPassChangeModal = false
                                } else {
                                    errText = "Incorrect current password"
                                }
                            }
                        }
                    }
                ) {
                    Text(Localization.translate("apply", lang))
                }
            }
        )
    }

    if (showUserCreatorModal) {
        val projectsList by viewModel.projects.collectAsState()
        val allUsersSystem by viewModel.users.collectAsState()

        var uName by remember { mutableStateOf("") }
        var fName by remember { mutableStateOf("") }
        var mob by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var selRole by remember { mutableStateOf("Project Admin") }
        var selProjId by remember { mutableStateOf<Int?>(if (projectsList.isNotEmpty()) projectsList.first().id else null) }
        var passInit by remember { mutableStateOf("User@123456789") }

        // Smart Suggest User ID based on selection count (Rule 75)
        var customUserId by remember { mutableStateOf("") }
        LaunchedEffect(selRole, selProjId) {
            val prefix = when (selRole) {
                "Super Admin" -> "SUPER"
                "Project Admin" -> if (selProjId != null) "PF%03d-ADMIN".format(selProjId) else "ADMIN"
                "Member" -> if (selProjId != null) "PF%03d-MEM".format(selProjId) else "MEM"
                "Auditor" -> "AUD"
                else -> "USER"
            }
            val count = allUsersSystem.count { it.role == selRole } + 1
            customUserId = "%s-%03d".format(prefix, count)
        }

        AlertDialog(
            onDismissRequest = { showUserCreatorModal = false },
            title = { Text("Register Employee Users") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = customUserId, onValueChange = { customUserId = it }, label = { Text("Unique User ID Code (Rule 75)") })
                    OutlinedTextField(value = uName, onValueChange = { uName = it }, label = { Text("Username login ID") })
                    OutlinedTextField(value = fName, onValueChange = { fName = it }, label = { Text("Full Employee Name") })
                    OutlinedTextField(value = mob, onValueChange = { mob = it }, label = { Text("Mobile Number") })
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") })

                    // Role switcher Dropdowns
                    val roles = listOf("Project Admin", "Member", "Auditor")
                    var rExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { rExpanded = true }) {
                            Text("Select Role: $selRole")
                        }
                        DropdownMenu(expanded = rExpanded, onDismissRequest = { rExpanded = false }) {
                            roles.forEach { r ->
                                DropdownMenuItem(text = { Text(r) }, onClick = { selRole = r; rExpanded = false })
                            }
                        }
                    }

                    // Project Switcher dropdown
                    if (projectsList.isNotEmpty() && selRole != "Auditor") {
                        var pExpanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(onClick = { pExpanded = true }) {
                                val currentSel = projectsList.find { it.id == selProjId }?.name ?: "No project"
                                Text("Assign Project: $currentSel")
                            }
                            DropdownMenu(expanded = pExpanded, onDismissRequest = { pExpanded = false }) {
                                projectsList.forEach { p ->
                                    DropdownMenuItem(text = { Text(p.name) }, onClick = { selProjId = p.id; pExpanded = false })
                                }
                            }
                        }
                    }

                    OutlinedTextField(value = passInit, onValueChange = { passInit = it }, label = { Text("Starting Password (Forced change)") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (uName.isNotBlank() && fName.isNotBlank()) {
                            viewModel.createUserAccount(
                                username = uName.trim(),
                                fullName = fName,
                                mobile = mob,
                                email = email,
                                role = selRole,
                                projectId = if (selRole == "Auditor") null else selProjId,
                                startingPass = passInit,
                                userId = customUserId
                            )
                            showUserCreatorModal = false
                        }
                    }
                ) {
                    Text(Localization.translate("apply", lang))
                }
            }
        )
    }
}

@Composable
fun InstallationWizardScreen(viewModel: AppViewModel, onDismiss: () -> Unit) {
    val lang = viewModel.currentLanguage
    var currentStep by remember { mutableStateOf(1) } // 1: Input details, 2: Verification, 3: Recovery Key
    
    // Step 1 states:
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf("") }
    
    // Password Indicators
    val holdsMinLength = password.length >= 12
    val holdsUpper = password.any { it.isUpperCase() }
    val holdsLower = password.any { it.isLowerCase() }
    val holdsDigit = password.any { it.isDigit() }
    val holdsSpecial = password.any { !it.isLetterOrDigit() }
    val isPasswordStrong = holdsMinLength && holdsUpper && holdsLower && holdsDigit && holdsSpecial

    // Step 2 states:
    var smsOtp by remember { mutableStateOf("") }
    var emailCode by remember { mutableStateOf("") }
    val targetSmsOtp = "4491"
    val targetEmailCode = "SEC-8032"
    
    // Step 3 states:
    val registeredRecoveryCode by remember { mutableStateOf(com.example.ui.SecurityUtils.generateRecoveryCode()) }
    var isConfirmedSaved by remember { mutableStateOf(false) }
    
    val context = androidx.compose.ui.platform.LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
                Text(
                    text = "First Setup & Installation Wizard",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Box(modifier = Modifier.size(24.dp)) // placeholder
            }
            
            // Steps indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Profile Setup", "Core Verification", "Emergency Recovery").forEachIndexed { index, title ->
                    val stepNum = index + 1
                    val isCurrent = stepNum == currentStep
                    val isPast = stepNum < currentStep
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (isCurrent) MaterialTheme.colorScheme.primary
                                else if (isPast) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }
        }

        if (currentStep == 1) {
            item {
                Text(
                    text = "Welcome to First installation & Setup Wizard",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Configure a secure system Super Admin profile. The details entered here will manage your enterprise projects securely (Rule 63).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            
            item {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            item {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Mobile Number (with +880)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Strong Password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Pass"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Button(
                    onClick = {
                        val strong = com.example.ui.SecurityUtils.generateStrongPassword()
                        password = strong
                        passwordVisible = true
                        viewModel.logConsole("SYSTEM GENERATED PASS: Copy of generated seed password '$strong' dispatched safely.")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Magic")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Auto-Generate Strong Password (Rule 71)")
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Super Admin Password Policy Checklists (Rule 64):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        val checks = listOf(
                            Pair("Minimum 12 Characters", holdsMinLength),
                            Pair("Require Uppercase Letter [A-Z]", holdsUpper),
                            Pair("Require Lowercase Letter [a-z]", holdsLower),
                            Pair("Require Numeric Digit [0-9]", holdsDigit),
                            Pair("Require Special Character [@#$%^&*_!]", holdsSpecial)
                        )
                        
                        checks.forEach { (title, holds) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (holds) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = "Status",
                                    tint = if (holds) Color(0xFF67C23A) else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (holds) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            if (feedbackText.isNotEmpty()) {
                item {
                    Text(feedbackText, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }

            item {
                Button(
                    onClick = {
                        if (fullName.isBlank() || username.isBlank() || mobile.isBlank() || email.isBlank() || password.isBlank()) {
                            feedbackText = "All fields are required to setup system admin"
                        } else if (!isPasswordStrong) {
                            feedbackText = "Password fails company strong policy criteria!"
                        } else {
                            feedbackText = ""
                            currentStep = 2
                            viewModel.logConsole("MANDATORY VERIFICATION CODE: Mobile OTP: $targetSmsOtp, Email Verification Code: $targetEmailCode")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(50.dp),
                    enabled = isPasswordStrong,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Proceed to Verification factors")
                }
            }
        } else if (currentStep == 2) {
            item {
                Text(
                    text = "Initial security Verification factors (Rule 65)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Enter simulated mobile OTP and email authentication key dispatched to security logs console to activate profile.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "🔑 Dispatch Console",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Simulated Mobile OTP: $targetSmsOtp\nSimulated Email Code: $targetEmailCode",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = smsOtp,
                    onValueChange = { smsOtp = it },
                    label = { Text("Mobile SMS OTP") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = emailCode,
                    onValueChange = { emailCode = it },
                    label = { Text("Email Verification Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (feedbackText.isNotEmpty()) {
                item {
                    Text(feedbackText, color = MaterialTheme.colorScheme.error)
                }
            }

            item {
                Button(
                    onClick = {
                        if (smsOtp != targetSmsOtp || emailCode != targetEmailCode) {
                            feedbackText = "Incorrect SMS OTP or Email Verification Code!"
                        } else {
                            feedbackText = ""
                            currentStep = 3
                            viewModel.logConsole("VERIFICATION PASSED: Security factors cleared. Key generated.")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Verify & Continue")
                }
            }
        } else {
            item {
                Text(
                    text = "Emergency Recovery Code generated (Rule 69)",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "An Emergency Recovery Backup code has been created. The Super Admin must download, print, or copy this code offline. It is the ONLY way to recover access if passwords are forgotten.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "EMERGENCY BACKUP RECOVERY KEY",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = registeredRecoveryCode,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Recovery Key", registeredRecoveryCode)
                            clipboard.setPrimaryClip(clip)
                            viewModel.logConsole("RECOVERY KEY COPYING: Key '$registeredRecoveryCode' copied successfully.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Key")
                    }

                    Button(
                        onClick = {
                            viewModel.logConsole("SYSTEM DOCUMENTATION: Triggered recovery emergency download backup file sequence 'recovery_credentials.txt' containing key '$registeredRecoveryCode'.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download txt")
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isConfirmedSaved,
                        onCheckedChange = { isConfirmedSaved = it ?: false }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "I have downloaded or written my Emergency Recovery Code in a secure physical vault.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        val passHash = com.example.ui.SecurityUtils.hashPassword(password)
                        viewModel.registerSuperAdminFromWizard(
                            username = username.lowercase().trim(),
                            fullName = fullName,
                            mobile = mobile,
                            email = email,
                            passHash = passHash,
                            recoveryCode = registeredRecoveryCode
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(52.dp),
                    enabled = isConfirmedSaved,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Activate Account & Finish")
                }
            }
        }
    }
}

// ==================================
// INTERNAL MESSAGING SYSTEM SCREEN
// ==================================
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InternalMessagingScreen(viewModel: AppViewModel) {
    val lang = viewModel.currentLanguage
    val currentUser = viewModel.currentUser ?: return
    val currentUserId = if (currentUser.userId.isNotBlank()) currentUser.userId else currentUser.username.uppercase()

    val messagesList by viewModel.messages.collectAsState()
    val usersList by viewModel.users.collectAsState()

    var activeSubTab by remember { mutableStateOf("inbox") } // "inbox", "sent", "drafts", "trash"
    var searchText by remember { mutableStateOf("") }
    var selectedPriorityFilter by remember { mutableStateOf("All") } // "All", "Normal", "Important", "Urgent"

    var showComposeDialog by remember { mutableStateOf(false) }
    var activeConversationMessage by remember { mutableStateOf<MessageEntity?>(null) } // Conversation thread detail viewer

    // Filter messages based on search, priority, deleted states and folder (inbox/sent/drafts/archive)
    val filteredMessages = messagesList.filter { msg ->
        // Priority filter
        val matchesPriority = selectedPriorityFilter == "All" || msg.priority.equals(selectedPriorityFilter, ignoreCase = true)

        // Search text matching subject, body, or sender/receiver User IDs
        val matchesSearch = searchText.isEmpty() ||
                msg.subject.contains(searchText, ignoreCase = true) ||
                msg.body.contains(searchText, ignoreCase = true) ||
                msg.senderUserId.contains(searchText, ignoreCase = true) ||
                msg.receiverUserId.contains(searchText, ignoreCase = true)

        if (!matchesPriority || !matchesSearch) return@filter false

        when (activeSubTab) {
            "inbox" -> msg.receiverUserId.equals(currentUserId, ignoreCase = true) && !msg.receiverDeleted && !msg.isDraft && !msg.isTrash
            "sent" -> msg.senderUserId.equals(currentUserId, ignoreCase = true) && !msg.senderDeleted && !msg.isDraft && !msg.isTrash
            "drafts" -> msg.senderUserId.equals(currentUserId, ignoreCase = true) && msg.isDraft && !msg.isTrash
            "trash" -> msg.isTrash || (msg.receiverUserId.equals(currentUserId, ignoreCase = true) && msg.receiverDeleted) || (msg.senderUserId.equals(currentUserId, ignoreCase = true) && msg.senderDeleted)
            else -> false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Localization.translate("messages", lang),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            // Compose action
            if (currentUser.role != "Auditor") {
                Button(
                    onClick = { showComposeDialog = true },
                    modifier = Modifier.testTag("msg_compose_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Compose")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Localization.translate("compose", lang))
                }
            } else {
                Text(
                    "Restricted Mode",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Super Admin Toggle Panel for cross project messaging (Rule 81)
        if (currentUser.role == "Super Admin") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Cross-Project Communications",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "When disabled, non-admin users cannot message outside their project.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = viewModel.isCrossProjectMessagingEnabled,
                        onCheckedChange = { viewModel.isCrossProjectMessagingEnabled = it },
                        modifier = Modifier.testTag("toggle_cross_project")
                    )
                }
            }
        }

        // Folder Tabs Scrollable Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Pair("inbox", Localization.translate("inbox", lang)),
                Pair("sent", Localization.translate("sent", lang)),
                Pair("drafts", Localization.translate("drafts", lang)),
                Pair("trash", Localization.translate("trash", lang))
            )

            tabs.forEach { (key, label) ->
                val selected = activeSubTab == key
                val counts = when (key) {
                    "inbox" -> messagesList.count { it.receiverUserId.equals(currentUserId, ignoreCase = true) && !it.isRead && !it.isTrash && !it.isDraft }
                    else -> 0
                }

                InputChip(
                    selected = selected,
                    onClick = { activeSubTab = key },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(label)
                            if (counts > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge(containerColor = MaterialTheme.colorScheme.error) {
                                    Text(counts.toString(), color = Color.White)
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("msg_tab_$key"),
                    leadingIcon = {
                        val icon = when (key) {
                            "inbox" -> Icons.Default.Inbox
                            "sent" -> Icons.Default.Send
                            "drafts" -> Icons.Default.Drafts
                            else -> Icons.Default.Delete
                        }
                        Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }

        // Search + Priority Filter Panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Search messages...") },
                placeholder = { Text("Subject, description, User ID...") },
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("msg_search_input"),
                singleLine = true
            )

            // Priority Trigger
            var pExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(onClick = { pExpanded = true }) {
                    Text("Priority: $selectedPriorityFilter")
                }
                DropdownMenu(expanded = pExpanded, onDismissRequest = { pExpanded = false }) {
                    listOf("All", "Normal", "Important", "Urgent").forEach { priority ->
                        DropdownMenuItem(
                            text = { Text(priority) },
                            onClick = {
                                selectedPriorityFilter = priority
                                pExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Conversation thread detail viewer overlay pane
        activeConversationMessage?.let { selectedMsg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedMsg.subject,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { activeConversationMessage = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close thread view")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("From: ${selectedMsg.senderUserId}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text("To: ${selectedMsg.receiverUserId}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }

                    Text(
                        text = selectedMsg.body,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val formattedDate = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(selectedMsg.sentTime))
                        Text(formattedDate, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Star
                            IconButton(onClick = { viewModel.toggleStarMessage(selectedMsg) }) {
                                Icon(
                                    imageVector = if (selectedMsg.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Star message",
                                    tint = if (selectedMsg.isStarred) Color(0xFFFFC107) else Color.Gray
                                )
                            }
                            // Delete
                            IconButton(
                                onClick = {
                                    val isSender = selectedMsg.senderUserId.equals(currentUserId, ignoreCase = true)
                                    viewModel.softDeleteMessage(selectedMsg, isSender)
                                    activeConversationMessage = null
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Move to trash")
                            }
                        }
                    }

                    // Composer for threaded reply
                    var replyText by remember { mutableStateOf("") }
                    var replyStatusMessage by remember { mutableStateOf("") }

                    if (currentUser.role != "Auditor") {
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            placeholder = { Text("Write quick reply thread...") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (replyText.isNotBlank()) {
                                            viewModel.sendInternalMessage(
                                                trackerSubject = "Re: ${selectedMsg.subject}",
                                                bodyText = replyText,
                                                toUserId = selectedMsg.senderUserId,
                                                priority = selectedMsg.priority,
                                                parentMsgId = selectedMsg.id
                                            ) { success, resultText ->
                                                if (success) {
                                                    replyText = ""
                                                    replyStatusMessage = "Reply dispatched successfully!"
                                                } else {
                                                    replyStatusMessage = resultText
                                                }
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Send reply")
                                }
                            }
                        )

                        if (replyStatusMessage.isNotEmpty()) {
                            Text(replyStatusMessage, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Messages List Scrollable layout
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredMessages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No internal correspondence found.", color = Color.Gray)
                    }
                }
            } else {
                items(filteredMessages) { msg ->
                    val isUnread = !msg.isRead && msg.receiverUserId.equals(currentUserId, ignoreCase = true)
                    val cardBgColor = if (isUnread) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isUnread) {
                                    viewModel.markMessageAsRead(msg)
                                }
                                activeConversationMessage = msg
                            },
                        colors = CardDefaults.cardColors(containerColor = cardBgColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Priority Icon/Badging indicator
                            val (badgeColor, label) = when (msg.priority.uppercase()) {
                                "URGENT" -> Pair(MaterialTheme.colorScheme.error, "Urgent")
                                "IMPORTANT" -> Pair(Color(0xFFE65100), "Important")
                                else -> Pair(MaterialTheme.colorScheme.secondary, "Normal")
                            }

                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(badgeColor, shape = CircleShape)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (msg.senderUserId.equals(currentUserId, ignoreCase = true)) "To: ${msg.receiverUserId}" else "From: ${msg.senderUserId}",
                                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )

                                    val df = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                                    Text(
                                        text = df.format(Date(msg.sentTime)),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = msg.subject,
                                    fontWeight = if (isUnread) FontWeight.Black else FontWeight.Medium,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = msg.body,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Quick star toggle indicator
                            IconButton(
                                onClick = { viewModel.toggleStarMessage(msg) }
                            ) {
                                Icon(
                                    imageVector = if (msg.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Star toggle",
                                    tint = if (msg.isStarred) Color(0xFFFFC107) else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Compose Messaging Alert Dialog Interface (Rule 77 & Code Rules)
    if (showComposeDialog) {
        var composerToUser by remember { mutableStateOf("") }
        var composerSubject by remember { mutableStateOf("") }
        var composerBody by remember { mutableStateOf("") }
        var composerPriority by remember { mutableStateOf("Normal") }
        var dialogStatusMessage by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showComposeDialog = false },
            title = { Text(Localization.translate("compose", lang)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Receiver Auto Update Select dropdown options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = composerToUser,
                            onValueChange = { composerToUser = it },
                            label = { Text(Localization.translate("msg_to", lang)) },
                            placeholder = { Text("e.g. ADMIN-001 or pm1") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("msg_to_input_field"),
                            singleLine = true
                        )

                        // Quick Select register users list drawer helper
                        var suggestionsExpanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { suggestionsExpanded = true }) {
                                Icon(Icons.Default.ContactMail, contentDescription = "Open quick directory list")
                            }
                            DropdownMenu(
                                expanded = suggestionsExpanded,
                                onDismissRequest = { suggestionsExpanded = false }
                            ) {
                                usersList.filter { it.userId != currentUserId && it.username != currentUser.username }.forEach { u ->
                                    val formattedLabel = "${u.fullName} (${u.role} - ID: ${u.userId.ifBlank { u.username.uppercase() }})"
                                    DropdownMenuItem(
                                        text = { Text(formattedLabel) },
                                        onClick = {
                                            composerToUser = u.userId.ifBlank { u.username.uppercase() }
                                            suggestionsExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = composerSubject,
                        onValueChange = { composerSubject = it },
                        label = { Text(Localization.translate("msg_subject", lang)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("msg_subject_input_field"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = composerBody,
                        onValueChange = { composerBody = it },
                        label = { Text(Localization.translate("msg_body", lang)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("msg_body_input_field")
                    )

                    // Priority triggers selection
                    var pExp by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(Localization.translate("msg_priority", lang))
                        Box {
                            OutlinedButton(onClick = { pExp = true }) {
                                Text(composerPriority)
                            }
                            DropdownMenu(expanded = pExp, onDismissRequest = { pExp = false }) {
                                listOf("Normal", "Important", "Urgent").forEach { priority ->
                                    DropdownMenuItem(
                                        text = { Text(priority) },
                                        onClick = {
                                            composerPriority = priority
                                            pExp = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (dialogStatusMessage.isNotEmpty()) {
                        Text(dialogStatusMessage, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (composerToUser.isNotBlank() && composerSubject.isNotBlank() && composerBody.isNotBlank()) {
                            viewModel.sendInternalMessage(
                                trackerSubject = composerSubject,
                                bodyText = composerBody,
                                toUserId = composerToUser,
                                priority = composerPriority
                            ) { success, resultMsg ->
                                if (success) {
                                    showComposeDialog = false
                                } else {
                                    dialogStatusMessage = resultMsg
                                }
                            }
                        } else {
                            dialogStatusMessage = "All fields are required to establish transmission."
                        }
                    },
                    modifier = Modifier.testTag("msg_send_btn")
                ) {
                    Text(Localization.translate("send", lang))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        // Draft Auto-Save Trigger (Rule 78)
                        if (composerToUser.isNotEmpty() || composerSubject.isNotEmpty() || composerBody.isNotEmpty()) {
                            viewModel.saveDraftMessage(
                                toUserId = composerToUser,
                                subject = composerSubject,
                                body = composerBody,
                                priority = composerPriority
                            )
                        }
                        showComposeDialog = false
                    }
                ) {
                    Text("Save Draft & Close")
                }
            }
        )
    }
}
