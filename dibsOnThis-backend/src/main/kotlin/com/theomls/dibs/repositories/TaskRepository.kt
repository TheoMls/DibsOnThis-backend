package com.theomls.dibs.repositories

import com.google.cloud.firestore.Firestore
import com.theomls.dibs.models.Task

class TaskRepository (private val db: Firestore) {
    suspend fun existsById(taskId: String): Boolean {
        // TODO: Implement actual firestore check
        return false
    }

    fun saveTask(newTask: Task): Task {
        // TODO: Implement task saving logic
        return newTask
    }
}