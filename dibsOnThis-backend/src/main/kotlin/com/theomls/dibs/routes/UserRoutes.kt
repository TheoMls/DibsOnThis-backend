package com.theomls.dibs.routes

import com.theomls.dibs.models.CreateUserRequest
import com.theomls.dibs.services.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import com.theomls.dibs.auth.FirebaseUserPrincipal
import io.ktor.server.routing.get

fun Route.userRoutes(userService: UserService) {
    //authenticate("firebase-auth") {}
        // Create user
        post("/users") {
            val request = call.receive<CreateUserRequest>()

            // Extracts the real Firebase UID from the validated token
//          val principal = call.principal<FirebaseUserPrincipal>()
//              ?: return@post call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
//
//          val userId = principal.uid

            // Use a mock ID for testing right now
            val userId = "test-user-id-1"

            val createdUser = userService.createUser(userId, request)
            call.respond(HttpStatusCode.Created, createdUser)
        }

        // for testing
        // Get user from id
        get("/users/{id}"){
            val userId = call.parameters["id"]
                ?: throw IllegalArgumentException("Missing user id")

            val user = userService.getUserById(userId)
            call.respond(
                HttpStatusCode.OK,
                user
            )
        }

        // Get current user
        get("/users/me") {
//            val principal = call.principal<FirebaseUserPrincipal>()
//                ?: return@get call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
//
//            val userId = principal.uid

            // Use a mock ID for testing right now
            val userId = "test-user-id-1"

            val user = userService.getUserById(userId)
            call.respond(HttpStatusCode.OK, user)
        }
}