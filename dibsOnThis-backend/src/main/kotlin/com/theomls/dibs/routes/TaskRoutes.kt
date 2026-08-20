package com.theomls.dibs.routes

import com.theomls.dibs.auth.FirebaseUserPrincipal
import com.theomls.dibs.models.CreateTaskRequest
import com.theomls.dibs.services.TaskService
import io.ktor.http.*
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.taskRoutes(taskService: TaskService) {

    // Create task
    post("/tasks") {
        val request = call.receive<CreateTaskRequest>()
//        val principal = call.principal<FirebaseUserPrincipal>()
//            ?: throw IllegalStateException("Principal not found in authenticated route")
//
//        val user_id = principal.uid
        val userId = "test_user_1"

        val createdTask = taskService.createTask(userId, request)
        call.respond(HttpStatusCode.Created, createdTask)
    }

    // Get a task
    get("/tasks/{taskId}") {
        // TODO: Implement task retrieval
    }

    // Get tasks for a group
    get("/groups/{groupId}/tasks") {
        // TODO: Implement task retrieval for a group
    }

    // Claim task
    post("/tasks/{taskId}/claim") {
        // TODO: Implement task claiming
    }

    // Complete task
    post("/tasks/{taskId}/complete") {
        // TODO: Implement task completion
    }

    // Unclaim Task
    post("/tasks/{taskId}/unclaim") {
        // TODO: Implement task unclaiming
    }
}