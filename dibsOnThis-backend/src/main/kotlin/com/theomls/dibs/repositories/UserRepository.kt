package com.theomls.dibs.repositories

import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Transaction
import com.theomls.dibs.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class UserRepository(db: Firestore) {
    protected open val database = db

    open suspend fun saveUser(user: User): User = withContext(Dispatchers.IO) {
        val userRef = database.collection("users").document(user.id)

        database.runTransaction { transaction ->
            val userSnapshot = transaction.get(userRef).get()
            if (userSnapshot.exists()) {
                throw IllegalArgumentException("User profile already exists!")
            }
            transaction.set(userRef, user)
            return@runTransaction user
        }.get()
    }

    open suspend fun getUserById(id: String): User? = withContext(Dispatchers.IO) {
        val snapshot: DocumentSnapshot = database.collection("users").document(id).get().get()
        return@withContext if (snapshot.exists()) snapshot.toObject(User::class.java) else null
    }

    open suspend fun existsById(id: String): Boolean = withContext(Dispatchers.IO) {
        database.collection("users").document(id).get().get().exists()
    }

    companion object {
        fun fetchUserOrThrow(
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
}