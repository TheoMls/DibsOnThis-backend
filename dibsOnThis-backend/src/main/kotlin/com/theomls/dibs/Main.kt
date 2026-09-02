package com.theomls.dibs

import com.theomls.dibs.application.configureFirebase
import com.theomls.dibs.application.configureSerialization
import com.theomls.dibs.application.configureStatusPages
import com.theomls.dibs.auth.configureAuthentication
import com.theomls.dibs.repositories.GroupRepository
import com.theomls.dibs.repositories.TaskRepository
import com.theomls.dibs.repositories.UserRepository
import com.theomls.dibs.routes.groupRoutes
import com.theomls.dibs.routes.taskRoutes
import com.theomls.dibs.routes.userRoutes
import com.theomls.dibs.services.GroupService
import com.theomls.dibs.services.TaskService
import com.theomls.dibs.services.UserService
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.netty.EngineMain
import io.ktor.server.response.respondText
import io.ktor.server.routing.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

/**
 * Ktor automatically calls this function when starting up due to application.yaml config.
 */
fun Application.module() {
    configureFirebase()
    configureSerialization()
    configureStatusPages()
    configureAuthentication()

    val firestore: com.google.cloud.firestore.Firestore = com.google.firebase.cloud.FirestoreClient.getFirestore()

    // Instantiate Repository and Service layers
    val userRepository = UserRepository(firestore)
    val userService = UserService(userRepository)

    val groupRepository = GroupRepository(firestore)
    val groupService = GroupService(groupRepository, userRepository)

    val taskRepository = TaskRepository(firestore)
    val taskService = TaskService(taskRepository, groupRepository, userRepository)

    // Register routes into Ktor
    routing {
        get("/") { call.respondText("DibsOnThis Server Running!") }

        //authenticate ("firebase-auth") {
            userRoutes(userService)
            groupRoutes(groupService)
            taskRoutes(taskService)
        //}
    }
}