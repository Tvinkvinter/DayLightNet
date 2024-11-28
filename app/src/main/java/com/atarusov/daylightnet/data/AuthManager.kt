package com.atarusov.daylightnet.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userSessionManager: UserSessionManager
) {
    val TAG = AuthManager::class.java.simpleName

    suspend fun logInUser(email: String, password: String): Result<String> {
        return try {
            Log.d(TAG, "Attempt to sign in with email: $email")
            firebaseAuth.signInWithEmailAndPassword(email, password).await()

            userSessionManager.current_user_id.value?.let {
                Result.success("User with ID $it is successfully signed in")
                    .also { Log.d(TAG, it.toString()) }
            } ?: Result.failure<String>(Exception("User ID is null after signing in"))
                .also { Log.e(TAG, it.toString()) }

        } catch (e: Exception) {
            Result.failure<String>(e).also { Log.e(TAG, it.toString()) }
        }
    }

    suspend fun registerUser(email: String, password: String): Result<String> {
        return try {
            Log.d(TAG, "Attempt to create new user with email: $email")
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()

            userSessionManager.current_user_id.value?.let {
                Result.success(it).also { Log.d(TAG, it.toString()) }
            } ?: Result.failure<String>(Exception("User ID is null after registration"))
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