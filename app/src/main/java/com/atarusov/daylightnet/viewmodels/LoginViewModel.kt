package com.atarusov.daylightnet.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.atarusov.App
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

data class LogInValidationState(
    val isEmailValid: Boolean = true,
    val isPasswordValid: Boolean = true
)

class LoginViewModel(
    private val postsRepository: PostsRepository,
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

    var currentUserData: User? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            usersRepository.currentUserId.collect {
                currentUserData = usersRepository.getCurrentUserDataOrNull()
            }
        }
    }

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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val postsRepository = (this[APPLICATION_KEY] as App).postsRepository
                val usersRepository = (this[APPLICATION_KEY] as App).usersRepository

                LoginViewModel(
                    postsRepository = postsRepository,
                    usersRepository = usersRepository
                )
            }
        }
    }
}