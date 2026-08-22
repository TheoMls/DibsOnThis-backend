package com.theomls.dibs.validation

import com.theomls.dibs.models.CreateTaskRequest
import com.theomls.dibs.models.TaskConstants

object TaskValidator {
    
    fun validateCreateTask(request: CreateTaskRequest): ValidationResult<CreateTaskRequest> {
        val titleResult = ValidationUtils.validateString(
            request.title,
            TaskConstants.MIN_TITLE_LENGTH,
            TaskConstants.MAX_TITLE_LENGTH,
            "Task title"
        )

        if (titleResult is ValidationResult.Failure) return titleResult
        
        val descriptionResult = ValidationUtils.validateString(
            request.description,
            0, TaskConstants.MAX_DESCRIPTION_LENGTH,
            "Task description"
        )
        if (descriptionResult is ValidationResult.Failure) return descriptionResult

        return ValidationResult.Success(
            request.copy(
                title = (titleResult as ValidationResult.Success).value,
                description = (descriptionResult as ValidationResult.Success).value
            )
        )
    }
}