package com.theomls.dibs.validation

import com.theomls.dibs.models.CreateUserRequest

object UserValidator {
    
    private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    /**
     * Sanitizes and validates the [CreateUserRequest].
     * Returns a [ValidationResult] containing either the sanitized request or an error message.
     */
    fun validateCreateUser(request: CreateUserRequest): ValidationResult<CreateUserRequest> {
        val cleanName = request.displayName.trim()
        val cleanEmail = request.email.trim().lowercase()

        if (cleanName.length !in 3..20) {
            return ValidationResult.Failure("Name must be 3-20 characters")
        }

        if (!EMAIL_REGEX.matches(cleanEmail)) {
            return ValidationResult.Failure("Invalid email format")
        }

        return ValidationResult.Success(
            request.copy(
                displayName = cleanName,
                email = cleanEmail
            )
        )
    }
}
