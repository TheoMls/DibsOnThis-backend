package com.theomls.dibs.repositories

import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.Firestore
import com.theomls.dibs.models.Task
import com.theomls.dibs.models.TaskStatus
import io.ktor.server.plugins.NotFoundException
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

    open suspend fun saveTask(newTask: Task): Task = withContext(Dispatchers.IO) {
        tasksCollection.document(newTask.taskId).set(newTask).get()
        return@withContext newTask
    }

    open suspend fun getTaskById(taskId: String): Task? = withContext(Dispatchers.IO) {
        val doc = tasksCollection.document(taskId).get().get()
        return@withContext if (doc != null && doc.exists()) doc.toObject(Task::class.java) else null
    }

    open suspend fun getTasksByGroupId(groupId: String): List<Task> = withContext(Dispatchers.IO) {
        val querySnapshot = tasksCollection.whereEqualTo("groupId", groupId).get().get()
        return@withContext querySnapshot.documents.mapNotNull { it.toObject(Task::class.java) }
    }

    open suspend fun fetchTaskOrThrow(taskId: String): Task = withContext(Dispatchers.IO) {
        getTaskById(taskId) ?: throw NotFoundException("Task not found")
    }

    open suspend fun claimTask(task: Task, userId: String): Task = withContext(Dispatchers.IO) {
        val taskRef = tasksCollection.document(task.taskId)

        database.runTransaction { transaction ->
            transaction.update(taskRef, "assignedToUserId", userId)
            transaction.update(taskRef, "status", TaskStatus.CLAIMED)
            return@runTransaction task.copy(assignedToUserId = userId, status = TaskStatus.CLAIMED)
        }.get()
    }

    open suspend fun unclaimTask(task: Task, userId: String): Task = withContext(Dispatchers.IO) {
        val taskRef = tasksCollection.document(task.taskId)

        database.runTransaction { transaction ->
            transaction.update(taskRef, "assignedToUserId", null)
            transaction.update(taskRef, "status", TaskStatus.OPEN)
            return@runTransaction task.copy(assignedToUserId = null, status = TaskStatus.OPEN)
        }.get()
    }

    open suspend fun completeTask(task: Task, userId: String): Task = withContext(Dispatchers.IO) {
        val taskRef = tasksCollection.document(task.taskId)

        database.runTransaction { transaction ->
            transaction.update(taskRef, "status", TaskStatus.COMPLETED)
            transaction.update(taskRef, "completedAt", System.currentTimeMillis())
            return@runTransaction task.copy(status = TaskStatus.COMPLETED, completedAt = System.currentTimeMillis())
        }.get()
    }

    open suspend fun deleteTask(task: Task, userId: String): Boolean = withContext(Dispatchers.IO) {
        val taskRef = tasksCollection.document(task.taskId)
        database.runTransaction { transaction ->
            transaction.delete(taskRef)
        }.get()

        return@withContext true
    }
}