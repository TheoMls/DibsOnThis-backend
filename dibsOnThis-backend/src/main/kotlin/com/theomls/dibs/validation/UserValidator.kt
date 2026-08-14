package com.theomls.dibs.validation

import com.theomls.dibs.models.*

object UserValidator {
    
    private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    /**
     * Sanitizes and validates the [CreateUserRequest].
     * Returns a [ValidationResult] containing either the sanitized request or an error message.
     */
    fun validateCreateUser(request: CreateUserRequest): ValidationResult<CreateUserRequest> {
        val cleanName = request.username.trim()
        val cleanEmail = request.email.trim().lowercase()

        val minUsernameLen = UserConstants.MIN_USERNAME_LENGTH
        val maxUsernameLen = UserConstants.MAX_USERNAME_LENGTH
        
        if (cleanName.length !in minUsernameLen..maxUsernameLen) {
            return ValidationResult.Failure("Name must be $minUsernameLen-$maxUsernameLen characters")
        }

        if (!EMAIL_REGEX.matches(cleanEmail)) {
            return ValidationResult.Failure("Invalid email format")
        }

        return ValidationResult.Success(
            request.copy(
                username = cleanName,
                email = cleanEmail
            )
        )
    }
}
