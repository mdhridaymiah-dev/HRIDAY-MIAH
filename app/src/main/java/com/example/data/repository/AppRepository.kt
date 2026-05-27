package com.example.data.repository

import com.example.data.dao.AppDao
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONObject

class AppRepository(private val appDao: AppDao) {

    // --- Projects ---
    val allProjects: Flow<List<ProjectEntity>> = appDao.getAllProjects()
    suspend fun getProjectById(id: Int): ProjectEntity? = appDao.getProjectById(id)
    suspend fun insertProject(project: ProjectEntity): Long = appDao.insertProject(project)
    suspend fun updateProject(project: ProjectEntity) = appDao.updateProject(project)

    // --- Users ---
    val allUsers: Flow<List<UserEntity>> = appDao.getAllUsers()
    suspend fun getUserById(id: Int): UserEntity? = appDao.getUserById(id)
    suspend fun getUserByUsername(username: String): UserEntity? = appDao.getUserByUsername(username)
    suspend fun insertUser(user: UserEntity): Long = appDao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = appDao.updateUser(user)

    // --- Members ---
    fun getMembersForProject(projectId: Int): Flow<List<MemberEntity>> = appDao.getMembersForProject(projectId)
    suspend fun getMembersForProjectSync(projectId: Int): List<MemberEntity> = appDao.getMembersForProjectSync(projectId)
    suspend fun getMemberById(id: Int): MemberEntity? = appDao.getMemberById(id)
    suspend fun insertMember(member: MemberEntity): Long = appDao.insertMember(member)
    suspend fun updateMember(member: MemberEntity) = appDao.updateMember(member)

    // --- Shares ---
    fun getSharesForProject(projectId: Int): Flow<List<ShareEntity>> = appDao.getSharesForProject(projectId)
    suspend fun getSharesForProjectSync(projectId: Int): List<ShareEntity> = appDao.getSharesForProjectSync(projectId)
    suspend fun insertShare(share: ShareEntity): Long = appDao.insertShare(share)
    suspend fun updateShare(share: ShareEntity) = appDao.updateShare(share)
    suspend fun deleteShare(id: Int) = appDao.deleteShare(id)

    // --- Share History ---
    fun getShareHistoryForProject(projectId: Int): Flow<List<ShareHistoryEntity>> = appDao.getShareHistoryForProject(projectId)
    suspend fun insertShareHistory(history: ShareHistoryEntity): Long = appDao.insertShareHistory(history)

    // --- Requests ---
    val allRequests: Flow<List<RequestEntity>> = appDao.getAllRequests()
    fun getRequestsForProject(projectId: Int): Flow<List<RequestEntity>> = appDao.getRequestsForProject(projectId)
    suspend fun getRequestById(id: Int): RequestEntity? = appDao.getRequestById(id)
    suspend fun insertRequest(request: RequestEntity): Long = appDao.insertRequest(request)
    suspend fun updateRequest(request: RequestEntity) = appDao.updateRequest(request)

    // --- Votes ---
    fun getVotesForRequest(requestId: Int): Flow<List<ApprovalVoteEntity>> = appDao.getVotesForRequest(requestId)
    suspend fun getVotesForRequestSync(requestId: Int): List<ApprovalVoteEntity> = appDao.getVotesForRequestSync(requestId)
    suspend fun insertVote(vote: ApprovalVoteEntity): Long = appDao.insertVote(vote)
    suspend fun deleteVotesForRequest(requestId: Int) = appDao.deleteVotesForRequest(requestId)

    // --- Monitoring & Ponds ---
    fun getPondsForProject(projectId: Int): Flow<List<PondEntity>> = appDao.getPondsForProject(projectId)
    suspend fun insertPond(pond: PondEntity): Long = appDao.insertPond(pond)

