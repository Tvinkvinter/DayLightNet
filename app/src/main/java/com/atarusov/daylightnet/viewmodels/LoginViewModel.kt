package com.atarusov.daylightnet.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atarusov.daylightnet.data.PostsRepository
import com.atarusov.daylightnet.data.UsersRepository
import com.atarusov.daylightnet.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class LogInValidationState(
    val isEmailValid: Boolean = true,
    val isPasswordValid: Boolean = true
)

class LoginViewModel(
    private val usersRepository: UsersRepository
) : ViewModel() {

    sealed class NavigationEvent {
        object NavigateToRegisterScreen : NavigationEvent()
        object NavigateToBottomNavigationScreens : NavigationEvent()
    }

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent

    private val _validationStateFlow =
        MutableStateFlow<LogInValidationState>(LogInValidationState())
    val validationStateFlow: StateFlow<LogInValidationState> = _validationStateFlow

    private val _authErrorSharedFlow = MutableSharedFlow<Exception>()
    val authErrorSharedFlow: SharedFlow<Exception> = _authErrorSharedFlow

    fun signInWithEmailAndPassword(loginData: User.LoginData) {

        _validationStateFlow.value = LogInValidationState(
            isEmailValid = validEmail(loginData.email),
            isPasswordValid = validPassword(loginData.password)
        )

        if (_validationStateFlow.value.isEmailValid && _validationStateFlow.value.isPasswordValid)
            viewModelScope.launch(Dispatchers.IO) {
                val result = usersRepository.logInUser(loginData)

                withContext(Dispatchers.Main) {
                    if (result.isSuccess) navigateToBottomNavigationScreens()
                    else _authErrorSharedFlow.emit(result.exceptionOrNull() as Exception)
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

    class Factory @Inject constructor(
        private val usersRepository: UsersRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass == LoginViewModel::class.java) {
                return LoginViewModel(usersRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}