package com.atarusov.daylightnet.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    val TAG = AuthManager::class.java.simpleName

    private val _current_user_id = MutableStateFlow<String?>(null)
    val current_user_id: StateFlow<String?> = _current_user_id

    init {
        firebaseAuth.addAuthStateListener {
            _current_user_id.value = firebaseAuth.uid
            Log.d(TAG, "UID has been changed to ${_current_user_id.value}")
        }
    }

    fun logOut(): Result<String> {
        return try {
            firebaseAuth.signOut()
            Result.success("User successfully signed out").also {
                Log.d("UserSessionManager", it.toString())
            }
        } catch (e: Exception) {
            Result.failure<String>(e).also { Log.e("UserSessionManager", it.toString()) }
        }
    }

    fun isUserLoggedIn(): Boolean {
        return _current_user_id.value != null
    }
}