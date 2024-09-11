package com.atarusov.daylightnet.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

object AuthManager {
    val TAG = AuthManager.javaClass.simpleName//Class.forName("AuthManager").kotlin.simpleName

    val firebaseAuth = Firebase.auth

    private val _current_user_id = MutableStateFlow<String?>(null)
    val current_user_id: StateFlow<String?> = _current_user_id

    init {
        firebaseAuth.addAuthStateListener {
            _current_user_id.value = firebaseAuth.uid
            Log.d(TAG, "UID has been changed to ${_current_user_id.value}")
        }
    }

    suspend fun logInUser(email: String, password: String): Result<String> {
        return try {
            Log.d(TAG, "Attempt to sign in with email: $email")
            firebaseAuth.signInWithEmailAndPassword(email, password).await()

            current_user_id.value?.let {
                Result.success("User with ID $it is sucessfully signed in")
                    .also { Log.d(TAG, it.toString()) }
            } ?: Result.failure<String>(Exception("User ID is null after signing in"))
                .also { Log.e(TAG, it.toString()) }

        } catch (e: Exception) {
            Result.failure<String>(e).also { Log.e(TAG, it.toString()) }
        }
    }

    suspend fun logOutCurrentUser(): Result<String> {
        return try {
            Log.d(TAG, "Attempt to sign out current user")
            firebaseAuth.signOut()
            Result.success("User is successfully signed out").also { Log.d(TAG, it.toString()) }
        } catch (e: Exception) {
            Result.failure<String>(e).also { Log.e(TAG, it.toString()) }
        }
    }

    suspend fun registerUser(email: String, password: String): Result<String> {
        return try {
            Log.d(TAG, "Attempt to create new user with email: $email")
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()

            current_user_id.value?.let { Result.success(it).also { Log.d(TAG, it.toString()) } }
                ?: Result.failure<String>(Exception("User ID is null after registration"))
                    .also { Log.w(TAG, it.toString()) }

        } catch (e: Exception) {
            Result.failure<String>(e).also { Log.e(TAG, it.toString()) }
        }
    }

    suspend fun deleteCurrentUser(): Result<String> {
        return try {
            Log.d(TAG, "Attempt to delete current user account")
            firebaseAuth.currentUser?.delete()?.await()
            Result.success("User account is successfully deleted")
                .also { Log.d(TAG, it.toString()) }
        } catch (e: Exception) {
            Result.failure<String>(e).also { Log.e(TAG, it.toString()) }
        }
    }
}