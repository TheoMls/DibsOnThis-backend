package com.theomls.dibs.services

import com.theomls.dibs.models.*
import com.theomls.dibs.models.Task
import com.theomls.dibs.repositories.GroupRepository
import com.theomls.dibs.repositories.TaskRepository
import com.theomls.dibs.repositories.UserRepository

class TaskService(
    private val taskRepository: TaskRepository,
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository
) {
    suspend fun createTask(userId: String, groupId: String, request: CreateTaskRequest): Task {
        TODO()
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