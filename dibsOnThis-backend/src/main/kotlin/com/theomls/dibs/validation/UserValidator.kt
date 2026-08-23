package com.theomls.dibs.validation

import com.theomls.dibs.models.*
import com.theomls.dibs.utils.ValidationUtils

object UserValidator {
    
    private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    /**
     * Sanitizes and validates the [CreateUserRequest].
     * Returns a [ValidationResult] containing either the sanitized request or an error message.
     */
    fun validateCreateUser(request: CreateUserRequest): ValidationResult<CreateUserRequest> {
        val nameResult = ValidationUtils.validateString(
            request.username,
            UserConstants.MIN_USERNAME_LENGTH,
            UserConstants.MAX_USERNAME_LENGTH,
            "Userame"
        )
        if (nameResult is ValidationResult.Failure) return nameResult

        val cleanEmail = request.email.trim().lowercase()

        if (!EMAIL_REGEX.matches(cleanEmail)) {
            return ValidationResult.Failure("Invalid email format")
        }

        return ValidationResult.Success(
            request.copy(
                username = (nameResult as ValidationResult.Success).value,
                email = cleanEmail
            )
        )
    }
}
