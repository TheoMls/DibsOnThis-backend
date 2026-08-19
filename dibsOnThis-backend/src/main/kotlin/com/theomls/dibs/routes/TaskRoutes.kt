package com.theomls.dibs.routes

import com.theomls.dibs.auth.FirebaseUserPrincipal
import com.theomls.dibs.models.CreateTaskRequest
import io.ktor.http.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.taskRoutes() {

    //authenticate("firebase-auth") {
        // Create task
        post("/tasks") {
            val request = call.receive<CreateTaskRequest>()
            // TODO: Implement task creation
        }

        // Get tasks for a group
        get("/groups/{groupId}/tasks") {
            // TODO: Implement task retrieval
        }

        // Claim task
        post("/tasks/{groupId}/claim") {
            // TODO: Implement task claiming
        }

        // Complete task
        post("/tasks/{groupId}/complete") {
            // TODO: Implement task completion
        }

        // Unclaim Task
        post("/tasks/{groupId}/unclaim") {
            // TODO: Implement task unclaiming
        }
    //}
}