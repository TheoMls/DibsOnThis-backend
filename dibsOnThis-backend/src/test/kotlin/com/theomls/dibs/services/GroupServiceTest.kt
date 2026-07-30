package com.theomls.dibs.services

import com.theomls.dibs.models.*
import com.theomls.dibs.repositories.GroupRepository
import com.theomls.dibs.repositories.UserRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.*
import java.lang.reflect.Proxy

class GroupServiceTest {

    companion object {
        private fun <T : Any> createDummy(clazz: Class<T>): T {
            return Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz)) { _, method, _ ->
                if (method.name == "toString") return@newProxyInstance "Dummy${clazz.simpleName}"
                if (method.name == "hashCode") return@newProxyInstance System.identityHashCode(Proxy.getInvocationHandler(this))
                if (method.name == "equals") return@newProxyInstance false
                // Special handling for collection to avoid needing CollectionReference proxy
                if (method.name == "collection") return@newProxyInstance null
                throw UnsupportedOperationException("Dummy object method ${method.name} not implemented")
            } as T
        }
        
        private val dummyDb = createDummy(com.google.cloud.firestore.Firestore::class.java)
    }

    private class MockUserRepository : UserRepository(dummyDb) {
        override val database: com.google.cloud.firestore.Firestore get() = dummyDb
        var userExists = true
        override suspend fun existsById(id: String): Boolean = userExists
    }

    private class MockGroupRepository : GroupRepository(dummyDb) {
        override val database: com.google.cloud.firestore.Firestore get() = dummyDb
        override val groupsCollection: com.google.cloud.firestore.CollectionReference get() = throw UnsupportedOperationException("Mocked")
        override val usersCollection: com.google.cloud.firestore.CollectionReference get() = throw UnsupportedOperationException("Mocked")
        
        var groupExistsById = false
        var groupExistsByInviteCode = false
        var createdGroup: Group? = null
        var memberAddedGroup: Group? = null
        var groupToReturnByInviteCode: Group? = null

        override suspend fun existsById(groupId: String): Boolean = groupExistsById
        override suspend fun existsByInviteCode(inviteCode: String): Boolean = groupExistsByInviteCode
        override suspend fun createGroup(group: Group, userId: String): Group {
            createdGroup = group
            return group
        }
        override suspend fun getGroupByInviteCode(inviteCode: String): Group? = groupToReturnByInviteCode
        override suspend fun addMemberToGroup(groupId: String, userId: String): Group {
            return memberAddedGroup ?: throw IllegalStateException("memberAddedGroup not set")
        }
    }

    private lateinit var groupService: GroupService
    private lateinit var mockUserRepo: MockUserRepository
    private lateinit var mockGroupRepo: MockGroupRepository

    @BeforeTest
    fun setup() {
        mockUserRepo = MockUserRepository()
        mockGroupRepo = MockGroupRepository()
        groupService = GroupService(mockGroupRepo, mockUserRepo)
    }

    @Test
    fun `createGroup should succeed with valid request`() = runBlocking {
        val userId = "user123"
        val request = CreateGroupRequest(name = "New Group")
        
        val result = groupService.createGroup(userId, request)
        
        assertEquals("New Group", result.name)
        assertEquals(listOf(userId), result.memberIds)
        assertNotNull(result.groupId)
        assertNotNull(result.inviteCode)
        assertEquals(result, mockGroupRepo.createdGroup)
    }

    @Test
    fun `createGroup should throw if user does not exist`() {
        runBlocking {
            mockUserRepo.userExists = false
            val request = CreateGroupRequest(name = "New Group")

            assertFailsWith<IllegalArgumentException> {
                groupService.createGroup("invalid-user", request)
            }
        }
    }

    @Test
    fun `joinGroup should succeed with valid invite code`() = runBlocking {
        val userId = "user123"
        val inviteCode = "A".repeat(GroupConstants.INVITE_CODE_LENGTH)
        val group = Group(groupId = "group123", name = "Test Group", inviteCode = inviteCode)
        mockGroupRepo.groupToReturnByInviteCode = group
        mockGroupRepo.memberAddedGroup = group.copy(memberIds = listOf(userId))

        val result = groupService.joinGroup(userId, JoinGroupRequest(inviteCode))

        assertEquals(group.groupId, result.groupId)
        assertTrue(result.memberIds.contains(userId))
    }

    @Test
    fun `joinGroup should throw if group not found`() {
        runBlocking {
            val inviteCode = "A".repeat(GroupConstants.INVITE_CODE_LENGTH)
            mockGroupRepo.groupToReturnByInviteCode = null

            assertFailsWith<IllegalArgumentException> {
                groupService.joinGroup("user123", JoinGroupRequest(inviteCode))
            }
        }
    }
}
