package com.theomls.dibs.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.taskRoutes() {

    // Create task
    post("/tasks") {
        // TODO: Implement task creation
    }

    // Get task by ID
    get("/groups/{groupId}/tasks"){
        // TODO: Implement task retrieval
    }

    // Claim task
    post("/tasks/{groupId}/claim"){
        // TODO: Implement task claiming
    }

    // Complete task
    post("/tasks/{groupId}/complete"){
        // TODO: Implement task completion
    }

    // Unclaim Task
    post("/tasks/{groupId}/unclaim"){
        // TODO: Implement task unclaiming
    }
}