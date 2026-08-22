package com.theomls.dibs.services

import com.theomls.dibs.models.*
import com.theomls.dibs.models.Task
import com.theomls.dibs.repositories.GroupRepository
import com.theomls.dibs.repositories.TaskRepository
import com.theomls.dibs.repositories.UserRepository
import com.theomls.dibs.validation.TaskValidator
import com.theomls.dibs.validation.ValidationResult

class TaskService(
    private val taskRepository: TaskRepository,
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository
) {
    suspend fun createTask(userId: String, groupId: String, request: CreateTaskRequest): Task {
        userRepository.ensureUserExists(userId)
        groupRepository.ensureGroupExists(groupId)
        val validation = TaskValidator.validateCreateTask(request)

        val cleanRequest = when (validation) {
            is ValidationResult.Success -> validation.value
            is ValidationResult.Failure -> throw IllegalArgumentException(validation.message)
        }

        val newTask = Task(
            groupId = groupId,
            title = cleanRequest.title,
            description = cleanRequest.description,
            createdByUserId = userId
        )

        return taskRepository.saveTask(newTask)
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