package com.atarusov.daylightnet.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LogInValidationState(
    val isEmailValid: Boolean = true,
    val isPasswordValid: Boolean = true
)

class LoginViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()

    sealed class NavigationEvent {
        object NavigateToRegisterScreen : NavigationEvent()
        object NavigateToBottomNavigationScreens : NavigationEvent()
    }

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _validationStateFlow =
        MutableStateFlow<LogInValidationState>(LogInValidationState())
    val validationStateFlow: StateFlow<LogInValidationState> = _validationStateFlow.asStateFlow()

    private val _authErrorSharedFlow = MutableSharedFlow<Boolean>()
    val authErrorSharedFlow: SharedFlow<Boolean> = _authErrorSharedFlow.asSharedFlow()

    init {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            navigateToBottomNavigationScreens()
        }
    }

    fun signInWithEmailAndPassword(email: String, password: String) {

        _validationStateFlow.value = LogInValidationState(
            isEmailValid = validEmail(email),
            isPasswordValid = validPassword(password)
        )

        if (_validationStateFlow.value.isEmailValid && _validationStateFlow.value.isPasswordValid)
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) navigateToBottomNavigationScreens()
                    else viewModelScope.launch {
                        _authErrorSharedFlow.emit(true)
                    }
                }
    }

    private fun validEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun validPassword(password: String): Boolean {
        return password.length >= 6 // Min password length
    }

    fun hideEmailError() {
        _validationStateFlow.value = _validationStateFlow.value.copy(
            isEmailValid = true
        )
    }

    fun hidePasswordError() {
        _validationStateFlow.value = _validationStateFlow.value.copy(
            isPasswordValid = true
        )
    }

    fun navigateToRegisterScreen() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.NavigateToRegisterScreen)
        }
    }

    private fun navigateToBottomNavigationScreens() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.NavigateToBottomNavigationScreens)
        }
    }

}