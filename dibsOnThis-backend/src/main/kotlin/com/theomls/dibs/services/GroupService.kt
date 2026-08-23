package com.theomls.dibs.services

import com.theomls.dibs.models.*
import com.theomls.dibs.repositories.GroupRepository
import com.theomls.dibs.repositories.UserRepository
import com.theomls.dibs.utils.generateUnique
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

    private fun generateInviteCode(length: Int = GroupConstants.INVITE_CODE_LENGTH): String {
        return (1..length)
            .map { charset[secureRandom.nextInt(charset.length)] }
            .joinToString("")
    }

    suspend fun createGroup(userId: String, request: CreateGroupRequest): Group {
        userRepository.ensureUserExists(userId)
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
        userRepository.ensureUserExists(userId)

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
        userRepository.ensureUserExists(userId)
        groupRepository.ensureGroupExists(groupId)

        val groupWasDeleted = groupRepository.removeMemberFromGroup(groupId, userId)

        return LeaveGroupResponse(
            groupId = groupId,
            deleted = groupWasDeleted
        )
    }

    suspend fun getGroupById(userId: String, groupId: String): Group {
        userRepository.ensureUserExists(userId)

        val group = groupRepository.getGroupById(groupId)
            ?: throw NotFoundException("Group not found with ID: $groupId")

        if (!group.memberIds.contains(userId)) {
            throw IllegalArgumentException("User is not a member of this group")
        }

        return group
    }
}