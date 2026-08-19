package com.theomls.dibs.routes

import com.theomls.dibs.models.CreateGroupRequest
import com.theomls.dibs.models.JoinGroupRequest
import com.theomls.dibs.services.GroupService
import com.theomls.dibs.auth.FirebaseUserPrincipal
import io.ktor.http.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.groupRoutes(groupService: GroupService) {

    //authenticate("firebase-auth") {
        // Create group
        post("/groups") {
            val request = call.receive<CreateGroupRequest>()
//            val principal = call.principal<FirebaseUserPrincipal>()
//                ?: throw IllegalStateException("Principal not found in authenticated route")
//
//            val user_id = principal.uid
            val user_id = "test_user_1"

            val group = groupService.createGroup(user_id, request)
            call.respond(HttpStatusCode.Created, group)
        }

        // Join group
        post("/groups/join") {
            val request = call.receive<JoinGroupRequest>()
//            val principal = call.principal<FirebaseUserPrincipal>()
//                ?: throw IllegalStateException("Principal not found in authenticated route")
//
//            val user_id = principal.uid
            val user_id = "test_user_1"

            val group = groupService.joinGroup(user_id, request)
            call.respond(HttpStatusCode.OK, group)
        }

        // Leave group
        post("/groups/{groupId}/leave") {
            val groupId = call.parameters["groupId"]
                ?: throw IllegalArgumentException("Missing group id")

//            val principal = call.principal<FirebaseUserPrincipal>()
//                ?: throw IllegalStateException("Principal not found in authenticated route")
//
//            val user_id = principal.uid

            val user_id = "test_user_1"

            val response = groupService.leaveGroup(user_id, groupId)
            call.respond(HttpStatusCode.OK, response)
        }
    //}

    // Get group info
    get("/groups/{groupId}") {
        val groupId = call.parameters["groupId"]
            ?: throw IllegalArgumentException("Missing group id")

        val group = groupService.getGroupById(groupId)
        call.respond(HttpStatusCode.OK, group)
    }
}
