package com.theomls.dibs.services

import com.theomls.dibs.models.*
import com.theomls.dibs.models.Task
import com.theomls.dibs.repositories.GroupRepository
import com.theomls.dibs.repositories.TaskRepository
import com.theomls.dibs.repositories.UserRepository
import com.theomls.dibs.utils.generateUnique
import com.theomls.dibs.validation.TaskValidator
import com.theomls.dibs.validation.ValidationResult
import java.util.UUID

class TaskService(
    private val taskRepository: TaskRepository,
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository
) {
    suspend fun createTask(userId: String, groupId: String, request: CreateTaskRequest): Task {
        val validation = TaskValidator.validateCreateTask(request)

        val cleanRequest = when (validation) {
            is ValidationResult.Success -> validation.value
            is ValidationResult.Failure -> throw IllegalArgumentException(validation.message)
        }

        val taskId = generateUnique(
            generator = { UUID.randomUUID().toString() },
            existsChecker = taskRepository::existsById,
            whatUnique = "Task ID"
        )

        val newTask = Task(
            taskId = taskId,
            groupId = groupId,
            title = cleanRequest.title,
            description = cleanRequest.description,
            createdByUserId = userId
        )

        return taskRepository.saveTask(newTask, userId)
    }

    suspend fun getTaskById(userId: String, taskId: String): Task {
        TODO()
    }

    suspend fun getTasksByGroupId(userId:String, groupId: String): List<Task> {
        TODO()
    }

    suspend fun claimTask(userId: String, taskId: String): Task {
        TODO()
    }

    suspend fun completeTask(userId: String, taskId: String): Task {
        TODO()
    }

    suspend fun unclaimTask(userId: String, taskId: String): Task {
        TODO()
    }

    suspend fun deleteTask(userId: String, taskId: String) {
        TODO()
    }
}