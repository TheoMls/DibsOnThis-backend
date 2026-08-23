package com.theomls.dibs.repositories

import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Transaction
import com.theomls.dibs.models.Group
import com.theomls.dibs.models.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class TaskRepository (db: Firestore) {
    protected open val tasksCollection: CollectionReference by lazy { db.collection("tasks") }
    protected open val groupsCollection: CollectionReference by lazy { db.collection("groups") }
    protected open val usersCollection: CollectionReference by lazy { db.collection("users") }
    protected open val database = db

    open suspend fun existsById(taskId: String): Boolean = withContext(Dispatchers.IO) {
        tasksCollection.document(taskId).get().get().exists()
    }

    open suspend fun saveTask(newTask: Task, userId: String): Task = withContext(Dispatchers.IO) {
        val taskRef = tasksCollection.document(newTask.taskId)
        val groupRef = groupsCollection.document(newTask.groupId)
        val userRef = usersCollection.document(userId)

        database.runTransaction { transaction ->
            // Verify user exists
            UserRepository.fetchUserOrThrow(transaction, userRef)

            // Verify group exists
            val group = GroupRepository.fetchGroupOrThrow(transaction, groupRef)

            // Verify task does not exist
            val taskSnapshot = transaction.get(taskRef).get()
            if (taskSnapshot.exists()) {
                throw IllegalArgumentException("Task already exists")
            }

            // Verify user is member of group
            if (!group.memberIds.contains(userId)) {
                throw IllegalArgumentException("User is not a member of this group")
            }

            // Save the task
            transaction.set(taskRef, newTask)

            return@runTransaction newTask
        }.get()
    }

    open suspend fun getTaskById(taskId: String): Task? = withContext(Dispatchers.IO) {
        val doc = tasksCollection.document(taskId).get().get()
        return@withContext if (doc != null && doc.exists()) doc.toObject(Task::class.java) else null
    }

    open suspend fun getTasksByGroupId(groupId: String): List<Task> = withContext(Dispatchers.IO) {
        val querySnapshot = tasksCollection.whereEqualTo("groupId", groupId).get().get()
        return@withContext querySnapshot.documents.mapNotNull { it.toObject(Task::class.java) }
    }

    companion object {
        fun fetchTaskOrThrow(
            transaction: Transaction,
            taskRef: DocumentReference
        ): Task {
            val taskSnapshot = transaction.get(taskRef).get()
            if (!taskSnapshot.exists()) {
                throw IllegalArgumentException("Task does not exist")
            }
            return taskSnapshot.toObject(Task::class.java)
                ?: throw IllegalArgumentException("Task data is invalid")
        }
    }
}