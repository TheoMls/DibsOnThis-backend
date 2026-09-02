package com.theomls.dibs.models

import kotlinx.serialization.Serializable

@Serializable
enum class TaskStatus {
    OPEN,       // Created, waiting for someone to "dib" it
    CLAIMED,    // Claimed by a user, but not finished yet
    COMPLETED   // Done and points awarded
}

@Serializable
data class Task(
    val taskId: String = "",
    val groupId: String = "",
    val title: String = "",
    val description: String = "",
    val points: Int = 1,
    val status: TaskStatus = TaskStatus.OPEN,
    val assignedToUserId: String? = null, // null if unclaimed
    val createdByUserId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

// DTOs
@Serializable
data class CreateTaskRequest(
    val title: String,
    val description: String = ""
)

@Serializable
data class ChangeTaskStatusRequest(
    val status: TaskStatus
)

// Constants
object TaskConstants {
    const val MIN_TITLE_LENGTH = 3
    const val MAX_TITLE_LENGTH = 50
    const val MAX_DESCRIPTION_LENGTH = 500
}