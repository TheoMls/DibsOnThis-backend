package com.theomls.dibs.validation

/**
 * Result of a validation operation.
 */
sealed class ValidationResult<out T> {
    data class Success<T>(val value: T) : ValidationResult<T>()
    data class Failure(val message: String) : ValidationResult<Nothing>()
}
