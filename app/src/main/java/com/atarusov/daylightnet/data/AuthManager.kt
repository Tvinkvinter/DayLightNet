package com.atarusov.daylightnet.data

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.atarusov.daylightnet.model.Post
import com.atarusov.daylightnet.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object AuthManager {
    val firebaseAuth = Firebase.auth

    private val _current_user_id = MutableStateFlow<String?>(null)
    val current_user_id: StateFlow<String?> = _current_user_id

    init {
        firebaseAuth.addAuthStateListener {
            _current_user_id.value = firebaseAuth.uid
        }
    }

    suspend fun logInUser(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
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