package com.atarusov.daylightnet.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.atarusov.App
import com.atarusov.daylightnet.data.UsersRepository
import com.atarusov.daylightnet.model.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val usersRepository: UsersRepository
) : ViewModel() {

    sealed class NavigationEvent {
        object NavigateToLoginScreen : NavigationEvent()
        object NavigateBack : NavigationEvent()
    }

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent

    private val _currentUserData = MutableStateFlow<User?>(null)
    val currentUserData = _currentUserData.asStateFlow()


    init {
        viewModelScope.launch {
            _currentUserData.value = usersRepository.getCurrentUserDataOrNull()
            usersRepository.currentUserId.collect {
                _currentUserData.value = usersRepository.getCurrentUserDataOrNull()
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            usersRepository.logOutCurrentUser()
        }
        navigateToLoginScreen()
    }

    private fun navigateToLoginScreen() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.NavigateToLoginScreen)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val usersRepository = (this[APPLICATION_KEY] as App).usersRepository
                ProfileViewModel(
                    usersRepository = usersRepository
                )
            }
        }
    }
}