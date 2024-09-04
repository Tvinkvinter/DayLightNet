package com.atarusov.daylightnet.data

import android.util.Log
import com.atarusov.daylightnet.model.User
import kotlinx.coroutines.flow.StateFlow

class UsersRepository(
    private val usersRemoteDataSource: UsersRemoteDataSource,
    private val authManager: AuthManager
) {
    val users: StateFlow<List<User>> = usersRemoteDataSource.users
    val currentUserId: StateFlow<String?> = authManager.current_user_id

    suspend fun getUserDataOrNullById(userId: String): User? {
        usersRemoteDataSource.getUserDataOrNullById(userId)
        return usersRemoteDataSource.getUserDataOrNullById(userId)
    }

    suspend fun getCurrentUserDataOrNull(): User? {
        return currentUserId.value?.let { usersRemoteDataSource.getUserDataOrNullById(it) }
    }

    suspend fun addUserData(user: User) {
        usersRemoteDataSource.addOrUpdateUser(user)
    }

    suspend fun updateUserData(user: User) {
        usersRemoteDataSource.addOrUpdateUser(user)
    }

    suspend fun deleteUserData(user: User) {
        usersRemoteDataSource.deleteUser(user)
    }

    suspend fun registerUser(registrationData: User.RegistrationData) {
        with(registrationData) {
            authManager.registerUser(email, password)
            currentUserId.value?.let {
                val user = User(it, firstName, lastName)
                addUserData(user)
            }
        }
    }

    suspend fun logInUser(loginData: User.LoginData): Result<String> {
        return authManager.logInUser(loginData.email, loginData.password)
    }

    suspend fun logOutCurrentUser() {
        authManager.logOutCurrentUser()
    }
}
