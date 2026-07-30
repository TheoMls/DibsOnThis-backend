package com.theomls.dibs.validation

import com.theomls.dibs.models.*

object GroupValidator {
    
    private val INVITE_CODE_REGEX = "^[A-Z0-9]{${GroupConstants.INVITE_CODE_LENGTH}}$".toRegex()

    /**
     * Sanitizes and validates the [CreateGroupRequest].
     * Returns a [ValidationResult] containing either the sanitized request or an error message.
     */
    fun validateCreateGroup(request: CreateGroupRequest): ValidationResult<CreateGroupRequest> {
        val cleanName = request.name.trim()

        if (cleanName.isBlank()) {
            return ValidationResult.Failure("Group name cannot be blank.")
        }

        if (cleanName.length !in 3..20) {
            return ValidationResult.Failure("Group name must be between 3 and 20 characters.")
        }

        return ValidationResult.Success(
            request.copy(name = cleanName)
        )
    }

    /**
     * Sanitizes and validates the [JoinGroupRequest].
     * Returns a [ValidationResult] containing either the sanitized request or an error message.
     */
    fun validateJoinGroup(request: JoinGroupRequest): ValidationResult<JoinGroupRequest> {
        val cleanCode = request.inviteCode.trim().uppercase()

        if (!cleanCode.matches(INVITE_CODE_REGEX)) {
            return ValidationResult.Failure("Invalid invite code format.")
        }

        return ValidationResult.Success(
            request.copy(inviteCode = cleanCode)
        )
    }
}