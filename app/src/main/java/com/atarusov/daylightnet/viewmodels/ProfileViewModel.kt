package com.atarusov.daylightnet.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atarusov.daylightnet.model.User
import com.atarusov.daylightnet.viewmodels.LoginViewModel.NavigationEvent
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = Firebase.firestore

    sealed class NavigationEvent {
        object NavigateToLoginScreen : NavigationEvent()
        object NavigateBack : NavigationEvent()
    }

    private val _navigationEvent =
        MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _currentUserData = MutableStateFlow<User?>(null)
    val currentUserData: StateFlow<User?> = _currentUserData.asStateFlow()

    init {
        fetchCurrentUserData()
    }

    fun fetchCurrentUserData() {
        val currentUserUid = firebaseAuth.uid
        if (currentUserUid != null) {
            database.collection("users").document(currentUserUid).get()
                .addOnCompleteListener { document ->
                    _currentUserData.value = document.result.data?.let { User.fromMap(it) }
                }
        }
    }

    fun signOut(){
        firebaseAuth.signOut()
        navigateToLoginScreen()
    }

    private fun navigateToLoginScreen() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.NavigateToLoginScreen)
        }
    }

}