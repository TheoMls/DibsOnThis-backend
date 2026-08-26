package com.theomls.dibs.repositories

import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.Firestore
import com.theomls.dibs.models.Group
import com.theomls.dibs.models.User
import io.ktor.server.plugins.NotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class GroupRepository(db: Firestore) {
    protected open val groupsCollection: CollectionReference by lazy { db.collection("groups") }
    protected open val usersCollection: CollectionReference by lazy { db.collection("users") }
    protected open val database = db

    open suspend fun saveGroup(group: Group, userId: String): Group = withContext(Dispatchers.IO) {
        val groupRef = groupsCollection.document(group.groupId)
        val userRef = usersCollection.document(userId)

        database.runTransaction { transaction ->
            transaction.set(groupRef, group)
            transaction.update(userRef, "groupIds", com.google.cloud.firestore.FieldValue.arrayUnion(group.groupId))
            return@runTransaction group
        }.get()
    }

    open suspend fun existsById(groupId: String): Boolean = withContext(Dispatchers.IO) {
        groupsCollection.document(groupId).get().get().exists()
    }

    open suspend fun existsByInviteCode(inviteCode: String): Boolean = withContext(Dispatchers.IO) {
        val querySnapshot = groupsCollection
            .whereEqualTo("inviteCode", inviteCode)
            .limit(1)
            .get()
            .get()
        return@withContext !querySnapshot.isEmpty
    }

    open suspend fun getGroupById(groupId: String): Group? = withContext(Dispatchers.IO) {
        val doc = groupsCollection.document(groupId).get().get()
        return@withContext if (doc.exists()) doc.toObject(Group::class.java) else null
    }

    open suspend fun getGroupByInviteCode(inviteCode: String): Group? = withContext(Dispatchers.IO) {
        val querySnapshot = groupsCollection.whereEqualTo("inviteCode", inviteCode).get().get()
        val doc = querySnapshot.documents.firstOrNull()
        return@withContext if (doc != null && doc.exists()) doc.toObject(Group::class.java) else null
    }

    open suspend fun fetchGroupOrThrow(groupId: String): Group = withContext(Dispatchers.IO) {
        getGroupById(groupId) ?: throw NotFoundException("Group not found with ID: $groupId")
    }

    open suspend fun addMemberToGroup(group: Group, user: User): Group = withContext(Dispatchers.IO) {
        val groupRef = groupsCollection.document(group.groupId)
        val userRef = usersCollection.document(user.userId)

        database.runTransaction { transaction ->
            val updatedMembers = group.memberIds + user.userId
            transaction.update(groupRef, "memberIds", updatedMembers)

            val updatedGroup = group.copy(memberIds = updatedMembers)
            transaction.update(userRef, "groupIds", user.groupIds + group.groupId)

            return@runTransaction updatedGroup
        }.get()
    }

    open suspend fun removeMemberFromGroup(group: Group, user: User): Boolean = withContext(Dispatchers.IO) {
        val groupRef = groupsCollection.document(group.groupId)
        val userRef = usersCollection.document(user.userId)

        database.runTransaction { transaction ->
            val updatedMemberIds = group.memberIds.filterNot { it == user.userId }
            val updatedGroupIds = user.groupIds.filterNot { it == group.groupId }
            val groupWasDeleted = updatedMemberIds.isEmpty()

            if (groupWasDeleted)
                transaction.delete(groupRef)
            else
                transaction.update(groupRef, "memberIds", updatedMemberIds)

            transaction.update(userRef, "groupIds", updatedGroupIds)

            return@runTransaction groupWasDeleted
        }.get()
    }
}