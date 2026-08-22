package com.theomls.dibs.validation

object ValidationUtils {
    fun validateString(str: String, minLen: Int, maxLen: Int, fieldName: String): ValidationResult<String> {
        val cleanStr = str.trim()

        if (cleanStr.length !in minLen..maxLen) {
            return ValidationResult.Failure("$fieldName must be between $minLen and $maxLen characters.")
        }
        return ValidationResult.Success(cleanStr)
    }
}
