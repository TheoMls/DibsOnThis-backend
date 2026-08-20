package com.theomls.dibs.routes

import com.theomls.dibs.models.CreateGroupRequest
import com.theomls.dibs.models.JoinGroupRequest
import com.theomls.dibs.services.GroupService
import com.theomls.dibs.auth.requireUserId
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.groupRoutes(groupService: GroupService) {

    // Create group
    post("/groups") {
        val request = call.receive<CreateGroupRequest>()
        val userId = call.requireUserId()

        val group = groupService.createGroup(userId, request)
        call.respond(HttpStatusCode.Created, group)
    }

    // Join group
    post("/groups/join") {
        val request = call.receive<JoinGroupRequest>()
        val userId = call.requireUserId()

        val group = groupService.joinGroup(userId, request)
        call.respond(HttpStatusCode.OK, group)
    }

    // Leave group
    post("/groups/{groupId}/leave") {
        val groupId = call.parameters["groupId"]
            ?: throw IllegalArgumentException("Missing group id")

        val userId = call.requireUserId(true)

        val response = groupService.leaveGroup(userId, groupId)
        call.respond(HttpStatusCode.OK, response)
    }

    // Get group info
    get("/groups/{groupId}") {
        val groupId = call.parameters["groupId"]
            ?: throw IllegalArgumentException("Missing group id")

        val userId = call.requireUserId(true)

        val group = groupService.getGroupById(groupId, userId)
        call.respond(HttpStatusCode.OK, group)
    }
}
