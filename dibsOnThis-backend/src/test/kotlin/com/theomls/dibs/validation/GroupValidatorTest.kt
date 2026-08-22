package com.theomls.dibs.validation

import com.theomls.dibs.models.*
import kotlin.test.*

class GroupValidatorTest {

    @Test
    fun `test validateCreateGroup with valid name`() {
        val request = CreateGroupRequest(name = "Test Group")
        val result = GroupValidator.validateCreateGroup(request)

        assertTrue(result is ValidationResult.Success)
        assertEquals("Test Group", result.value.name)
    }

    @Test
    fun `test validateCreateGroup with blank name`() {
        val request = CreateGroupRequest(name = "  ")
        val result = GroupValidator.validateCreateGroup(request)

        assertTrue(result is ValidationResult.Failure)
        assertEquals("Group name must be between 3 and 20 characters.", result.message)
    }

    @Test
    fun `test validateCreateGroup with short name`() {
        val request = CreateGroupRequest(name = "ab")
        val result = GroupValidator.validateCreateGroup(request)

        assertTrue(result is ValidationResult.Failure)
        assertEquals("Group name must be between 3 and 20 characters.", result.message)
    }

    @Test
    fun `test validateCreateGroup with long name`() {
        val request = CreateGroupRequest(name = "a".repeat(21))
        val result = GroupValidator.validateCreateGroup(request)

        assertTrue(result is ValidationResult.Failure)
        assertEquals("Group name must be between 3 and 20 characters.", result.message)
    }

    @Test
    fun `test validateJoinGroup with valid code`() {
        val validCode = "A".repeat(GroupConstants.INVITE_CODE_LENGTH)
        val request = JoinGroupRequest(inviteCode = validCode)
        val result = GroupValidator.validateJoinGroup(request)

        assertTrue(result is ValidationResult.Success)
        assertEquals(validCode, result.value.inviteCode)
    }

    @Test
    fun `test validateJoinGroup with invalid code`() {
        val invalidCode = "A".repeat(GroupConstants.INVITE_CODE_LENGTH - 1)
        val request = JoinGroupRequest(inviteCode = invalidCode)
        val result = GroupValidator.validateJoinGroup(request)

        assertTrue(result is ValidationResult.Failure)
        assertEquals("Invalid invite code format.", result.message)
    }
}