    fun getFishStocksForPond(pondId: Int): Flow<List<FishStockEntity>> = appDao.getFishStocksForPond(pondId)
    val allFishStocks: Flow<List<FishStockEntity>> = appDao.getAllFishStocks()
    suspend fun insertFishStock(stock: FishStockEntity): Long = appDao.insertFishStock(stock)
    suspend fun updateFishStock(stock: FishStockEntity) = appDao.updateFishStock(stock)

    fun getFeedUseForPond(pondId: Int): Flow<List<FeedEntity>> = appDao.getFeedUseForPond(pondId)
    val allFeedUse: Flow<List<FeedEntity>> = appDao.getAllFeedUse()
    suspend fun insertFeedUse(feed: FeedEntity): Long = appDao.insertFeedUse(feed)

    // --- Notifications ---
    fun getNotifications(username: String, projectId: Int?): Flow<List<NotificationEntity>> = appDao.getNotifications(username, projectId)
    suspend fun insertNotification(notification: NotificationEntity): Long = appDao.insertNotification(notification)
    suspend fun markNotificationAsRead(id: Int) = appDao.markNotificationAsRead(id)

    // --- Audit Logs ---
    val allAuditLogs: Flow<List<AuditLogEntity>> = appDao.getAllAuditLogs()
    fun getAuditLogsForProject(projectId: Int): Flow<List<AuditLogEntity>> = appDao.getAuditLogsForProject(projectId)
    suspend fun insertAuditLog(log: AuditLogEntity): Long = appDao.insertAuditLog(log)

    // --- Internal Messaging System ---
    val allMessagesFlow: Flow<List<MessageEntity>> = appDao.getAllMessagesFlow()
    fun getMessagesForUser(userId: String): Flow<List<MessageEntity>> = appDao.getMessagesForUser(userId)
    suspend fun getMessageById(id: Int): MessageEntity? = appDao.getMessageById(id)
    suspend fun insertMessage(message: MessageEntity): Long = appDao.insertMessage(message)
    suspend fun updateMessage(message: MessageEntity) = appDao.updateMessage(message)

    // --- Accounting Flows ---
    val allIncome: Flow<List<IncomeEntity>> = appDao.getAllIncome()
    val allExpenses: Flow<List<ExpenseEntity>> = appDao.getAllExpenses()

    fun getIncomeForProject(projectId: Int): Flow<List<IncomeEntity>> = appDao.getIncomeForProject(projectId)
    fun getExpensesForProject(projectId: Int): Flow<List<ExpenseEntity>> = appDao.getExpensesForProject(projectId)

    suspend fun insertIncomeDirect(income: IncomeEntity): Long = appDao.insertIncome(income)
    suspend fun insertExpenseDirect(expense: ExpenseEntity): Long = appDao.insertExpense(expense)

