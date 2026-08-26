package com.theomls.dibs.services

import com.theomls.dibs.models.*
import com.theomls.dibs.models.Task
import com.theomls.dibs.repositories.GroupRepository
import com.theomls.dibs.repositories.TaskRepository
import com.theomls.dibs.repositories.UserRepository
import com.theomls.dibs.utils.checkUserIsMemberOfGroup
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

        userRepository.fetchUserOrThrow(userId)

        val group = groupRepository.fetchGroupOrThrow(groupId)

        checkUserIsMemberOfGroup(userId, group)

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

        return taskRepository.saveTask(newTask)
    }

    suspend fun getTaskById(userId: String, taskId: String): Task {
        val task = taskRepository.fetchTaskOrThrow(taskId)
        val group = groupRepository.fetchGroupOrThrow(task.groupId)
        checkUserIsMemberOfGroup(userId, group)
        return task
    }

    suspend fun getTasksByGroupId(userId: String, groupId: String): List<Task> {
        val group = groupRepository.fetchGroupOrThrow(groupId)
        checkUserIsMemberOfGroup(userId, group)

        return taskRepository.getTasksByGroupId(groupId)
    }

    suspend fun claimTask(userId: String, taskId: String): Task {
        val task = taskRepository.fetchTaskOrThrow(taskId)
        val group = groupRepository.fetchGroupOrThrow(task.groupId)
        checkUserIsMemberOfGroup(userId, group)

        if (task.createdByUserId == userId)
            throw IllegalArgumentException("You cannot claim your own task")

        if (task.assignedToUserId != null)
            if (task.assignedToUserId == userId)
                throw IllegalArgumentException("Task is already claimed by you")
            else
                throw IllegalArgumentException("Task is already claimed")

        if (task.status != TaskStatus.OPEN)
            throw IllegalArgumentException("Task is not open")

        return taskRepository.claimTask(task, userId)
    }

    suspend fun completeTask(userId: String, taskId: String): Task {
        val task = taskRepository.fetchTaskOrThrow(taskId)
        val group = groupRepository.fetchGroupOrThrow(task.groupId)
        checkUserIsMemberOfGroup(userId, group)

        if (task.assignedToUserId != userId)
            throw IllegalArgumentException("You haven't claimed this task")

        if (task.status != TaskStatus.CLAIMED)
            throw IllegalArgumentException("Task is not in claimed status")

        return taskRepository.completeTask(task, userId)
    }

    suspend fun unclaimTask(userId: String, taskId: String): Task {
        val task = taskRepository.fetchTaskOrThrow(taskId)
        val group = groupRepository.fetchGroupOrThrow(task.groupId)
        checkUserIsMemberOfGroup(userId, group)

        if (task.assignedToUserId != userId)
            throw IllegalArgumentException("You haven't claimed this task")

        if (task.status != TaskStatus.CLAIMED)
            throw IllegalArgumentException("Task is not in claimed status")

        return taskRepository.unclaimTask(task, userId)
    }

    suspend fun deleteTask(userId: String, taskId: String) {
        val task = taskRepository.fetchTaskOrThrow(taskId)
        val group = groupRepository.fetchGroupOrThrow(task.groupId)
        checkUserIsMemberOfGroup(userId, group)

        if (task.createdByUserId != userId)
            throw IllegalArgumentException("Only the creator can delete the task")

        taskRepository.deleteTask(task, userId)
    }
}