package com.theomls.dibs.validation

import com.theomls.dibs.models.CreateUserRequest

/**
 * Dedicated validator for User-related requests.
 */
object UserValidator {

    /**
     * Sanitizes and validates the [CreateUserRequest].
     * Returns a [ValidationResult] containing either the sanitized request or an error message.
     */
    fun validateCreateUser(request: CreateUserRequest): ValidationResult<CreateUserRequest> {
        val cleanName = request.displayName.trim()
        val cleanEmail = request.email.trim().lowercase()

        if (cleanName.length !in 2..50) {
            return ValidationResult.Failure("Name must be 2-50 characters")
        }

        if (!cleanEmail.contains("@")) {
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
