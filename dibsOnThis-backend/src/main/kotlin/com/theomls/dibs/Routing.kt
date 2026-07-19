package com.theomls.dibs

import com.google.firebase.cloud.FirestoreClient
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("DibsOnThis Server Running!")
        }
        // 🔥 Firebase Test Route
        get("/test-firebase") {
            try {
                val db = FirestoreClient.getFirestore()

                // Creates a 'health_check' collection if it doesn't exist
                // and writes a document inside it.
                val testData = mapOf(
                    "status" to "ok",
                    "timestamp" to System.currentTimeMillis(),
                    "test" to true,
                    "user" to "admin theoMls"
                )

                db.collection("health_check")
                    .document("connection_test")
                    .set(testData)
                    .get() // Block to wait for completion

                call.respond(HttpStatusCode.OK, mapOf("status" to "success", "message" to "Connected to Firestore!"))
            } catch (e: Exception) {
                application.log.error("Firestore connection failed", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Unknown error")))
            }
        }
    }
}