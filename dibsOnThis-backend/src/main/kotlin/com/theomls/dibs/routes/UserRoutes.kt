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

fun Route.userRoutes(userService: UserService) {
    //authenticate("firebase-auth") {
        post("/users") {
            val request = call.receive<CreateUserRequest>()

            // Extracts the real Firebase UID from the validated token
//            val principal = call.principal<FirebaseUserPrincipal>()
//                ?: throw IllegalArgumentException("Unauthorized")
//            val userId = principal.uid

            // Use a mock ID for testing right now
            val userId = "test-user-id-1"

            val createdUser = userService.createUser(userId, request)
            call.respond(HttpStatusCode.Created, createdUser)
        //}
    }
}