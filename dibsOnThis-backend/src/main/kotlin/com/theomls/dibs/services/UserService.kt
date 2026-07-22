package com.theomls.dibs.services

import com.theomls.dibs.models.CreateUserRequest
import com.theomls.dibs.models.User
import com.theomls.dibs.repositories.UserRepository

class UserService(private val userRepository: UserRepository) {

    fun createUser(userId: String, request: CreateUserRequest): User {
        // Business Rule: Check if the user already has a profile registered
        val existingUser = userRepository.findUserById(userId)
        if (existingUser != null) {
            throw IllegalArgumentException("User profile already exists!")
        }

        // Combine clean input with server rules (points = 0, current timestamp)
        val newUser = User(
            id = userId,
            displayName = request.displayName,
            email = request.email,
            avatarUrl = request.avatarUrl,
            totalPoints = 0,               // Default starting points
            groupIds = emptyList(),        // Starts with no groups
            createdAt = System.currentTimeMillis()
        )

        // CALL THE NEXT LAYER -> UserRepository
        userRepository.saveUser(newUser)

        return newUser
    }
}