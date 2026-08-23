package com.theomls.dibs.repositories

import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Transaction
import com.theomls.dibs.models.Group
import com.theomls.dibs.models.Task
import com.theomls.dibs.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskRepository (db: Firestore) {
    private val tasksCollection: CollectionReference = db.collection("tasks")
    private val groupsCollection: CollectionReference = db.collection("groups")
    private val usersCollection: CollectionReference = db.collection("users")
    private val database = db

    suspend fun existsById(taskId: String): Boolean = withContext(Dispatchers.IO) {
        tasksCollection.document(taskId).get().get().exists()
    }

    suspend fun saveTask(newTask: Task, userId: String): Task = withContext(Dispatchers.IO) {
        val taskRef = tasksCollection.document(newTask.taskId)
        val groupRef = groupsCollection.document(newTask.groupId)
        val userRef = usersCollection.document(userId)

        database.runTransaction { transaction ->
            // Verify user exists
            getUserFromTransaction(transaction, userRef)

            // Verify group exists
            val groupSnapshot = transaction.get(groupRef).get()
            if (!groupSnapshot.exists()) {
                throw IllegalArgumentException("Group does not exist")
            }
            val group = groupSnapshot.toObject(Group::class.java)
                ?: throw IllegalArgumentException("Group data is invalid")

            // Verify user is member of group
            if (!group.memberIds.contains(userId)) {
                throw IllegalArgumentException("User is not a member of this group")
            }

            // Save the task
            transaction.set(taskRef, newTask)

            return@runTransaction newTask
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
}