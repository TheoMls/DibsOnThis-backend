package com.theomls.dibs.services

import com.theomls.dibs.models.*
import com.theomls.dibs.repositories.GroupRepository
import com.theomls.dibs.repositories.UserRepository
import com.theomls.dibs.utils.checkUserIsMemberOfGroup
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
        val validation = GroupValidator.validateCreateGroup(request)

        val cleanRequest = when (validation) {
            is ValidationResult.Success -> validation.value
            is ValidationResult.Failure -> throw IllegalArgumentException(validation.message)
        }

        val user = userRepository.fetchUserOrThrow(userId)

        if (user.groupIds.size >= UserConstants.MAX_GROUP_MEMBERSHIPS)
            throw IllegalArgumentException("User cannot join more than ${UserConstants.MAX_GROUP_MEMBERSHIPS} groups.")

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

        val newGroup = Group(
            groupId = groupId,
            name = cleanRequest.name,
            inviteCode = inviteCode,
            memberIds = listOf(userId),
        )

        return groupRepository.saveGroup(newGroup, userId)
    }

    suspend fun joinGroup(userId: String, request: JoinGroupRequest): Group {
        val validation = GroupValidator.validateJoinGroup(request)

        val cleanRequest = when (validation) {
            is ValidationResult.Success -> validation.value
            is ValidationResult.Failure -> throw IllegalArgumentException(validation.message)
        }

        val user = userRepository.fetchUserOrThrow(userId)
        val group = groupRepository.getGroupByInviteCode(cleanRequest.inviteCode)
            ?: throw NotFoundException("Group not found with invite code: ${cleanRequest.inviteCode}")

        if (group.memberIds.contains(userId))
            return group

        if (user.groupIds.contains(group.groupId))
            throw IllegalArgumentException("User is already a member of this group.")

        if (user.groupIds.size >= UserConstants.MAX_GROUP_MEMBERSHIPS)
            throw IllegalArgumentException("User cannot join more than ${UserConstants.MAX_GROUP_MEMBERSHIPS} groups.")

        if (group.memberIds.size >= group.maxGroupSize)
            throw IllegalArgumentException("Group is full.")

        return groupRepository.addMemberToGroup(group, user)
    }

    suspend fun leaveGroup(userId: String, groupId: String): LeaveGroupResponse {
        val group = groupRepository.fetchGroupOrThrow(groupId)
        val user = userRepository.fetchUserOrThrow(userId)

        checkUserIsMemberOfGroup(userId, group)

        val groupWasDeleted = groupRepository.removeMemberFromGroup(group, user)

        return LeaveGroupResponse(
            groupId = groupId,
            deleted = groupWasDeleted
        )
    }

    suspend fun getGroupById(userId: String, groupId: String): Group {
        val group = groupRepository.fetchGroupOrThrow(groupId)

        checkUserIsMemberOfGroup(userId, group)

        return group
    }
}