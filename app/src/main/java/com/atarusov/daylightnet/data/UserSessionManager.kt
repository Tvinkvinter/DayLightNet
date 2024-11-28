package com.atarusov.daylightnet.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface UserSessionManager {
    val current_user_id: StateFlow<String?>
    fun logOut(): Result<String>
    fun isUserLoggedIn(): Boolean
}

@Singleton
class UserSessionManagerImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
): UserSessionManager {
    val TAG = AuthManager::class.java.simpleName

    private val _current_user_id = MutableStateFlow<String?>(null)
    override val current_user_id: StateFlow<String?> = _current_user_id

    init {
        firebaseAuth.addAuthStateListener {
            _current_user_id.value = firebaseAuth.uid
            Log.d(TAG, "UID has been changed to ${_current_user_id.value}")
        }
    }

    override fun logOut(): Result<String> {
        return try {
            firebaseAuth.signOut()
            Result.success("User successfully signed out").also {
                Log.d("UserSessionManager", it.toString())
            }
        } catch (e: Exception) {
            Result.failure<String>(e).also { Log.e("UserSessionManager", it.toString()) }
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return _current_user_id.value != null
    }
}