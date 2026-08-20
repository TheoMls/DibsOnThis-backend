package com.theomls.dibs.repositories

import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Transaction
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
        val groupRef = groupsCollection.document(group.groupId)
        val userRef = usersCollection.document(userId)

        // Check if user exists and group does not exist, then update user and group
        database.runTransaction { transaction ->
            val user = getUserFromTransaction(transaction, userRef)

            val groupSnapshot = transaction.get(groupRef).get()
            if (groupSnapshot.exists()) {
                throw IllegalArgumentException("Group already exists")
            }

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

    open suspend fun addMemberToGroup(groupId: String, userId: String): Group = withContext(Dispatchers.IO) {
        val groupRef = groupsCollection.document(groupId)
        val userRef = usersCollection.document(userId)

        database.runTransaction { transaction ->
            val group = getGroupFromTransaction(transaction, groupRef)
            val user = getUserFromTransaction(transaction, userRef)

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
    }

    open suspend fun removeMemberFromGroup(groupId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        val groupRef = groupsCollection.document(groupId)
        val userRef = usersCollection.document(userId)

        database.runTransaction { transaction ->
            val group = getGroupFromTransaction(transaction, groupRef)
            val user = getUserFromTransaction(transaction, userRef)

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
    }

    private fun getUserFromTransaction(
        transaction: Transaction,
        userRef: DocumentReference
    ): User {
        val userSnapshot = transaction.get(userRef).get()
        if (!userSnapshot.exists()) {
            throw IllegalArgumentException("User does not exist")
        }
        return userSnapshot.toObject(User::class.java)
            ?: throw IllegalArgumentException("User data is invalid")
    }

    private fun getGroupFromTransaction(
        transaction: Transaction,
        groupRef: DocumentReference
    ): Group {
        val groupSnapshot = transaction.get(groupRef).get()
        if (!groupSnapshot.exists()) {
            throw IllegalArgumentException("Group does not exist")
        }
        return groupSnapshot.toObject(Group::class.java)
            ?: throw IllegalArgumentException("Group data is invalid")
    }
}