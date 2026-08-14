package com.theomls.dibs.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",                    // Firebase Auth UID
    val username: String,
    val email: String,
    val groupIds: List<String> = emptyList(),
    val totalPoints: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

// DTOs
@Serializable
data class CreateUserRequest(
    val username: String,
    val email: String,
)

@Serializable
data class UpdateUserRequest(
    val username: String? = null,
)

// Constants
object UserConstants {
    const val MIN_USERNAME_LENGTH = 2
    const val MAX_USERNAME_LENGTH = 20

    const val MAX_GROUP_MEMBERSHIPS = 20
}
