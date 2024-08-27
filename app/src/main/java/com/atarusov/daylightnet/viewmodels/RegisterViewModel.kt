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

data class RegisterValidationState(
    val isFirstNameValid: Boolean = true,
    val isLastNameValid: Boolean = true,
    val isEmailValid: Boolean = true,
    val isPasswordValid: Boolean = true,
    val isRepeatPasswordValid: Boolean = true
)

class RegisterViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = Firebase.firestore

    sealed class NavigationEvent {
        object NavigateToBottomNavigationScreens : NavigationEvent()
        object NavigateBack : NavigationEvent()
    }

    private val _navigationEvent =
        MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _validationStateFlow =
        MutableStateFlow<RegisterValidationState>(RegisterValidationState())
    val validationStateFlow: StateFlow<RegisterValidationState> = _validationStateFlow.asStateFlow()

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

        if (allDataValid())
            firebaseAuth.createUserWithEmailAndPassword(
                registrationData.email,
                registrationData.password
            )
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        navigateToBottomNavigationScreens()
                        val userUid = firebaseAuth.uid
                        if (userUid != null) {
                            val newUser =
                                User(userUid, registrationData.firstName, registrationData.lastName)
                            addUserToDB(newUser)
                        }
                    }
                }
    }

    private fun addUserToDB(user: User) {
        database.collection("users").document(user.uid).set(user)
    }

    private fun allDataValid(): Boolean {
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
}