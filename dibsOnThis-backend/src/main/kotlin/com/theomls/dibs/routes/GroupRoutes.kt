package com.theomls.dibs.routes

import com.theomls.dibs.models.CreateGroupRequest
import com.theomls.dibs.models.JoinGroupRequest
import com.theomls.dibs.services.GroupService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.groupRoutes(groupService: GroupService) {

    // Create group
    post("/groups") {
        val request = call.receive<CreateGroupRequest>()
        // Mock userId
        val userId = "test-user-id-1"
        val group = groupService.createGroup(userId, request)
        call.respond(HttpStatusCode.Created, group)
    }

    // Join group
    post("/groups/join") {
        val request = call.receive<JoinGroupRequest>()
        // Mock userId
        val userId = "test-user-id-1"
        val group = groupService.joinGroup(userId, request)
        call.respond(HttpStatusCode.OK, group)
    }

    // Get group info
    get("/groups/{groupId}") {
        val groupId = call.parameters["groupId"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing group id")

        groupService.getGroupById(groupId)
        call.respond(HttpStatusCode.OK, "Group found")
    }

    // Leave group
    post("/groups/{groupId}/leave") {
        val groupId = call.parameters["groupId"]
            ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing group id")

        // Mock userId
        val userId = "test-user-id-1"

        val response = groupService.leaveGroup(userId, groupId)
        call.respond(HttpStatusCode.OK, response)
    }
}
