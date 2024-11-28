package com.atarusov.daylightnet.data

import android.util.Log
import com.atarusov.daylightnet.model.User
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepository @Inject constructor(
    private val usersRemoteDataSource: UsersRemoteDataSource,
    private val userSessionManager: UserSessionManager,
    private val authManager: AuthManager
) {
    val currentUserId: StateFlow<String?> = userSessionManager.current_user_id

    suspend fun getUserDataOrNullById(userId: String): User? {
        usersRemoteDataSource.getUserDataOrNullById(userId)
        return usersRemoteDataSource.getUserDataOrNullById(userId)
    }

    suspend fun getCurrentUserDataOrNull(): User? {
        return currentUserId.value?.let { usersRemoteDataSource.getUserDataOrNullById(it) }
    }

    suspend fun addUserData(user: User): Result<String> {
        return usersRemoteDataSource.addOrUpdateUser(user)
    }

    suspend fun updateUserData(user: User): Result<String> {
        return usersRemoteDataSource.addOrUpdateUser(user)
    }

    suspend fun deleteCurrentUserAccountAndData(): Result<String> {
        val delete_account_result = authManager.deleteCurrentUser()
        if (delete_account_result.isSuccess)
            if (userSessionManager.isUserLoggedIn()) {
                usersRemoteDataSource.deleteUserById(currentUserId.value.toString())
            } else Log.d(
                TAG,
                "User account hasn't been deleted, so data deleting process is not started "
            )

        return delete_account_result
    }

    suspend fun registerUser(registrationData: User.RegistrationData): Result<String> {
        with(registrationData) {
            val auth_result = authManager.registerUser(email, password)
            var add_data_result = Result.failure<String>(
                Exception("User account hasn't been created, so user data is not added to database")
            )

            if (userSessionManager.isUserLoggedIn()) {
                val user = User(currentUserId.value.toString(), firstName, lastName)
                add_data_result = addUserData(user)
            }

            Log.d(TAG, auth_result.toString())
            Log.d(TAG, add_data_result.toString())

            if (auth_result.isFailure) return auth_result
            else return add_data_result
        }
    }

    suspend fun logInUser(loginData: User.LoginData): Result<String> {
        return authManager.logInUser(loginData.email, loginData.password)
    }

    suspend fun logOutCurrentUser(): Result<String> {
        return userSessionManager.logOut()
    }

    companion object {
        val TAG = UsersRepository::class.java.simpleName
    }
}