    // --- Transactional Smart Approval Staging Engine ---
    suspend fun evaluateAndProcessRequest(requestId: Int, finalStatus: String, actionUsername: String, rejectionReason: String? = null) {
        val request = appDao.getRequestById(requestId) ?: return
        val project = appDao.getProjectById(request.projectId) ?: return

        if (finalStatus == "Rejected") {
            val updatedRequest = request.copy(status = "Rejected", rejectionReason = rejectionReason)
            appDao.updateRequest(updatedRequest)

            // Audit Trail Logger
            appDao.insertAuditLog(
                AuditLogEntity(
                    username = actionUsername,
                    action = "Request Rejected",
                    module = getModuleForRequest(request.requestType),
                    projectId = request.projectId,
                    oldValue = "Pending",
                    newValue = "Rejected (Reason: ${rejectionReason ?: "N/A"})"
                )
            )

            // Notify request creator
            appDao.insertNotification(
                NotificationEntity(
                    projectId = request.projectId,
                    targetUsername = request.creatorUsername,
                    titleEn = "Request Rejected",
                    titleBn = "অনুরোধ বাতিল করা হয়েছে",
                    messageEn = "Your request of type '${request.requestType}' has been rejected.",
                    messageBn = "আপনার '${request.requestType}' টাইপের আবেদনটি বাতিল করা হয়েছে।"
                )
            )
            return
        }

        if (finalStatus == "Approved") {
            // Apply JSON Payload Changes securely to the Live Database
            val success = commitStagedPayload(request)
            if (success) {
                val updatedRequest = request.copy(status = "Approved")
                appDao.updateRequest(updatedRequest)

                // Log audit trail
                appDao.insertAuditLog(
                    AuditLogEntity(
                        username = actionUsername,
                        action = "Request Staging Committed",
                        module = getModuleForRequest(request.requestType),
                        projectId = request.projectId,
                        oldValue = "Pending Staged",
                        newValue = "Approved & Committed"
                    )
                )

                // Notify request creator
                appDao.insertNotification(
                    NotificationEntity(
                        projectId = request.projectId,
                        targetUsername = request.creatorUsername,
                        titleEn = "Request Approved",
                        titleBn = "অনুরোধ অনুমোদিত হয়েছে",
                        messageEn = "Your pending request has been approved and committed successfully.",
                        messageBn = "আপনার আবেদনটি অনুমোদিত হয়েছে এবং ডাটাবেজে যুক্ত করা হয়েছে।"
                    )
                )
            } else {
                // If parsing fails, fall back to Rejected
                val failedRequest = request.copy(status = "Rejected", rejectionReason = "Failed to commit payload schema")
                appDao.updateRequest(failedRequest)
            }
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

    private suspend fun commitStagedPayload(request: RequestEntity): Boolean {
        return try {
            val payload = JSONObject(request.payloadJson)
            when (request.requestType) {
                "MemberRemove" -> {
                    val memberId = payload.getInt("memberId")
                    val member = appDao.getMemberById(memberId)
                    if (member != null) {
                        appDao.updateMember(member.copy(status = "Removed"))

                        // Handle associated shares according to user's selections
                        val exitStrategy = payload.optString("exitStrategy", "Project Buyback")
                        val share = appDao.getShareByMember(request.projectId, memberId)
                        if (share != null && share.shareCount > 0) {
                            when (exitStrategy) {
                                "Project Buyback" -> {
                                    // Retained in system as company shares
                                    appDao.deleteShare(share.id)
                                    appDao.insertShareHistory(
                                        ShareHistoryEntity(
                                            projectId = request.projectId,
                                            previousMemberId = memberId,
                                            newMemberId = null,
                                            shareAmount = share.shareCount,
                                            purchaseValue = share.shareCount * share.shareValue,
                                            reason = "Project Buyback on Exit",
                                            requestId = request.id
                                        )
                                    )
                                }
                                "Existing Member Purchase" -> {
                                    val buyerMemberId = payload.getInt("buyerMemberId")
                                    val buyerShare = appDao.getShareByMember(request.projectId, buyerMemberId)
                                    if (buyerShare != null) {
                                        appDao.updateShare(buyerShare.copy(shareCount = buyerShare.shareCount + share.shareCount))
                                    } else {
                                        appDao.insertShare(
                                            ShareEntity(
                                                projectId = request.projectId,
                                                memberId = buyerMemberId,
                                                shareCount = share.shareCount,
                                                shareValue = share.shareValue
                                            )
                                        )
                                    }
                                    appDao.deleteShare(share.id)
                                    appDao.insertShareHistory(
                                        ShareHistoryEntity(
                                            projectId = request.projectId,
                                            previousMemberId = memberId,
                                            newMemberId = buyerMemberId,
                                            shareAmount = share.shareCount,
                                            purchaseValue = share.shareCount * share.shareValue,
                                            reason = "Internal Purchase - Shareholder Exit",
                                            requestId = request.id
                                        )
                                    )
                                }
                                "New Person Purchase" -> {
                                    val buyerName = payload.getString("buyerName")
                                    // Make new member
                                    val newMemberId = appDao.insertMember(
                                        MemberEntity(
                                            projectId = request.projectId,
                                            fullName = buyerName,
                                            mobile = payload.optString("buyerMobile", ""),
                                            email = payload.optString("buyerEmail", ""),
                                            status = "Active"
                                        )
                                    ).toInt()

                                    appDao.insertShare(
                                        ShareEntity(
                                            projectId = request.projectId,
                                            memberId = newMemberId,
                                            shareCount = share.shareCount,
                                            shareValue = share.shareValue
                                        )
                                    )
                                    appDao.deleteShare(share.id)

                                    appDao.insertShareHistory(
                                        ShareHistoryEntity(
                                            projectId = request.projectId,
                                            previousMemberId = memberId,
                                            newMemberId = newMemberId,
                                            shareAmount = share.shareCount,
                                            purchaseValue = share.shareCount * share.shareValue,
                                            reason = "New Member Onboard - Shareholder Exit",
                                            requestId = request.id
                                        )
                                    )
                                }
                            }
                        }
                    }
                    true
                }
                "ShareTransfer" -> {
                    val fromMemberId = payload.getInt("fromMemberId")
                    val toMemberId = payload.getInt("toMemberId")
                    val shareAmount = payload.getDouble("shareAmount")
                    val value = payload.getDouble("purchaseValue")

                    val sourceShare = appDao.getShareByMember(request.projectId, fromMemberId)
                    if (sourceShare != null && sourceShare.shareCount >= shareAmount) {
                        // Deduct from source
                        val remaining = sourceShare.shareCount - shareAmount
                        if (remaining <= 0) {
                            appDao.deleteShare(sourceShare.id)
                        } else {
                            appDao.updateShare(sourceShare.copy(shareCount = remaining))
                        }

                        // Add to target
                        val targetShare = appDao.getShareByMember(request.projectId, toMemberId)
                        if (targetShare != null) {
                            appDao.updateShare(targetShare.copy(shareCount = targetShare.shareCount + shareAmount))
                        } else {
                            appDao.insertShare(
                                ShareEntity(
                                    projectId = request.projectId,
                                    memberId = toMemberId,
                                    shareCount = shareAmount,
                                    shareValue = sourceShare.shareValue
                                )
                            )
                        }

                        // Write to share history
                        appDao.insertShareHistory(
                            ShareHistoryEntity(
                                projectId = request.projectId,
                                previousMemberId = fromMemberId,
                                newMemberId = toMemberId,
                                shareAmount = shareAmount,
                                purchaseValue = value,
                                reason = payload.optString("reason", "Standard Share Transfer"),
                                requestId = request.id
                            )
                        )
                        true
                    } else {
                        false
                    }
                }
                "ExpenseApproval" -> {
                    val category = payload.getString("category")
                    val amount = payload.getDouble("amount")
                    val date = payload.getLong("date")
                    val description = payload.getString("description")
                    val creator = payload.getString("creator")

                    appDao.insertExpense(
                        ExpenseEntity(
                            projectId = request.projectId,
                            category = category,
                            amount = amount,
                            date = date,
                            description = description,
                            creatorUsername = creator,
                            requestId = request.id
                        )
                    )
                    true
                }
                "IncomeEntry" -> {
                    val category = payload.getString("category")
                    val amount = payload.getDouble("amount")
                    val date = payload.getLong("date")
                    val description = payload.getString("description")
                    val creator = payload.getString("creator")

                    appDao.insertIncome(
                        IncomeEntity(
                            projectId = request.projectId,
                            category = category,
                            amount = amount,
                            date = date,
                            description = description,
                            creatorUsername = creator,
                            requestId = request.id
                        )
                    )
                    true
                }
                "RuleChange" -> {
                    val newMajority = payload.getInt("majorityPercent")
                    val project = appDao.getProjectById(request.projectId)
                    if (project != null) {
                        appDao.updateProject(project.copy(majorityApprovalPercent = newMajority))
                    }
                    true
                }
                else -> false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
