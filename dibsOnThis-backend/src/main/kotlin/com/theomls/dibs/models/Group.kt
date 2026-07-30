package com.theomls.dibs.models

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val groupId: String = "",
    val name: String = "",
    val inviteCode: String = "",
    val memberIds: List<String> = emptyList(),
    val maxGroupSize: Int = 10,
)

//DTOs
@Serializable
data class CreateGroupRequest(
    val name: String,
)

@Serializable
data class JoinGroupRequest(
    val inviteCode: String
)

object GroupConstants {
    const val INVITE_CODE_LENGTH = 8
}