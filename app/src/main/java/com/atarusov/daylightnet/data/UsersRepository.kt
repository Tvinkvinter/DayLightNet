package com.atarusov.daylightnet.data

import android.util.Log
import com.atarusov.daylightnet.model.User
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface UsersRepository {
    val currentUserId: StateFlow<String?>
    suspend fun getUserDataOrNullById(userId: String): User?
    suspend fun getCurrentUserDataOrNull(): User?
    suspend fun addUserData(user: User): Result<String>
    suspend fun updateUserData(user: User): Result<String>
    suspend fun deleteCurrentUserAccountAndData(): Result<String>
    suspend fun registerUser(registrationData: User.RegistrationData): Result<String>
    suspend fun logInUser(loginData: User.LoginData): Result<String>
    suspend fun logOutCurrentUser(): Result<String>
}

@Singleton
class UsersRepositoryImpl @Inject constructor(
    private val usersRemoteDataSource: UsersRemoteDataSource,
    private val userSessionManager: UserSessionManager,
    private val authManager: AuthManager
): UsersRepository {
    override val currentUserId: StateFlow<String?> = userSessionManager.current_user_id

    override suspend fun getUserDataOrNullById(userId: String): User? {
        usersRemoteDataSource.getUserDataOrNullById(userId)
        return usersRemoteDataSource.getUserDataOrNullById(userId)
    }

    override suspend fun getCurrentUserDataOrNull(): User? {
        return currentUserId.value?.let { usersRemoteDataSource.getUserDataOrNullById(it) }
    }

    override suspend fun addUserData(user: User): Result<String> {
        return usersRemoteDataSource.addOrUpdateUser(user)
    }

    override suspend fun updateUserData(user: User): Result<String> {
        return usersRemoteDataSource.addOrUpdateUser(user)
    }

    override suspend fun deleteCurrentUserAccountAndData(): Result<String> {
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

    override suspend fun registerUser(registrationData: User.RegistrationData): Result<String> {
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

    override suspend fun logInUser(loginData: User.LoginData): Result<String> {
        return authManager.logInUser(loginData.email, loginData.password)
    }

    override suspend fun logOutCurrentUser(): Result<String> {
        return userSessionManager.logOut()
    }

    companion object {
        val TAG = UsersRepository::class.java.simpleName
    }
}
