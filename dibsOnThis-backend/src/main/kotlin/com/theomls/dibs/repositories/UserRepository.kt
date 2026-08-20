package com.theomls.dibs.repositories

import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.WriteResult
import com.theomls.dibs.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class UserRepository(db: Firestore) {
    protected open val database = db

    open suspend fun saveUser(user: User): WriteResult = withContext(Dispatchers.IO) {
        database.collection("users")
            .document(user.id)
            .set(user)
            .get()
    }

    open suspend fun getUserById(id: String): User? = withContext(Dispatchers.IO) {
        val snapshot: DocumentSnapshot = database.collection("users").document(id).get().get()
        return@withContext if (snapshot.exists()) snapshot.toObject(User::class.java) else null
    }

    open suspend fun existsById(id: String): Boolean = withContext(Dispatchers.IO) {
        database.collection("users").document(id).get().get().exists()
    }
}