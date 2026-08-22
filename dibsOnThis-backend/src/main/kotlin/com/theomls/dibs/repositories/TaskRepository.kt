package com.theomls.dibs.repositories

import com.google.cloud.firestore.Firestore
import com.theomls.dibs.models.Task

class TaskRepository (private val db: Firestore) {
    fun saveTask(newTask: Task): Task {
        // TODO: Implement task saving logic
        return newTask
    }
}