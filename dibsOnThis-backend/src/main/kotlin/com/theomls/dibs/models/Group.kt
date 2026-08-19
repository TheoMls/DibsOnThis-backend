package com.theomls.dibs.models

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val groupId: String = "",
    val name: String = "",
    val inviteCode: String = "",
    val memberIds: List<String> = emptyList(),
    val maxGroupSize: Int = GroupConstants.MAX_GROUP_SIZE
)

// DTOs
@Serializable
data class CreateGroupRequest(
    val name: String,
)

@Serializable
data class JoinGroupRequest(
    val inviteCode: String
)

@Serializable
data class LeaveGroupResponse(
    val groupId: String,
    val deleted: Boolean
)

// Constants
object GroupConstants {
    const val MIN_NAME_LENGTH = 3
    const val MAX_NAME_LENGTH = 20

    const val INVITE_CODE_LENGTH = 8
    const val INVITE_CODE_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    const val MAX_GROUP_SIZE = 10

    const val UNIQUE_GENERATION_MAX_ATTEMPTS = 50
}