package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val logoUrl: String = "",
    val isActive: Boolean = true,
    val majorityApprovalPercent: Int = 51,
    val createdBy: String,
    val createdDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "", // Unique formatted identifier like SUPER-001 or PF001-ADMIN-01
    val username: String,
    val fullName: String,
    val mobileNumber: String,
    val email: String,
    val role: String, // "Super Admin", "Project Admin", "Member", "Auditor"
    val projectAssignmentId: Int? = null,
    val status: String = "Active", // "Active", "Suspended"
    val profileImage: String = "",
    val passwordHash: String,
    val isFirstLogin: Boolean = true,
    val failedLogins: Int = 0,
    val lockedUntil: Long? = null,
    val createdDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val userId: Int? = null,
    val fullName: String,
    val mobile: String,
    val email: String,
    val status: String = "Active", // "Active", "Suspended"
    val createdDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "shares")
data class ShareEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val memberId: Int,
    val shareCount: Double,
    val shareValue: Double,
    val createdDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "share_history")
data class ShareHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val previousMemberId: Int? = null,
    val newMemberId: Int? = null,
    val shareAmount: Double,
    val transferDate: Long = System.currentTimeMillis(),
    val purchaseValue: Double,
    val reason: String,
    val requestId: Int? = null
)

@Entity(tableName = "requests")
data class RequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val requestType: String, // "MemberRemove", "ShareTransfer", "ExpenseApproval", "IncomeEntry", "RuleChange"
    val creatorUsername: String,
    val createdDate: Long = System.currentTimeMillis(),
    val status: String = "Pending", // "Pending", "Approved", "Rejected"
    val approvalTypeNeeded: String, // "100% Approval", "Majority Approval"
    val reason: String,
    val payloadJson: String, // Contains JSON description of action to apply upon approval
    val rejectionReason: String? = null
)

@Entity(tableName = "approval_votes")
data class ApprovalVoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val requestId: Int,
    val voterUsername: String,
    val voteType: String, // "Approve", "Reject"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "income")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val category: String, // "Fish Sale", "Fry Sale", "Egg Sale", "Service Income", "Investment", "Other Income"
    val amount: Double,
    val date: Long,
    val description: String,
    val creatorUsername: String,
    val requestId: Int? = null,
    val createdTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val category: String, // "Feed", "Medicine", "Labor Salary", "Electricity", "Transport", "Pond Maintenance", "Equipment", "Water Treatment", "Miscellaneous"
    val amount: Double,
    val date: Long,
    val description: String,
    val receiptPath: String = "",
    val creatorUsername: String,
    val requestId: Int? = null,
    val createdTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "ponds")
data class PondEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val pondNumber: String,
    val sizeSqFt: Double,
    val waterCondition: String = "Good" // "Good", "Warning", "Alert"
)

@Entity(tableName = "fish_stocks")
data class FishStockEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pondId: Int,
    val species: String,
    val quantityStarted: Int,
    val quantityCurrent: Int,
    val releaseDate: Long,
    val avgWeightGm: Double,
    val mortalityCount: Int = 0,
    val growthRatePercentPerWeek: Double = 5.0,
    val isHarvested: Boolean = false
)

@Entity(tableName = "feed_use")
data class FeedEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pondId: Int,
    val feedType: String,
    val brand: String,
    val quantityKg: Double,
    val cost: Double,
    val dailyConsumptionKg: Double,
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int?,
    val targetUsername: String?, // Null means notification is for everyone in the project/system
    val titleEn: String,
    val titleBn: String,
    val messageEn: String,
    val messageBn: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val action: String, // "Login", "Logout", "Request Created", "Approved", "Rejected", "Password Change", etc.
    val module: String, // "Auth", "Shares", "Accounting", "Members", "Settings"
    val projectId: Int?,
    val timestamp: Long = System.currentTimeMillis(),
    val oldValue: String = "",
    val newValue: String = "",
    val device: String = "Android Device",
    val ipAddress: String = "127.0.0.1"
)

@Entity(tableName = "internal_messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderUserId: String,
    val receiverUserId: String,
    val subject: String,
    val body: String,
    val attachmentPath: String? = null,
    val priority: String = "Normal", // "Normal", "Important", "Urgent"
    val isRead: Boolean = false,
    val isStarred: Boolean = false,
    val isArchived: Boolean = false,
    val isDraft: Boolean = false,
    val isTrash: Boolean = false, // Soft deleted status
    val sentTime: Long = System.currentTimeMillis(),
    val readTime: Long? = null,
    val parentMessageId: Int? = null,
    val threadId: Int? = null,
    val senderDeleted: Boolean = false,
    val receiverDeleted: Boolean = false
)
