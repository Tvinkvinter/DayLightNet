package com.atarusov.daylightnet.data

import com.atarusov.daylightnet.model.User
import com.google.firebase.Firebase
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

    suspend fun addOrUpdateUser(user: User) {
        firestore.collection("users").document(user.uid).set(user).await()
    }

    suspend fun getUserDataOrNullById(userId: String): User? {
        val documentSnapshot = firestore.collection("users").document(userId).get().await()
        return documentSnapshot.data?.let { User.fromMap(it) }
    }

    suspend fun deleteUser(user: User) {
        firestore.collection("users").document(user.uid).delete().await()
    }
}
