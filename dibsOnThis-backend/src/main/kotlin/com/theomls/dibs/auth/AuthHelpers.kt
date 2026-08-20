package com.theomls.dibs.auth

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal

fun ApplicationCall.requireUserId(test: Boolean = false): String {
    if (test) return "test-user-id-1"
    return principal<FirebaseUserPrincipal>()?.uid
        ?: throw IllegalStateException("Unauthorized")
}