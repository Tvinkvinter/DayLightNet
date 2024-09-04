package com.atarusov.daylightnet.data

import com.atarusov.daylightnet.model.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class UsersRemoteDataSource {
    private val firestore = Firebase.firestore

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    init {
        firestore.collection("users").addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            val users = value?.documents?.mapNotNull { it.toObject<User>() } ?: emptyList()
            _users.value = users
        }
    }

    suspend fun addOrUpdateUser(user: User): Result<String> {
        return try {
            firestore.collection("users").document(user.uid).set(user).await()
            Result.success("Data about user with id ${user.uid} sucessfully set")
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun getUserDataOrNullById(userId: String): User? {
        val documentSnapshot = firestore.collection("users").document(userId).get().await()
        return documentSnapshot.data?.let { User.fromMap(it) }
    }

    suspend fun deleteUserById(userId: String): Result<String> {
        return try {
            firestore.collection("users").document(userId).delete().await()
            Result.success("Data about user with id ${userId} sucessfully deleted")
        } catch (e: Exception) {
            Result.failure<String>(e)
        }
    }
}
