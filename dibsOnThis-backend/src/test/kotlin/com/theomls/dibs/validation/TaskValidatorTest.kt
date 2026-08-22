package com.theomls.dibs.validation

import com.theomls.dibs.models.CreateTaskRequest
import kotlin.test.*

class TaskValidatorTest {

    @Test
    fun `validateCreateTask should succeed for valid request`() {
        val request = CreateTaskRequest(title = "Valid Title", description = "Valid Description")
        val result = TaskValidator.validateCreateTask(request)
        
        assertTrue(result is ValidationResult.Success)
        assertEquals("Valid Title", result.value.title)
        assertEquals("Valid Description", result.value.description)
    }

    @Test
    fun `validateCreateTask should succeed for empty description`() {
        val request = CreateTaskRequest(title = "Valid Title", description = "")
        val result = TaskValidator.validateCreateTask(request)
        
        assertTrue(result is ValidationResult.Success)
        assertEquals("", result.value.description)
    }

    @Test
    fun `validateCreateTask should fail for short title`() {
        val request = CreateTaskRequest(title = "Ab", description = "")
        val result = TaskValidator.validateCreateTask(request)
        
        assertTrue(result is ValidationResult.Failure)
        assertEquals("Task title must be between 3 and 50 characters.", result.message)
    }

    @Test
    fun `validateCreateTask should fail for long title`() {
        val request = CreateTaskRequest(title = "a".repeat(51), description = "")
        val result = TaskValidator.validateCreateTask(request)
        
        assertTrue(result is ValidationResult.Failure)
    }

    @Test
    fun `validateCreateTask should trim title and description`() {
        val request = CreateTaskRequest(title = "  Trimmed Title  ", description = "  Trimmed Description  ")
        val result = TaskValidator.validateCreateTask(request)
        
        assertTrue(result is ValidationResult.Success)
        assertEquals("Trimmed Title", result.value.title)
        assertEquals("Trimmed Description", result.value.description)
    }
}
