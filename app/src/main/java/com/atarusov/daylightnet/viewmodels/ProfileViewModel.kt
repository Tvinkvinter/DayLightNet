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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProfileViewModel(
    private val usersRepository: UsersRepository
) : ViewModel() {

    sealed class NavigationEvent {
        data object NavigateToLoginScreen : NavigationEvent()
        data object NavigateBack : NavigationEvent()
    }

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent

    private val _currentUserData = MutableStateFlow<User?>(null)
    val currentUserData = _currentUserData

    private val _signOutErrorSharedFlow = MutableSharedFlow<Exception>()
    val signOutErrorSharedFlow: SharedFlow<Exception> = _signOutErrorSharedFlow


    init {
        viewModelScope.launch(Dispatchers.IO) {
            usersRepository.currentUserId.collect {
                _currentUserData.value = usersRepository.getCurrentUserDataOrNull()
            }
        }
    }

    fun signOut() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = usersRepository.logOutCurrentUser()

            withContext(Dispatchers.Main) {
                if (result.isSuccess) navigateToLoginScreen()
                else _signOutErrorSharedFlow.emit(result.exceptionOrNull() as Exception)
            }
        }
    }

    private fun navigateToLoginScreen() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.NavigateToLoginScreen)
        }
    }

    class Factory @Inject constructor(private val usersRepository: UsersRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass == ProfileViewModel::class.java) {
                return ProfileViewModel(usersRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}