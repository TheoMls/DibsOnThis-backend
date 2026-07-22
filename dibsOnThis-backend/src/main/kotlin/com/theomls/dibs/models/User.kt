package com.theomls.dibs.models

import kotlinx.serialization.Serializable

/**
 * Core User entity representing a registered user in the dibsOnThis app.
 */
@Serializable
data class User(
    val id: String = "",                    // Firebase Auth UID
    val displayName: String = "",                // User's full name or nickname
    val email: String = "",                      // User's email address
    val groupIds: List<String> = emptyList(),// List of Group IDs this user belongs to
    val totalPoints: Int = 0,               // Cumulative score earned from completing tasks
    val avatarUrl: String? = null,          // Optional profile picture URL
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Data Transfer Object (DTO) sent by the client upon first login/signup
 * to register user details in Firestore.
 */
@Serializable
data class CreateUserRequest(
    val displayName: String,
    val email: String,
    val avatarUrl: String? = null
)

/**
 * DTO sent when a user updates their profile details.
 */
@Serializable
data class UpdateUserRequest(
    val displayName: String? = null,
    val avatarUrl: String? = null
)

fun CreateUserRequest.sanitizeAndValidate(): CreateUserRequest {
    val cleanName = displayName.trim()
    val cleanEmail = email.trim().lowercase()

    require(cleanName.length in 2..50) { "Name must be 2-50 characters" }
    require(cleanEmail.contains("@")) { "Invalid email format" }

    return this.copy(displayName = cleanName, email = cleanEmail)
}