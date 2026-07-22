package com.theomls.dibs.repositories

import com.google.cloud.firestore.Firestore
import com.theomls.dibs.models.User

class UserRepository(private val db: Firestore) {

    fun saveUser(user: User) {
        try {
            db.collection("users")
                .document(user.id)
                .set(user)
                .get()
        } catch (e: Exception) {
            throw RuntimeException("Failed to save user", e)
        }
    }

    fun findUserById(id: String): User? {
        try {
            val snapshot = db.collection("users").document(id).get().get()
            return if (snapshot.exists()) snapshot.toObject(User::class.java) else null
        } catch (e: Exception) {
            throw RuntimeException("Failed to find user by id", e)
        }
    }
}