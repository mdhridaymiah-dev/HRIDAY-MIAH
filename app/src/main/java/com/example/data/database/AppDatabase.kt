package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AppDao
import com.example.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProjectEntity::class,
        UserEntity::class,
        MemberEntity::class,
        ShareEntity::class,
        ShareHistoryEntity::class,
        RequestEntity::class,
        ApprovalVoteEntity::class,
        IncomeEntity::class,
        ExpenseEntity::class,
        PondEntity::class,
        FishStockEntity::class,
        FeedEntity::class,
        NotificationEntity::class,
        AuditLogEntity::class,
        MessageEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fish_farm_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.appDao())
                }
            }
        }

        suspend fun populateDatabase(dao: AppDao) {
            // Seed base projects
            val project1Id = dao.insertProject(
                ProjectEntity(
                    name = "Bismillah Fish Culture",
                    logoUrl = "ic_launcher_foreground",
                    isActive = true,
                    majorityApprovalPercent = 60,
                    createdBy = "admin"
                )
            ).toInt()

            val project2Id = dao.insertProject(
                ProjectEntity(
                    name = "Sonar Bangla Aqua Farm",
                    logoUrl = "ic_launcher_foreground",
                    isActive = true,
                    majorityApprovalPercent = 50,
                    createdBy = "admin"
                )
            ).toInt()

            // Seed Users with default SHA-256 equivalent passwords
            // We store passwords hashed or authenticated securely. Let's use simple standard hash representations.
            // "admin123" -> hashed
            // Let's seed users:
            // 1. Super Admin: username = admin, password = 11
            // 2. Project Admin: username = pm1, password = PM@12345
            // 3. Member: username = member1, password = Member@1
            // 4. Auditor: username = auditor1, password = Auditor@1
            dao.insertUser(
                UserEntity(
                    userId = "SUPER-001",
                    username = "admin",
                    fullName = "Super Admin Md. Hriday",
                    mobileNumber = "+8801700000001",
                    email = "admin@fishfarm.com",
                    role = "Super Admin",
                    projectAssignmentId = null,
                    status = "Active",
                    passwordHash = com.example.ui.SecurityUtils.hashPassword("11"), // Seeded with initial setup password "11"
                    isFirstLogin = true // First login rule!
                )
            )

            dao.insertUser(
                UserEntity(
                    userId = "ADMIN-001",
                    username = "pm1",
                    fullName = "Project Manager Rahman",
                    mobileNumber = "+8801700000002",
                    email = "pm1@fishfarm.com",
                    role = "Project Admin",
                    projectAssignmentId = project1Id,
                    status = "Active",
                    passwordHash = com.example.ui.SecurityUtils.hashPassword("Pm@123456"),
                    isFirstLogin = false
                )
            )

            dao.insertUser(
                UserEntity(
                    userId = "MEM-001",
                    username = "member1",
                    fullName = "Shareholder Kalam Chowdhury",
                    mobileNumber = "+8801700000003",
                    email = "kalam@fishfarm.com",
                    role = "Member",
                    projectAssignmentId = project1Id,
                    status = "Active",
                    passwordHash = com.example.ui.SecurityUtils.hashPassword("Member@12"),
                    isFirstLogin = false
                )
            )

            dao.insertUser(
                UserEntity(
                    userId = "MEM-002",
                    username = "member2",
                    fullName = "Shareholder Jalal Uddin",
                    mobileNumber = "+8801700000004",
                    email = "jalal@fishfarm.com",
                    role = "Member",
                    projectAssignmentId = project1Id,
                    status = "Active",
                    passwordHash = com.example.ui.SecurityUtils.hashPassword("Member@34"),
                    isFirstLogin = false
                )
            )

            dao.insertUser(
                UserEntity(
                    userId = "AUD-001",
                    username = "auditor1",
                    fullName = "External Auditor Shafiq",
                    mobileNumber = "+8801700000005",
                    email = "auditor@fishfarm.com",
                    role = "Auditor",
                    projectAssignmentId = null,
                    status = "Active",
                    passwordHash = com.example.ui.SecurityUtils.hashPassword("Auditor@1"),
                    isFirstLogin = false
                )
            )

            // Seed standard members
            val mem1Id = dao.insertMember(
                MemberEntity(
                    projectId = project1Id,
                    fullName = "Kalam Chowdhury",
                    mobile = "+8801700000003",
                    email = "kalam@fishfarm.com",
                    status = "Active"
                )
            ).toInt()

            val mem2Id = dao.insertMember(
                MemberEntity(
                    projectId = project1Id,
                    fullName = "Jalal Uddin",
                    mobile = "+8801700000004",
                    email = "jalal@fishfarm.com",
                    status = "Active"
                )
            ).toInt()

            val mem3Id = dao.insertMember(
                MemberEntity(
                    projectId = project1Id,
                    fullName = "Rahman Malik",
                    mobile = "+8801700000002",
                    email = "pm1@fishfarm.com",
                    status = "Active"
                )
            ).toInt()

            // Seed shares
            dao.insertShare(ShareEntity(projectId = project1Id, memberId = mem1Id, shareCount = 40.0, shareValue = 1000.0))
            dao.insertShare(ShareEntity(projectId = project1Id, memberId = mem2Id, shareCount = 45.0, shareValue = 1000.0))
            dao.insertShare(ShareEntity(projectId = project1Id, memberId = mem3Id, shareCount = 15.0, shareValue = 1000.0))

            // Seed share history
            dao.insertShareHistory(
                ShareHistoryEntity(
                    projectId = project1Id,
                    newMemberId = mem1Id,
                    shareAmount = 40.0,
                    purchaseValue = 40000.0,
                    reason = "Initial Share Issue"
                )
            )
            dao.insertShareHistory(
                ShareHistoryEntity(
                    projectId = project1Id,
                    newMemberId = mem2Id,
                    shareAmount = 45.0,
                    purchaseValue = 45000.0,
                    reason = "Initial Share Issue"
                )
            )
            dao.insertShareHistory(
                ShareHistoryEntity(
                    projectId = project1Id,
                    newMemberId = mem3Id,
                    shareAmount = 15.0,
                    purchaseValue = 15000.0,
                    reason = "Initial Share Issue"
                )
            )

            // Seed Ponds
            val pond1Id = dao.insertPond(PondEntity(projectId = project1Id, pondNumber = "Pond-01", sizeSqFt = 12000.0, waterCondition = "Good")).toInt()
            val pond2Id = dao.insertPond(PondEntity(projectId = project1Id, pondNumber = "Pond-02", sizeSqFt = 8500.0, waterCondition = "Warning")).toInt()

            // Seed Fish Stocks
            val stock1Id = dao.insertFishStock(
                FishStockEntity(
                    pondId = pond1Id,
                    species = "Rui (Carpet)",
                    quantityStarted = 5000,
                    quantityCurrent = 4920,
                    releaseDate = System.currentTimeMillis() - 60L * 24 * 60 * 60 * 1000, // 60 days ago
                    avgWeightGm = 420.0,
                    mortalityCount = 80,
                    growthRatePercentPerWeek = 4.8
                )
            ).toInt()

            val stock2Id = dao.insertFishStock(
                FishStockEntity(
                    pondId = pond2Id,
                    species = "Tilapia (Monosex)",
                    quantityStarted = 8000,
                    quantityCurrent = 7850,
                    releaseDate = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000, // 30 days ago
                    avgWeightGm = 180.0,
                    mortalityCount = 150,
                    growthRatePercentPerWeek = 6.2
                )
            ).toInt()

            // Seed Feed Usage
            dao.insertFeedUse(FeedEntity(pondId = pond1Id, feedType = "Floating Feed", brand = "Mega Feed", quantityKg = 50.0, cost = 3200.0, dailyConsumptionKg = 25.0))
            dao.insertFeedUse(FeedEntity(pondId = pond2Id, feedType = "Sinking Feed", brand = "CP Feed", quantityKg = 40.0, cost = 2400.0, dailyConsumptionKg = 18.0))

            // Seed some active accounting transactions
            dao.insertIncome(
                IncomeEntity(
                    projectId = project1Id,
                    category = "Investment",
                    amount = 100000.0,
                    date = System.currentTimeMillis() - 70L * 24 * 60 * 60 * 1000,
                    description = "Initial Share Capital Raised",
                    creatorUsername = "admin"
                )
            )

            dao.insertIncome(
                IncomeEntity(
                    projectId = project1Id,
                    category = "Fish Sale",
                    amount = 45000.0,
                    date = System.currentTimeMillis() - 10L * 24 * 60 * 60 * 1000,
                    description = "Partial harvest sale from Pond 1 (Rui Fish)",
                    creatorUsername = "pm1"
                )
            )

            dao.insertExpense(
                ExpenseEntity(
                    projectId = project1Id,
                    category = "Feed",
                    amount = 12500.0,
                    date = System.currentTimeMillis() - 40L * 24 * 60 * 60 * 1000,
                    description = "Purchase of 5 sacks of Mega Floating Feed",
                    creatorUsername = "pm1"
                )
            )

            dao.insertExpense(
                ExpenseEntity(
                    projectId = project1Id,
                    category = "Medicine",
                    amount = 3500.0,
                    date = System.currentTimeMillis() - 25L * 24 * 60 * 60 * 1000,
                    description = "Aqua Oxygen Tablets & Lime water treatment",
                    creatorUsername = "pm1"
                )
            )

            dao.insertExpense(
                ExpenseEntity(
                    projectId = project1Id,
                    category = "Labor Salary",
                    amount = 15000.0,
                    date = System.currentTimeMillis() - 5L * 24 * 60 * 60 * 1000,
                    description = "Monthly wages for Pond assistant staff",
                    creatorUsername = "pm1"
                )
            )

            // Seed Notifications
            dao.insertNotification(
                NotificationEntity(
                    projectId = project1Id,
                    targetUsername = null,
                    titleEn = "New Financial Approvals Setup",
                    titleBn = "নতুন ফিন্যান্সিয়াল অনুমোদন সেটআপ",
                    messageEn = "Expense approvals threshold established based on transaction size.",
                    messageBn = "লেনদেনের হারের ওপর ভিত্তি করে খরচ অনুমোদনের স্তর স্থাপন করা হয়েছে।"
                )
            )

            // Seed Audit Logs
            dao.insertAuditLog(
                AuditLogEntity(
                    username = "system",
                    action = "System Database Initialized",
                    module = "Auth",
                    projectId = null,
                    oldValue = "",
                    newValue = "DB Seeding Complete"
                )
            )
        }
    }
}
