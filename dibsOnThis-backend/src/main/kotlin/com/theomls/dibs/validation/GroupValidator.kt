package com.theomls.dibs.validation

import com.theomls.dibs.models.*
import com.theomls.dibs.utils.ValidationUtils

object GroupValidator {
    
    private val INVITE_CODE_REGEX = "^[A-Z0-9]{${GroupConstants.INVITE_CODE_LENGTH}}$".toRegex()

    /**
     * Sanitizes and validates the [CreateGroupRequest].
     * Returns a [ValidationResult] containing either the sanitized request or an error message.
     */
    fun validateCreateGroup(request: CreateGroupRequest): ValidationResult<CreateGroupRequest> {
        val result = ValidationUtils.validateString(
            request.name,
            GroupConstants.MIN_NAME_LENGTH,
            GroupConstants.MAX_NAME_LENGTH,
            "Group name"
        )

        return when (result) {
            is ValidationResult.Success -> ValidationResult.Success(request.copy(name = result.value))
            is ValidationResult.Failure -> result
        }
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