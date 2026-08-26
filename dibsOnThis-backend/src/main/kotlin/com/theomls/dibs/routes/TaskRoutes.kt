package com.theomls.dibs.routes

import com.theomls.dibs.utils.requireUserId
import com.theomls.dibs.models.CreateTaskRequest
import com.theomls.dibs.services.TaskService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*

fun Route.taskRoutes(taskService: TaskService) {

    // Create task
    post("/groups/{groupId}/tasks") {
        val request = call.receive<CreateTaskRequest>()

        val groupId = call.parameters["groupId"]
        ?: throw IllegalArgumentException("Missing group id")
        val userId = call.requireUserId(true)

        val createdTask = taskService.createTask(userId, groupId, request)
        call.respond(HttpStatusCode.Created, createdTask)
    }

    // Get tasks for a group
    get("/groups/{groupId}/tasks") {
        val groupId = call.parameters["groupId"]
        ?: throw IllegalArgumentException("Missing group id")
        val userId = call.requireUserId(true)

        val tasks = taskService.getTasksByGroupId(userId, groupId)
        call.respond(HttpStatusCode.OK, tasks)
    }

    // Get a task
    get("/tasks/{taskId}") {
        val taskId = call.parameters["taskId"]
            ?: throw IllegalArgumentException("Missing task id")
        val userId = call.requireUserId(true)

        val task = taskService.getTaskById(userId, taskId)
        call.respond(HttpStatusCode.OK, task)
    }

    // Claim task
    post("/tasks/{taskId}/claim") {
        val taskId = call.parameters["taskId"]
            ?: throw IllegalArgumentException("Missing task id")
        val userId = call.requireUserId(true)

        val claimedTask = taskService.claimTask(userId, taskId)
        call.respond(HttpStatusCode.OK, claimedTask)
    }

    // Complete task
    post("/tasks/{taskId}/complete") {
        val taskId = call.parameters["taskId"]
        ?: throw IllegalArgumentException("Missing task id")
        val userId = call.requireUserId(true)

        val claimedTask = taskService.completeTask(userId, taskId)
        call.respond(HttpStatusCode.OK, claimedTask)
    }

    // Unclaim Task
    post("/tasks/{taskId}/unclaim") {
        val taskId = call.parameters["taskId"]
            ?: throw IllegalArgumentException("Missing task id")
        val userId = call.requireUserId(true)

        val claimedTask = taskService.unclaimTask(userId, taskId)
        call.respond(HttpStatusCode.OK, claimedTask)
    }

    // Delete Task
    delete("/tasks/{taskId}") {
        val taskId = call.parameters["taskId"]
            ?: throw IllegalArgumentException("Missing task id")
        val userId = call.requireUserId(true)

        taskService.deleteTask(userId, taskId)
        call.respond(HttpStatusCode.OK)
    }
}