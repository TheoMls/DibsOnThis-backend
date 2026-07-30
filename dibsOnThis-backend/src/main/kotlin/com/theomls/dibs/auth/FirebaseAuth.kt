package com.theomls.dibs.auth

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.net.URI
import java.util.concurrent.TimeUnit

fun Application.configureAuthentication() {
    // Replace "dibs-on-this" with your actual Firebase Project ID
    val issuer = "https://securetoken.google.com/dibs-on-this"

    // Fixed the deprecated string-to-URL constructor using URI
    val jwkUrl: java.net.URL = URI("https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com").toURL()

    val jwkProvider: com.auth0.jwk.JwkProvider = JwkProviderBuilder(jwkUrl)
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    install(Authentication) {
        jwt("firebase-auth") {
            realm = "DibsOnThis Server"
            verifier(jwkProvider, issuer) {
                acceptLeeway(3)
            }
            validate { credential ->
                val uid = credential.payload.subject
                if (!uid.isNullOrBlank()) {
                    FirebaseUserPrincipal(uid)
                } else {
                    null
                }
            }
        }
    }
}