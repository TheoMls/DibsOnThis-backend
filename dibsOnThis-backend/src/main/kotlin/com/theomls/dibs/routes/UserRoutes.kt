package com.theomls.dibs.routes

import com.theomls.dibs.models.CreateUserRequest
import com.theomls.dibs.services.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import com.theomls.dibs.utils.requireUserId
import io.ktor.server.routing.get

fun Route.userRoutes(userService: UserService) {
    // Create user
    post("/users") {
        val request = call.receive<CreateUserRequest>()
        val userId = call.requireUserId()

        val createdUser = userService.createUser(userId, request)
        call.respond(HttpStatusCode.Created, createdUser)
    }

    // Get current user
    get("/users/me") {
        val userId = call.requireUserId()

        val user = userService.getUserById(userId)
        call.respond(HttpStatusCode.OK, user)
    }

    // for testing
    // Get user from id
    get("/users/{id}") {
        val userId = call.parameters["id"]
            ?: throw IllegalArgumentException("Missing user id")

        val user = userService.getUserById(userId)
        call.respond(HttpStatusCode.OK, user)
    }
}