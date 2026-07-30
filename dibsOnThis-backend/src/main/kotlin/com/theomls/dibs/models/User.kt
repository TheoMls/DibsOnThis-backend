package com.theomls.dibs.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",                    // Firebase Auth UID
    val displayName: String = "",
    val email: String = "",
    val groupIds: List<String> = emptyList(),
    val totalPoints: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

//DTOs
@Serializable
data class CreateUserRequest(
    val displayName: String,
    val email: String,
)

@Serializable
data class UpdateUserRequest(
    val displayName: String? = null,
)
