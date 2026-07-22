package com.theomls.dibs.services

import com.theomls.dibs.models.CreateUserRequest
import com.theomls.dibs.models.User
import com.theomls.dibs.repositories.UserRepository
import com.theomls.dibs.validation.UserValidator
import com.theomls.dibs.validation.ValidationResult

class UserService(private val userRepository: UserRepository) {

    fun createUser(userId: String, request: CreateUserRequest): User {
        val cleanRequest = when (val validation = UserValidator.validateCreateUser(request)) {
            is ValidationResult.Success -> validation.value
            is ValidationResult.Failure -> throw IllegalArgumentException(validation.message)
        }

        // Business Rule: Check if the user already has a profile registered
        val existingUser = userRepository.findUserById(userId)
        if (existingUser != null) {
            throw IllegalArgumentException("User profile already exists!")
        }

        // Combine clean input with server rules (points = 0, current timestamp)
        val newUser = User(
            id = userId,
            displayName = cleanRequest.displayName,
            email = cleanRequest.email,
            totalPoints = 0,
            groupIds = emptyList(),
            createdAt = System.currentTimeMillis()
        )

        // CALL THE NEXT LAYER -> UserRepository
        userRepository.saveUser(newUser)

        return newUser
    }
}