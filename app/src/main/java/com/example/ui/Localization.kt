package com.example.ui

object Localization {

    enum class Language { EN, BN }

    private val translations = mapOf(
        "app_title" to Pair("Fish Farm Management", "মৎস্য খামার ব্যবস্থাপনা"),
        "dashboard" to Pair("Dashboard", "ড্যাশবোর্ড"),
        "accounting" to Pair("Accounting", "হিসাব কিতাব"),
        "members" to Pair("Members & Shares", "সদস্য ও শেয়ার"),
        "monitoring" to Pair("Ponds & Stocks", "পুকুর ও স্টক"),
        "approvals" to Pair("Approvals", "অনুমোদন সমূহ"),
        "profile" to Pair("Profile", "প্রোফাইল"),
        "switch_lang" to Pair("বাংলায় দেখুন", "View in English"),
        "active_project" to Pair("Active Project", "সক্রিয় প্রজেক্ট"),
        "no_project" to Pair("Global (No Project Assigned)", "গ্লোবাল (কোন প্রজেক্ট বরাদ্দ নেই)"),
        "super_admin_panel" to Pair("Super Admin Panel", "সুপার এডমিন প্যানেল"),
        "system_audit_logs" to Pair("System Audit Logs", "সিস্টেম অডিট লগ"),
        "search_hint" to Pair("Global search...", "গ্লোবাল সার্চ..."),
        
        // Login & Auth
        "username" to Pair("Username", "ইউজারনেম"),
        "password" to Pair("Password", "পাসওয়ার্ড"),
        "login" to Pair("Login", "লগইন"),
        "logout" to Pair("Logout", "লগআউট"),
        "username_req" to Pair("Username is required", "ইউজারনেম আবশ্যক"),
        "password_req" to Pair("Password is required", "পাসওয়ার্ড আবশ্যক"),
        "invalid_credentials" to Pair("Invalid username or password", "ভুল ইউজারনেম অথবা পাসওয়ার্ড"),
        "wrong_password_try" to Pair("Incorrect Password. Please enter the correct password.", "পাসওয়ার্ড ভুল হয়েছে। অনুগ্রহ করে সঠিক পাসওয়ার্ড দিন।"),
        "default_pass_deactivated" to Pair("Default password '11' has been disabled and cannot be reused", "ডিফল্ট পাসওয়ার্ড '11' নিষ্ক্রিয় করা হয়েছে এবং পুনরায় ব্যবহার করা যাবে না"),
        "account_locked" to Pair("Account loaded or temporarily locked due to multiple failed login attempts.", "একাধিকবার ভুল চেষ্টার কারণে অ্যাকাউন্টটি সাময়িকভাবে লক করা হয়েছে।"),
        "forgot_pwd" to Pair("Forgot Password?", "পাসওয়ার্ড ভুলে গেছেন?"),
        "reset_pwd" to Pair("Reset Password", "পাসওয়ার্ড রিসেট"),
        
        // Force Password Reset
        "force_pwd_title" to Pair("First Login - Force Password Change", "প্রথম লগইন - বাধ্যতামূলক পাসওয়ার্ড পরিবর্তন"),
        "force_pwd_desc" to Pair("For security, you must configure a new unique password before accessing the system dashboards.", "নিরাপত্তার স্বার্থে ক্রিয়াকলাপের পূর্বে আপনাকে একটি নতুন পাসওয়ার্ড সেট করতে হবে।"),
        "current_pwd" to Pair("Current Password", "বর্তমান পাসওয়ার্ড"),
        "new_pwd" to Pair("New Password", "নতুন পাসওয়ার্ড"),
        "confirm_pwd" to Pair("Confirm Password", "পাসওয়ার্ড নিশ্চিত করুন"),
        "pwd_rules" to Pair("At least 8 chars, 1 Uppercase, 1 Lowercase, 1 Number, 1 Special character", "কমপক্ষে ৮টি অক্ষর, ১টি বড় হাত, ১টি ছোট হাত, ১টি সংখ্যা ও ১টি বিশেষ চিহ্ন"),
        "save_changes" to Pair("Save Changes", "পরিবর্তন সংরক্ষণ করুন"),
        "pwd_mismatch" to Pair("Passwords do not match", "পাসওয়ার্ড দুটি মেলেনি"),
        "pwd_weak" to Pair("Password does not meet the security policy", "পাসওয়ার্ডটি নিরাপত্তা নীতি পূরণ করে না"),
        "pwd_strength" to Pair("Password Strength", "পাসওয়ার্ডের শক্তি"),

        // Forgot Password Recovery Screen
        "forgot_title" to Pair("Forgot Password Recovery", "পাসওয়ার্ড পুনরুদ্ধার"),
        "recovery_desc" to Pair("Provide register credentials to receive mobile OTP and email secure codes.", "নিবন্ধন মোবাইল ও ইমেইলে ওটিপি পেতে সঠিক তথ্য প্রদান করুন।"),
        "reg_mobile" to Pair("Registered Mobile", "নিবন্ধিত মোবাইল নম্বর"),
        "reg_email" to Pair("Registered Email", "নিবন্ধিত ইমেইল"),
        "send_codes" to Pair("Send Verification Codes", "ভেরিফিকেশন কোড পাঠান"),
        "enter_otp_mobile" to Pair("Mobile OTP Code", "মোবাইল ওটিপি কোড"),
        "enter_vcode_email" to Pair("Email Verification Code", "ইমেইল ভেরিফিকেশন কোড"),
        "verify_allow_reset" to Pair("Verify & Reset Password", "যাচাই করুন ও পাসওয়ার্ড রিসেট করুন"),
        "attempts_left" to Pair("Attempts remaining: ", "অবশিষ্ট চেষ্টা: "),

        // Profile Management
        "profile_details" to Pair("Profile Details", "প্রোফাইলের বিবরণ"),
        "fullname" to Pair("Full Name", "পূর্ণ নাম"),
        "mobile_no" to Pair("Mobile Number", "মোবাইল নাম্বার"),
        "email_id" to Pair("Email ID", "ইমেইল আইডি"),
        "role" to Pair("Role", "ভূমিকা"),
        "status" to Pair("Status", "অবস্থা"),
        "active_sessions" to Pair("Active Devices / Sessions", "সক্রিয় সেশন / ডিভাইস"),
        "this_device" to Pair("This Android device (Active)", "এই অ্যান্ড্রয়েড ডিভাইস (সক্রিয়)"),
        "logout_others" to Pair("Terminate All Other Sessions", "অন্যান্য সকল সেশন বন্ধ করুন"),
        
        // Project Control (Super Admin)
        "create_project" to Pair("Create New Project / Farm", "নতুন প্রজেক্ট / খামার তৈরি করুন"),
        "project_name" to Pair("Project / Farm Name", "প্রজেক্ট / খামারের নাম"),
        "upload_logo" to Pair("Logo Upload (PNG/JPG/SVG)", "লোগো আপলোড (PNG/JPG/SVG)"),
        "ownership_config" to Pair("Ownership Threshold Settings", "মালিকানা ও অনুমোদন সেটিংস"),
        "majority_approval_req" to Pair("Majority Approval Percent Requirement", "সংখ্যাগরিষ্ঠতা অর্জনের শতকরা হার"),
        "activate" to Pair("Activate", "সক্রিয় করুন"),
        "deactivate" to Pair("Deactivate", "নিষ্ক্রিয় করুন"),
        "all_projects_hdr" to Pair("All Enterprise Projects", "সকল নিবন্ধিত প্রজেক্টসমূহ"),
        
        // Dashboard
        "total_balance" to Pair("Total Ledger Balance", "মোট লেজার ব্যালেন্স"),
        "total_income" to Pair("Total Income", "মোট জমা"),
        "total_expense" to Pair("Total Expenses", "মোট খরচ"),
        "net_profit" to Pair("Unallocated Net Profit", "বন্টনযোগ্য নিট মুনাফা"),
        "pending_req" to Pair("Pending Approvals", "অনুমোদন অপেক্ষমাণ"),
        "member_count" to Pair("Total Shareholders", "মোট শেয়ারহোল্ডার সংখ্যা"),
        "fish_summary" to Pair("Fish Stock Summary", "মাছের মজুদ সারসংক্ষেপ"),
        "recent_activities" to Pair("Recent Farm Activities", "সাম্প্রতিক কর্মকাণ্ডসমূহ"),
        "financial_flow" to Pair("Financial Flow Analytics", "আর্থিক প্রবাহ বিশ্লেষণ"),
        "no_data" to Pair("No data available.", "কোন তথ্য পাওয়া যায়নি।"),
        "currency_symbol" to Pair("৳", "৳"),
        "filter" to Pair("Filter", "ফিল্টার"),
        "apply" to Pair("Apply", "প্রয়োগ করুন"),
        "clear" to Pair("Clear", "মুছে ফেলুন"),

        // Member Management & Exit Workflow
        "sh_capital" to Pair("Share Capital Structure", "শেয়ার মূলধন কাঠামো"),
        "sh_history_hdr" to Pair("Share Ownership History Log", "শেয়ার মালিকানা স্থানান্তরের ইতিহাস"),
        "onboard_sh" to Pair("Onboard Shareholder / Member", "অংশীদার / সদস্য যুক্ত করুন"),
        "transfer_sh" to Pair("Transfer Shares", "শেয়ার স্থানান্তর করুন"),
        "exit_sh" to Pair("Process Member Exit / Removal", "মালিক সস্থানান্তর ও অংশীদার বিদায়"),
        "exit_strategy" to Pair("Select Exit Share Option", "স্থানান্তর বিকল্প নির্বাচন করুন"),
        "rollback_note" to Pair("A formal request will be created. Not effective until approved.", "একটি অনুমোদনের আবেদন তৈরি হবে। চূড়ান্ত সিদ্ধান্ত ছাড়া কার্যকর হবে না।"),
        "prev_owner" to Pair("Previous Owner", "পূর্ববর্তী মালিক"),
        "new_owner" to Pair("New Owner", "নতুন মালিক"),
        "share_count" to Pair("Number of Shares", "শেয়ারের সঙ্খ্যা"),
        "share_value" to Pair("Face Value of Share", "শেয়ারের আসল মূল্য"),
        "share_pct" to Pair("Holding Portion", "মালিকানার হার"),
        "buyback" to Pair("Project Buyback / Redempt", "প্রজেক্ট বাইব্যাক"),
        "exist_member_buy" to Pair("Existing Member Purchase", "বিদ্যমান অংশীদার কর্তৃক ক্রায়"),
        "new_person_buy" to Pair("New Person Purchase", "নতুন অংশীদার আগমন"),
        
        // Accounting
        "inc_cat" to Pair("Income Classification", "জমার ধরণ শ্রেণী"),
        "exp_cat" to Pair("Expense Classification", "খরচের ধরণ শ্রেণী"),
        "amount" to Pair("Amount (BDT)", "টাকা পরিমাণ"),
        "desc" to Pair("Description", "বিবরণ"),
        "receipt" to Pair("Receipt Image / Document", "রশিদ / প্রমাণপত্রক"),
        "add_income" to Pair("Record Income", "জমা যুক্ত করুন"),
        "add_expense" to Pair("Request Expense Approval", "খরচের অনুমোদনের আবেদন"),
        "profit_dist" to Pair("Dividend Distribution Solver", "ডিভিডেন্ড বন্টন ক্যালকুলেটর"),
        "distribute" to Pair("Generate Share-based Dividends", "শেয়ার ভিত্তিক লভ্যাংশ হিসাব করুন"),
        "dist_period" to Pair("Distribution Period", "বন্টন সময়কাল"),
        "dist_amount" to Pair("Total Dividend Pool", "মোট লভ্যাংশ বরাদ্দ"),
        "dividend_payout_table" to Pair("Calculated Dividend Layout", "লভ্যাংশ বন্টনের তালিকা"),

        // Monitoring
        "pond_no" to Pair("Pond Designation", "পুকুরের পরিচিতি"),
        "pond_sz" to Pair("Pond Surface Area (sq.ft)", "পুকুরের আয়তন (বর্গফুট)"),
        "water_lvl" to Pair("Water Condition (pH / DO)", "পানির অবস্থা"),
        "add_pond" to Pair("Add New Pond Layout", "নতুন পুকুর যোগ করুন"),
        "stocking_details" to Pair("Fish Stocking Details", "মাছ স্টক করার বিবরণ"),
        "fish_species" to Pair("Fish Breed Species", "মাছের প্রজাতি"),
        "qty_stocked" to Pair("Quantity Released", "পোনা ছাড়ার সংখ্যা"),
        "cur_qty" to Pair("Stock Density", "বর্তমান পোনা সংখ্যা"),
        "avg_wt" to Pair("Average Specimen Weight (gm)", "গড় ওজন (গ্রাম)"),
        "mortality" to Pair("Mortality Incidents", "মৃত্যুর হার সংখ্যা"),
        "feed_used" to Pair("Feed Registry & Brand", "ব্যবহৃত ফিডের বিবরণ ও ব্র্যান্ড"),
        "feed_cost" to Pair("Feed Expenditure", "ফিড খরচ"),
        "daily_feed" to Pair("Daily Intake (kg)", "দৈনিক সংস্থান (কেজি)"),
        "growth_forecast" to Pair("Growth Forecast Analysis", "বৃদ্ধি ও উৎপাদন পূর্বাভাস"),
        
        // Approvals Staging Screen
        "staging_queue" to Pair("Approval Queue & Staging Block", "অনুমোদন অপেক্ষমাণ এবং স্টেজিং ব্লক"),
        "vote_count" to Pair("Votes Recorded", "গৃহীত ভোট"),
        "approve" to Pair("Approve", "অনুমোদন করুন"),
        "reject" to Pair("Reject", "প্রত্যাখ্যান করুন"),
        "required_rule" to Pair("Requires: ", "প্রয়োজনীয় শর্ত: "),
        "rejection_reason_lbl" to Pair("Rejection Reason Required", "প্রত্যাখ্যানের সুনির্দিষ্ট কারণ দিন"),
        "no_pending_req" to Pair("No pending staging approvals found.", "কোন স্টেজিং অনুমোদন পেন্ডিং নেই।"),
        "messages" to Pair("Internal Messaging", "অভ্যন্তরীণ বার্তা"),
        "inbox" to Pair("Inbox", "ইনবক্স"),
        "sent" to Pair("Sent", "প্রেরিত"),
        "drafts" to Pair("Drafts", "খসড়া"),
        "trash" to Pair("Trash / Archive", "আর্কাইভ / আবর্জনা"),
        "compose" to Pair("Compose Message", "বার্তা লিখুন"),
        "msg_subject" to Pair("Subject", "বিষয়"),
        "msg_body" to Pair("Message Body", "বার্তার বিবরণ"),
        "msg_priority" to Pair("Priority Level", "অগ্রাধিকার স্তর"),
        "msg_to" to Pair("Receiver User ID", "প্রাপক ইউজার আইডি"),
        "send" to Pair("Send Message", "বার্তা পাঠান")
    )

    fun translate(key: String, lang: Language): String {
        val entry = translations[key] ?: return key
        return if (lang == Language.EN) entry.first else entry.second
    }
}
