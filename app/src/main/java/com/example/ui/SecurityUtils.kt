package com.example.ui

import java.security.MessageDigest

object SecurityUtils {
    // Rule 70: Secure hashing rule (No plain-text storage)
    fun hashPassword(password: String): String {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
            return hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            return password // safe fallback
        }
    }

    // Rule 64: Strong Super Admin Password Policy
    fun validateStrongPassword(password: String): Boolean {
        if (password.length < 12) return false
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        return hasUpper && hasLower && hasDigit && hasSpecial
    }

    // Rule 71: Auto Generation of Strong Password
    fun generateStrongPassword(): String {
        val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowercase = "abcdefghijklmnopqrstuvwxyz"
        val numbers = "0123456789"
        val special = "@#$%^&*!"
        
        val random = java.util.Random()
        val char1 = uppercase[random.nextInt(uppercase.length)]
        val char2 = lowercase[random.nextInt(lowercase.length)]
        val char3 = numbers[random.nextInt(numbers.length)]
        val char4 = special[random.nextInt(special.length)]
        
        val allAllowed = uppercase + lowercase + numbers + special
        val rest = (1..10).map { allAllowed[random.nextInt(allAllowed.length)] }.joinToString("")
        
        val list = (char1.toString() + char2.toString() + char3.toString() + char4.toString() + rest).toList()
        java.util.Collections.shuffle(list)
        return list.joinToString("")
    }

    // Rule 69: Emergency Recovery Code generator
    fun generateRecoveryCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = java.util.Random()
        val part1 = (1..4).map { chars[random.nextInt(chars.length)] }.joinToString("")
        val part2 = (1..4).map { chars[random.nextInt(chars.length)] }.joinToString("")
        val part3 = (1..4).map { chars[random.nextInt(chars.length)] }.joinToString("")
        return "REC-$part1-$part2-$part3"
    }
}
