package com.theomls.dibs.services
import com.theomls.dibs.models.*
import com.theomls.dibs.models.Task
import com.theomls.dibs.repositories.GroupRepository
import com.theomls.dibs.repositories.TaskRepository
import com.theomls.dibs.repositories.UserRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.*
import java.lang.reflect.Proxy

class TaskServiceTest {

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

    private class MockTaskRepository : TaskRepository(dummyDb) {
        override val tasksCollection: com.google.cloud.firestore.CollectionReference get() = throw UnsupportedOperationException("Mocked")
        override val groupsCollection: com.google.cloud.firestore.CollectionReference get() = throw UnsupportedOperationException("Mocked")
        override val usersCollection: com.google.cloud.firestore.CollectionReference get() = throw UnsupportedOperationException("Mocked")

        var taskExists = false
        var savedTask: Task? = null
        var shouldFailSave = false
        var failureMessage = ""

        override suspend fun existsById(taskId: String): Boolean = taskExists

        override suspend fun saveTask(newTask: Task): Task {
            if (shouldFailSave) {
                throw IllegalArgumentException(failureMessage)
            }
            savedTask = newTask
            return newTask
        }

        var tasksToReturn: List<Task> = emptyList()
        override suspend fun getTasksByGroupId(groupId: String): List<Task> = tasksToReturn

        var taskToReturn: Task? = null
        override suspend fun getTaskById(taskId: String): Task? = taskToReturn
        
        override suspend fun fetchTaskOrThrow(taskId: String): Task = getTaskById(taskId) ?: throw io.ktor.server.plugins.NotFoundException("Task not found")
    }

    private class MockGroupRepository : GroupRepository(dummyDb) {
        override val groupsCollection: com.google.cloud.firestore.CollectionReference get() = throw UnsupportedOperationException("Mocked")
        override val usersCollection: com.google.cloud.firestore.CollectionReference get() = throw UnsupportedOperationException("Mocked")

        var groupToReturn: Group? = null

        override suspend fun getGroupById(groupId: String): Group? = groupToReturn
        
        override suspend fun fetchGroupOrThrow(groupId: String): Group = getGroupById(groupId) ?: throw io.ktor.server.plugins.NotFoundException("Group not found")
    }

    private class MockUserRepository : UserRepository(dummyDb) {
        override val database: com.google.cloud.firestore.Firestore get() = dummyDb
        var userExists = true
        override suspend fun existsById(id: String): Boolean = userExists
        
        override suspend fun fetchUserOrThrow(id: String): User = if (userExists) User(userId = id) else throw io.ktor.server.plugins.NotFoundException("User does not exist")
    }

    private lateinit var taskService: TaskService
    private lateinit var mockTaskRepo: MockTaskRepository
    private lateinit var mockGroupRepo: MockGroupRepository
    private lateinit var mockUserRepo: MockUserRepository

    @BeforeTest
    fun setup() {
        mockTaskRepo = MockTaskRepository()
        mockGroupRepo = MockGroupRepository()
        mockUserRepo = MockUserRepository()
        taskService = TaskService(mockTaskRepo, mockGroupRepo, mockUserRepo)
    }

    @Test
    fun `createTask should succeed with valid request`() {
        runBlocking {
            val userId = "user123"
            val groupId = "group123"
            val request = CreateTaskRequest(title = "Test Task", description = "Test Description")
            
            mockGroupRepo.groupToReturn = Group(groupId = groupId, memberIds = listOf(userId))

            val result = taskService.createTask(userId, groupId, request)

            assertEquals("Test Task", result.title)
            assertEquals("Test Description", result.description)
            assertEquals(groupId, result.groupId)
            assertEquals(userId, result.createdByUserId)
            assertNotNull(result.taskId)
            assertEquals(result, mockTaskRepo.savedTask)
        }
    }

    @Test
    fun `createTask should throw when title is too short`() {
        runBlocking {
            val userId = "user123"
            val groupId = "group123"
            val request = CreateTaskRequest(title = "ab") // Min length is 3

            val exception = assertFailsWith<IllegalArgumentException> {
                taskService.createTask(userId, groupId, request)
            }
            assertTrue(exception.message!!.contains("characters"), "Expected validation message about characters")
        }
    }

    @Test
    fun `createTask should throw when repository save fails`() {
        runBlocking {
            val userId = "user123"
            val groupId = "group123"
            val request = CreateTaskRequest(title = "Test Task")

            mockGroupRepo.groupToReturn = Group(groupId = groupId, memberIds = listOf(userId))
            mockTaskRepo.shouldFailSave = true
            mockTaskRepo.failureMessage = "User is not a member of this group"

            val exception = assertFailsWith<IllegalArgumentException> {
                taskService.createTask(userId, groupId, request)
            }
            assertEquals("User is not a member of this group", exception.message)
        }
    }

    @Test
    fun `getTasksByGroupId should return tasks when user is member`() {
        runBlocking {
            val userId = "user1"
            val groupId = "group1"
            val group = Group(groupId = groupId, memberIds = listOf(userId))
            val tasks = listOf(Task(taskId = "task1", groupId = groupId))

            mockGroupRepo.groupToReturn = group
            mockTaskRepo.tasksToReturn = tasks

            val result = taskService.getTasksByGroupId(userId, groupId)

            assertEquals(tasks, result)
        }
    }

    @Test
    fun `getTasksByGroupId should throw when user is not member`() {
        runBlocking {
            val userId = "user1"
            val groupId = "group1"
            val group = Group(groupId = groupId, memberIds = listOf("otherUser"))

            mockGroupRepo.groupToReturn = group

            val exception = assertFailsWith<IllegalArgumentException> {
                taskService.getTasksByGroupId(userId, groupId)
            }
            assertEquals("User is not a member of this group", exception.message)
        }
    }

    @Test
    fun `getTaskById should return task when user is member`() {
        runBlocking {
            val userId = "user1"
            val groupId = "group1"
            val taskId = "task1"
            val task = Task(taskId = taskId, groupId = groupId)
            val group = Group(groupId = groupId, memberIds = listOf(userId))

            mockTaskRepo.taskToReturn = task
            mockGroupRepo.groupToReturn = group

            val result = taskService.getTaskById(userId, taskId)

            assertEquals(task, result)
        }
    }

    @Test
    fun `getTaskById should throw when user is not member`() {
        runBlocking {
            val userId = "user1"
            val groupId = "group1"
            val taskId = "task1"
            val task = Task(taskId = taskId, groupId = groupId)
            val group = Group(groupId = groupId, memberIds = listOf("otherUser"))

            mockTaskRepo.taskToReturn = task
            mockGroupRepo.groupToReturn = group

            val exception = assertFailsWith<IllegalArgumentException> {
                taskService.getTaskById(userId, taskId)
            }
            assertEquals("User is not a member of this group", exception.message)
        }
    }

    @Test
    fun `getTaskById should throw when task not found`() {
        runBlocking {
            mockTaskRepo.taskToReturn = null

            assertFailsWith<io.ktor.server.plugins.NotFoundException> {
                taskService.getTaskById("user1", "task1")
            }
        }
    }
}
