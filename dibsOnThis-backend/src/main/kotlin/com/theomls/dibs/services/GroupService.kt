package com.theomls.dibs.services

import com.theomls.dibs.models.*
import com.theomls.dibs.repositories.GroupRepository
import com.theomls.dibs.repositories.UserRepository
import com.theomls.dibs.validation.GroupValidator
import com.theomls.dibs.validation.ValidationResult
import io.ktor.server.plugins.NotFoundException
import java.security.SecureRandom
import java.util.*

class GroupService(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository
) {
    private val secureRandom = SecureRandom()
    private val charset = GroupConstants.INVITE_CODE_CHARSET

    private suspend fun generateUnique(
        generator: () -> String,
        existsChecker: suspend (String) -> Boolean,
        whatUnique: String
    ): String {
        var code: String
        var exists: Boolean
        var tries = 0
        do {
            code = generator()
            exists = existsChecker(code)
            tries++
            if (tries >= GroupConstants.UNIQUE_GENERATION_MAX_ATTEMPTS) {
                throw IllegalStateException("Failed to generate unique $whatUnique after $tries attempts.")
            }
        } while (exists)
        return code
    }

    private fun generateInviteCode(length: Int = GroupConstants.INVITE_CODE_LENGTH): String {
        return (1..length)
            .map { charset[secureRandom.nextInt(charset.length)] }
            .joinToString("")
    }

    suspend fun createGroup(userId: String, request: CreateGroupRequest): Group {
        if (!userRepository.existsById(userId)) {
            throw IllegalArgumentException("User does not exist")
        }
        val validation = GroupValidator.validateCreateGroup(request)

        val cleanRequest = when (validation) {
            is ValidationResult.Success -> validation.value
            is ValidationResult.Failure -> throw IllegalArgumentException(validation.message)
        }

        val groupId = generateUnique(
            generator = { UUID.randomUUID().toString() },
            existsChecker = groupRepository::existsById,
            whatUnique = "Group ID"
        )

        val inviteCode = generateUnique(
            generator = { generateInviteCode() },
            existsChecker = groupRepository::existsByInviteCode,
            whatUnique = "Invite Code"
        )

        val group = Group(
            groupId = groupId,
            name = cleanRequest.name,
            inviteCode = inviteCode,
            memberIds = listOf(userId),
        )

        return groupRepository.createGroup(group, userId)
    }

    suspend fun joinGroup(userId: String, request: JoinGroupRequest): Group {
        if (!userRepository.existsById(userId)) {
            throw IllegalArgumentException("User does not exist")
        }

        val validation = GroupValidator.validateJoinGroup(request)

        val cleanRequest = when (validation) {
            is ValidationResult.Success -> validation.value
            is ValidationResult.Failure -> throw IllegalArgumentException(validation.message)
        }

        val group = groupRepository.getGroupByInviteCode(cleanRequest.inviteCode)
            ?: throw NotFoundException("Group not found with invite code: ${cleanRequest.inviteCode}")

        return groupRepository.addMemberToGroup(group.groupId, userId)
    }

    suspend fun leaveGroup(userId: String, groupId: String): LeaveGroupResponse {
        if (!userRepository.existsById(userId)) {
            throw IllegalArgumentException("User does not exist")
        }

        if (!groupRepository.existsById(groupId)) {
            throw NotFoundException("Group does not exist")
        }

        val groupWasDeleted = groupRepository.removeMemberFromGroup(groupId, userId)

        return LeaveGroupResponse(
            groupId = groupId,
            deleted = groupWasDeleted
        )
    }

    suspend fun getGroupById(groupId: String): Group {
        return groupRepository.getGroupById(groupId)
            ?: throw NotFoundException("Group not found with ID: $groupId")
    }
}