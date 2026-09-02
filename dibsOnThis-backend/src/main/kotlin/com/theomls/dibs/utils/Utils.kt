package com.theomls.dibs.utils

import com.theomls.dibs.auth.FirebaseUserPrincipal
import com.theomls.dibs.models.Group
import com.theomls.dibs.validation.ValidationResult
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal

// Auth Utilities
fun ApplicationCall.requireUserId(test: Boolean = true): String {
    if (test) return "test_user_1"
    return principal<FirebaseUserPrincipal>()?.uid
        ?: throw IllegalStateException("Unauthorized")
}

// Group Utilities
/**
 * Checks if the given user is a member of the specified group.
 * @throws IllegalArgumentException if the user is not a member.
 */
fun checkUserIsMemberOfGroup(userId: String, group: Group) {
    if (!group.memberIds.contains(userId))
        throw IllegalArgumentException("User is not a member of this group")
}

// ID Generation Utilities
const val UNIQUE_GENERATION_MAX_ATTEMPTS = 50

suspend fun generateUnique(
    generator: () -> String,
    existsChecker: suspend (String) -> Boolean,
    whatUnique: String
): String {
    var code: String
    var exists: Boolean
    var tries = 0
    do {
        code = generator()
        exists = existsChecker(code)
        tries++
        if (tries >= UNIQUE_GENERATION_MAX_ATTEMPTS)
            throw IllegalStateException("Failed to generate unique $whatUnique after $tries attempts.")
    } while (exists)
    return code
}

// Validation Utilities
fun validateString(str: String, minLen: Int, maxLen: Int, fieldName: String): ValidationResult<String> {
    val cleanStr = str.trim()

    if (cleanStr.length !in minLen..maxLen)
        return ValidationResult.Failure("$fieldName must be between $minLen and $maxLen characters.")

    return ValidationResult.Success(cleanStr)
}
