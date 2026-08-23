package com.theomls.dibs.services

import com.theomls.dibs.models.CreateUserRequest
import com.theomls.dibs.models.User
import com.theomls.dibs.repositories.UserRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.*
import java.lang.reflect.Proxy

class UserServiceTest {

    companion object {
        private fun <T : Any> createDummy(clazz: Class<T>): T {
            return Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz)) { _, method, _ ->
                if (method.name == "toString") return@newProxyInstance "Dummy${clazz.simpleName}"
                if (method.name == "hashCode") return@newProxyInstance System.identityHashCode(Proxy.getInvocationHandler(this))
                if (method.name == "equals") return@newProxyInstance false
                if (method.name == "collection") return@newProxyInstance null
                throw UnsupportedOperationException("Dummy object method ${method.name} not implemented")
            } as T
        }
        
        private val dummyDb = createDummy(com.google.cloud.firestore.Firestore::class.java)
    }

    private class MockUserRepository : UserRepository(dummyDb) {
        var userExists = false
        var savedUser: User? = null

        override suspend fun saveUser(user: User): User {
            if (userExists) {
                throw IllegalArgumentException("User profile already exists!")
            }
            savedUser = user
            return user
        }

        override suspend fun getUserById(id: String): User? {
            return if (userExists) User(id = id) else null
        }
    }

    private lateinit var userService: UserService
    private lateinit var mockUserRepo: MockUserRepository

    @BeforeTest
    fun setup() {
        mockUserRepo = MockUserRepository()
        userService = UserService(mockUserRepo)
    }

    @Test
    fun `createUser should succeed when user does not exist`(): Unit = runBlocking {
        val userId = "user123"
        val request = CreateUserRequest(username = "testuser", email = "test@example.com")
        
        val result = userService.createUser(userId, request)
        
        assertEquals(userId, result.id)
        assertEquals("testuser", result.username)
        assertEquals("test@example.com", result.email)
        assertEquals(result, mockUserRepo.savedUser)
    }

    @Test
    fun `createUser should throw when user already exists`(): Unit = runBlocking {
        val userId = "user123"
        val request = CreateUserRequest(username = "testuser", email = "test@example.com")
        mockUserRepo.userExists = true
        
        assertFailsWith<IllegalArgumentException> {
            userService.createUser(userId, request)
        }
    }
}
