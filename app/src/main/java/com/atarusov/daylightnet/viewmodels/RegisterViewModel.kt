package com.atarusov.daylightnet.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atarusov.daylightnet.data.UsersRepository
import com.atarusov.daylightnet.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class RegisterValidationState(
    val isFirstNameValid: Boolean = true,
    val isLastNameValid: Boolean = true,
    val isEmailValid: Boolean = true,
    val isPasswordValid: Boolean = true,
    val isRepeatPasswordValid: Boolean = true
)

class RegisterViewModel(
    val usersRepository: UsersRepository
) : ViewModel() {

    sealed class NavigationEvent {
        object NavigateToBottomNavigationScreens : NavigationEvent()
        object NavigateBack : NavigationEvent()
    }

    private val _navigationEvent =
        MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _validationStateFlow =
        MutableStateFlow<RegisterValidationState>(RegisterValidationState())
    val validationStateFlow: StateFlow<RegisterValidationState> = _validationStateFlow

    private val _authErrorSharedFlow = MutableSharedFlow<Exception>()
    val authErrorSharedFlow: SharedFlow<Exception> = _authErrorSharedFlow

    fun signUpWithEmailAndPassword(registrationData: User.RegistrationData) {
        _validationStateFlow.value = RegisterValidationState(
            isFirstNameValid = validFirstName(registrationData.firstName),
            isLastNameValid = validLastName(registrationData.lastName),
            isEmailValid = validEmail(registrationData.email),
            isPasswordValid = validPassword(registrationData.password),
            isRepeatPasswordValid = validRepeatPassword(
                registrationData.password,
                registrationData.repeatPassword
            ),
        )

        if (isAllDataValid())
            viewModelScope.launch(Dispatchers.IO) {
                val result = usersRepository.registerUser(registrationData)

                withContext(Dispatchers.Main) {
                    if (result.isSuccess) navigateToBottomNavigationScreens()
                    else _authErrorSharedFlow.emit(result.exceptionOrNull() as Exception)
                }
            }
    }

    private fun isAllDataValid(): Boolean {
        return _validationStateFlow.value.isFirstNameValid &&
                _validationStateFlow.value.isLastNameValid &&
                _validationStateFlow.value.isEmailValid &&
                _validationStateFlow.value.isPasswordValid &&
                _validationStateFlow.value.isRepeatPasswordValid
    }

    private fun validFirstName(firstName: String): Boolean {
        return firstName.isNotEmpty()
    }

    private fun validLastName(lastName: String): Boolean {
        return lastName.isNotEmpty()
    }

    private fun validEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun validPassword(password: String): Boolean {
        return password.length >= 6 // Min password length
    }

    private fun validRepeatPassword(password: String, repeatPassword: String): Boolean {
        return password == repeatPassword
    }

    fun hideFirstNameError() {
        _validationStateFlow.value = _validationStateFlow.value.copy(
            isFirstNameValid = true
        )
    }

    fun hideLastNameError() {
        _validationStateFlow.value = _validationStateFlow.value.copy(
            isLastNameValid = true
        )
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

    fun hideRepeatPasswordError() {
        _validationStateFlow.value = _validationStateFlow.value.copy(
            isRepeatPasswordValid = true
        )
    }

    private fun navigateToBottomNavigationScreens() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.NavigateToBottomNavigationScreens)
        }
    }

    fun navigateBack() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.NavigateBack)
        }
    }

    class Factory @Inject constructor(private val usersRepository: UsersRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass == RegisterViewModel::class.java) {
                return RegisterViewModel(usersRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}