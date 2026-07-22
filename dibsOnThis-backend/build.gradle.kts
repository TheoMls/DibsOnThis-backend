plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.theomls.dibs"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}
dependencies {
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.callLogging)
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.statusPages)
    implementation(libs.logback.classic)
    implementation(ktorLibs.server.auth)
    implementation("io.ktor:ktor-server-auth-jvm:3.5.0")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:3.5.0")
    implementation("com.auth0:jwks-rsa:0.24.1")

    // Firebase Admin SDK
    implementation("com.google.firebase:firebase-admin:9.10.0")

    constraints {
        implementation("com.fasterxml.jackson.core:jackson-core:2.18.9") {
            because("Fixes vulnerability WS-2026-0003 flagged by Mend.io")
        }
        implementation("com.fasterxml.jackson.core:jackson-databind:2.18.9")
        implementation("com.fasterxml.jackson.core:jackson-annotations:2.18.9")
    }

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
}
