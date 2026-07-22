package com.theomls.dibs.models

import kotlinx.serialization.Serializable

/**
 * Core User entity representing a registered user in the dibsOnThis app.
 */
@Serializable
data class User(
    val id: String = "",                    // Firebase Auth UID
    val displayName: String = "",
    val email: String = "",
    val groupIds: List<String> = emptyList(),
    val totalPoints: Int = 0,
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
)

/**
 * DTO sent when a user updates their profile details.
 */
@Serializable
data class UpdateUserRequest(
    val displayName: String? = null,
)
