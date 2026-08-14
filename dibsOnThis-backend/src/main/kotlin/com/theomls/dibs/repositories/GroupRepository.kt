package com.theomls.dibs.repositories

import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.Firestore
import com.theomls.dibs.models.Group
import com.theomls.dibs.models.User
import com.theomls.dibs.models.UserConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class GroupRepository(db: Firestore) {
    protected open val groupsCollection: CollectionReference by lazy { db.collection("groups") }
    protected open val usersCollection: CollectionReference by lazy { db.collection("users") }
    protected open val database = db

    open suspend fun createGroup(group: Group, userId: String): Group = withContext(Dispatchers.IO) {
        try {
            val groupRef = groupsCollection.document(group.groupId)
            val userRef = usersCollection.document(userId)

            // Check if user exists and group does not exist, then update user and group
            database.runTransaction { transaction ->
                val userSnapshot = transaction.get(userRef).get()
                if (!userSnapshot.exists()) {
                    throw IllegalArgumentException("User does not exist")
                }

                val groupSnapshot = transaction.get(groupRef).get()
                if (groupSnapshot.exists()) {
                    throw IllegalArgumentException("Group already exists")
                }

                val user = userSnapshot.toObject(User::class.java)
                    ?: throw IllegalArgumentException("User data is invalid")

                val updatedGroupIds = if (user.groupIds.contains(group.groupId)) {
                    user.groupIds
                } else {
                    if (user.groupIds.size >= UserConstants.MAX_GROUP_MEMBERSHIPS) {
                        throw IllegalArgumentException("User cannot join more than ${UserConstants.MAX_GROUP_MEMBERSHIPS} groups.")
                    }
                    user.groupIds + group.groupId
                }

                transaction.set(groupRef, group)
                transaction.update(userRef, "groupIds", updatedGroupIds)

                return@runTransaction group
            }.get()
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException("Failed to create group and add it to user", e)
        }
    }

    open suspend fun existsById(groupId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            groupsCollection.document(groupId).get().get().exists()
        } catch (e: Exception) {
            throw RuntimeException("Failed to check if group exists", e)
        }
    }

    open suspend fun existsByInviteCode(inviteCode: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = groupsCollection
                .whereEqualTo("inviteCode", inviteCode)
                .limit(1)
                .get()
                .get()
            return@withContext !querySnapshot.isEmpty
        } catch (e: Exception) {
            throw RuntimeException("Failed to check if invite code exists", e)
        }
    }

    open suspend fun getGroupById(groupId: String): Group? = withContext(Dispatchers.IO) {
        try {
            val doc = groupsCollection.document(groupId).get().get()
            return@withContext if (doc.exists()) doc.toObject(Group::class.java) else null
        } catch (e: Exception) {
            throw RuntimeException("Failed to get group by id", e)
        }
    }

    open suspend fun getGroupByInviteCode(inviteCode: String): Group? = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = groupsCollection.whereEqualTo("inviteCode", inviteCode).get().get()
            val doc = querySnapshot.documents.firstOrNull()
            return@withContext if (doc != null && doc.exists()) doc.toObject(Group::class.java) else null
        } catch (e: Exception) {
            throw RuntimeException("Failed to get group by invite code", e)
        }
    }

    open suspend fun addMemberToGroup(groupId: String, userId: String): Group = withContext(Dispatchers.IO) {
        try {
            val groupRef = groupsCollection.document(groupId)
            val userRef = usersCollection.document(userId)

            database.runTransaction { transaction ->
                val groupSnapshot = transaction.get(groupRef).get()
                if (!groupSnapshot.exists()) {
                    throw IllegalArgumentException("Group does not exist")
                }

                val group = groupSnapshot.toObject(Group::class.java)
                    ?: throw IllegalArgumentException("Group data is invalid")

                val userSnapshot = transaction.get(userRef).get()
                if (!userSnapshot.exists()) {
                    throw IllegalArgumentException("User does not exist")
                }

                val user = userSnapshot.toObject(User::class.java)
                    ?: throw IllegalArgumentException("User data is invalid")

                if (!user.groupIds.contains(groupId) && user.groupIds.size >= UserConstants.MAX_GROUP_MEMBERSHIPS) {
                    throw IllegalArgumentException("User cannot join more than ${UserConstants.MAX_GROUP_MEMBERSHIPS} groups.")
                }

                val updatedGroup = if (group.memberIds.contains(userId)) {
                    return@runTransaction group
                } else {
                    if (group.memberIds.size >= group.maxGroupSize) {
                        throw IllegalArgumentException("Group is full.")
                    }

                    val updatedMembers = group.memberIds + userId
                    transaction.update(groupRef, "memberIds", updatedMembers)

                    group.copy(memberIds = updatedMembers)
                }

                if (!user.groupIds.contains(groupId)) {
                    transaction.update(userRef, "groupIds", user.groupIds + groupId)
                }

                return@runTransaction updatedGroup
            }.get()
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException("Failed to add member to group and user", e)
        }
    }

    open suspend fun removeMemberFromGroup(groupId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val groupRef = groupsCollection.document(groupId)
            val userRef = usersCollection.document(userId)

            database.runTransaction { transaction ->
                val groupSnapshot = transaction.get(groupRef).get()
                if (!groupSnapshot.exists()) {
                    throw IllegalArgumentException("Group does not exist")
                }

                val group = groupSnapshot.toObject(Group::class.java)
                    ?: throw IllegalArgumentException("Group data is invalid")

                val userSnapshot = transaction.get(userRef).get()
                if (!userSnapshot.exists()) {
                    throw IllegalArgumentException("User does not exist")
                }

                val user = userSnapshot.toObject(User::class.java)
                    ?: throw IllegalArgumentException("User data is invalid")

                if (!group.memberIds.contains(userId)) {
                    throw IllegalArgumentException("User is not a member of this group")
                }

                val updatedMemberIds = group.memberIds.filterNot { it == userId }
                val updatedGroupIds = user.groupIds.filterNot { it == groupId }
                val groupWasDeleted = updatedMemberIds.isEmpty()

                if (groupWasDeleted) {
                    transaction.delete(groupRef)
                } else {
                    transaction.update(groupRef, "memberIds", updatedMemberIds)
                }

                transaction.update(userRef, "groupIds", updatedGroupIds)

                return@runTransaction groupWasDeleted
            }.get()
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException("Failed to remove member from group", e)
        }
    }
}