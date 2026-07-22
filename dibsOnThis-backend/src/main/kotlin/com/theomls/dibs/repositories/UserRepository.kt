package com.theomls.dibs.repositories

import com.google.cloud.firestore.Firestore
import com.theomls.dibs.models.User

class UserRepository(private val db: Firestore) {

    fun saveUser(user: User) {
        // Writes the Kotlin User object directly into the Firestore "users" collection
        db.collection("users")
            .document(user.id)
            .set(user)
    }

    fun findUserById(id: String): User? {
        val snapshot = db.collection("users").document(id).get().get()
        return if (snapshot.exists()) snapshot.toObject(User::class.java) else null
    }
}