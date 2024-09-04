package com.atarusov.daylightnet.data

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

object AuthManager {
    val firebaseAuth = Firebase.auth

    private val _current_user_id = MutableStateFlow<String?>(null)
    val current_user_id: StateFlow<String?> = _current_user_id

    init {
        firebaseAuth.addAuthStateListener {
            _current_user_id.value = firebaseAuth.uid
        }
    }

    suspend fun logInUser(email: String, password: String): Result<String> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()

            current_user_id.value?.let { Result.success(it) }
                ?: Result.failure(Exception("User ID is null after login"))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logOutCurrentUser() {
        firebaseAuth.signOut()
    }

    suspend fun registerUser(email: String, password: String){
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
    }

    suspend fun deleteCurrentUser(){
        firebaseAuth.currentUser?.delete()?.await()
    }
}