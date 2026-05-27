package com.example.data.dao

import androidx.room.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Projects ---
    @Query("SELECT * FROM projects ORDER BY createdDate DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Int): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    // --- Users ---
    @Query("SELECT * FROM users ORDER BY createdDate DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    // --- Members ---
    @Query("SELECT * FROM members WHERE projectId = :projectId AND status != 'Removed' ORDER BY fullName ASC")
    fun getMembersForProject(projectId: Int): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE projectId = :projectId AND status != 'Removed' ORDER BY fullName ASC")
    suspend fun getMembersForProjectSync(projectId: Int): List<MemberEntity>

    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun getMemberById(id: Int): MemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity): Long

    @Update
    suspend fun updateMember(member: MemberEntity)

    // --- Shares ---
    @Query("SELECT * FROM shares WHERE projectId = :projectId")
    fun getSharesForProject(projectId: Int): Flow<List<ShareEntity>>

    @Query("SELECT * FROM shares WHERE projectId = :projectId")
    suspend fun getSharesForProjectSync(projectId: Int): List<ShareEntity>

    @Query("SELECT * FROM shares WHERE id = :id")
    suspend fun getShareById(id: Int): ShareEntity?

    @Query("SELECT * FROM shares WHERE projectId = :projectId AND memberId = :memberId LIMIT 1")
    suspend fun getShareByMember(projectId: Int, memberId: Int): ShareEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShare(share: ShareEntity): Long

    @Update
    suspend fun updateShare(share: ShareEntity)

    @Query("DELETE FROM shares WHERE id = :id")
    suspend fun deleteShare(id: Int)

    // --- Share History ---
    @Query("SELECT * FROM share_history WHERE projectId = :projectId ORDER BY transferDate DESC")
    fun getShareHistoryForProject(projectId: Int): Flow<List<ShareHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShareHistory(history: ShareHistoryEntity): Long

    // --- Requests & Votes ---
    @Query("SELECT * FROM requests ORDER BY createdDate DESC")
    fun getAllRequests(): Flow<List<RequestEntity>>

    @Query("SELECT * FROM requests WHERE projectId = :projectId ORDER BY createdDate DESC")
    fun getRequestsForProject(projectId: Int): Flow<List<RequestEntity>>

    @Query("SELECT * FROM requests WHERE id = :id")
    suspend fun getRequestById(id: Int): RequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: RequestEntity): Long

    @Update
    suspend fun updateRequest(request: RequestEntity)

    @Query("SELECT * FROM approval_votes WHERE requestId = :requestId")
    fun getVotesForRequest(requestId: Int): Flow<List<ApprovalVoteEntity>>

    @Query("SELECT * FROM approval_votes WHERE requestId = :requestId")
    suspend fun getVotesForRequestSync(requestId: Int): List<ApprovalVoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVote(vote: ApprovalVoteEntity): Long

    @Query("DELETE FROM approval_votes WHERE requestId = :requestId")
    suspend fun deleteVotesForRequest(requestId: Int)

    // --- Income ---
    @Query("SELECT * FROM income WHERE projectId = :projectId ORDER BY date DESC")
    fun getIncomeForProject(projectId: Int): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM income ORDER BY date DESC")
    fun getAllIncome(): Flow<List<IncomeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity): Long

    @Update
    suspend fun updateIncome(income: IncomeEntity)

    // --- Expenses ---
    @Query("SELECT * FROM expenses WHERE projectId = :projectId ORDER BY date DESC")
    fun getExpensesForProject(projectId: Int): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    // --- Ponds ---
    @Query("SELECT * FROM ponds WHERE projectId = :projectId ORDER BY pondNumber ASC")
    fun getPondsForProject(projectId: Int): Flow<List<PondEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPond(pond: PondEntity): Long

    // --- Fish Stock ---
    @Query("SELECT * FROM fish_stocks WHERE pondId = :pondId ORDER BY releaseDate DESC")
    fun getFishStocksForPond(pondId: Int): Flow<List<FishStockEntity>>

    @Query("SELECT * FROM fish_stocks ORDER BY releaseDate DESC")
    fun getAllFishStocks(): Flow<List<FishStockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFishStock(stock: FishStockEntity): Long

    @Update
    suspend fun updateFishStock(stock: FishStockEntity)

    // --- Feed Management ---
    @Query("SELECT * FROM feed_use WHERE pondId = :pondId ORDER BY date DESC")
    fun getFeedUseForPond(pondId: Int): Flow<List<FeedEntity>>

    @Query("SELECT * FROM feed_use ORDER BY date DESC")
    fun getAllFeedUse(): Flow<List<FeedEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedUse(feed: FeedEntity): Long

    // --- Notifications ---
    @Query("SELECT * FROM notifications WHERE (targetUsername IS NULL OR targetUsername = :username) AND (projectId IS NULL OR projectId = :projectId) ORDER BY timestamp DESC")
    fun getNotifications(username: String, projectId: Int?): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Int)

    // --- Audit Logs ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getAuditLogsForProject(projectId: Int): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity): Long

    // --- Internal Messaging System ---
    @Query("SELECT * FROM internal_messages ORDER BY sentTime DESC")
    fun getAllMessagesFlow(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM internal_messages WHERE (senderUserId = :userId OR receiverUserId = :userId) ORDER BY sentTime DESC")
    fun getMessagesForUser(userId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM internal_messages WHERE id = :id")
    suspend fun getMessageById(id: Int): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Update
    suspend fun updateMessage(message: MessageEntity)
}
