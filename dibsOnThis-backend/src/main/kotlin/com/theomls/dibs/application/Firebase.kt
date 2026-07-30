package com.theomls.dibs.application

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.server.application.*
import java.io.InputStream

fun Application.configureFirebase() {
    val serviceAccount: InputStream? = this::class.java.classLoader
        .getResourceAsStream("firebase-key-dev.json")

    if (serviceAccount == null) {
        environment.log.error("Could not find firebase-key-dev.json in resources!")
        return
    }

    val options: com.google.firebase.FirebaseOptions = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .build()

    if (FirebaseApp.getApps().isEmpty()) {
        FirebaseApp.initializeApp(options)
        environment.log.info("Firebase Admin SDK initialized successfully!")
    }
}