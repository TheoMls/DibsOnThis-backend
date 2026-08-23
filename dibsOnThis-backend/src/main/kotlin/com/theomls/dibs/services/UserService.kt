package com.theomls.dibs.services

import com.theomls.dibs.models.CreateUserRequest
import com.theomls.dibs.models.User
import com.theomls.dibs.repositories.UserRepository
import com.theomls.dibs.validation.UserValidator
import com.theomls.dibs.validation.ValidationResult
import io.ktor.server.plugins.NotFoundException

class UserService(private val userRepository: UserRepository) {

    suspend fun createUser(userId: String, request: CreateUserRequest): User {
        val validation = UserValidator.validateCreateUser(request)

        val cleanRequest = when (validation) {
            is ValidationResult.Success -> validation.value
            is ValidationResult.Failure -> throw IllegalArgumentException(validation.message)
        }
        
        // Combine clean input with server rules (points = 0, current timestamp)
        val newUser = User(
            id = userId,
            username = cleanRequest.username,
            email = cleanRequest.email,
            totalPoints = 0,
            groupIds = emptyList(),
            createdAt = System.currentTimeMillis()
        )

        return userRepository.saveUser(newUser)
    }

    suspend fun getUserById(userId: String): User {
        return userRepository.getUserById(userId)
            ?: throw NotFoundException("User does not exist")
    }
}