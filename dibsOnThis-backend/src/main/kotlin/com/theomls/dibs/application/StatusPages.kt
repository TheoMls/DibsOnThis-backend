package com.theomls.dibs.application

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        // Handle Validation & Business Logic Errors (Status 400)
        exception<IllegalArgumentException> { call, cause ->
            call.respondText(text = "400: ${cause.message ?: "Invalid request"}", status = HttpStatusCode.BadRequest)
        }

        // Handle Illegal State Errors (Status 409 or 500)
        exception<IllegalStateException> { call, cause ->
            call.respondText(text = "409: ${cause.message ?: "Conflict"}", status = HttpStatusCode.Conflict)
        }

        // Handle Malformed JSON or Request Body (Status 400)
        exception<BadRequestException> { call, cause ->
            call.respondText(text = "400: ${cause.message ?: "Bad Request"}", status = HttpStatusCode.BadRequest)
        }

        // Handle Resource Not Found (Status 404)
        exception<NotFoundException> { call, cause ->
            call.respondText(text = "404: ${cause.message ?: "Resource not found"}", status = HttpStatusCode.NotFound)
        }

        // Global Catch-all (Status 500)
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: ${cause.message ?: "Internal Server Error"}", status = HttpStatusCode.InternalServerError)
        }
    }
}
