package com.atarusov.daylightnet.data

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

    suspend fun addUserData(user: User): Result<String> {
        return usersRemoteDataSource.addOrUpdateUser(user)
    }

    suspend fun updateUserData(user: User): Result<String> {
        return usersRemoteDataSource.addOrUpdateUser(user)
    }

    suspend fun deleteCurrentUserAccountAndData(): Result<String> {
        val user_id = currentUserId.value
        val delete_account_result = authManager.deleteCurrentUser()
        if (delete_account_result.isSuccess) user_id?.let { usersRemoteDataSource.deleteUserById(it) }

        return delete_account_result
    }

    suspend fun registerUser(registrationData: User.RegistrationData): Result<String> {
        with(registrationData) {
            val auth_result = authManager.registerUser(email, password)
            var add_data_result = Result.failure<String>(
                Exception("User account hasn't been created, so user data is not added to database")
            )

            currentUserId.value?.let {
                val user = User(it, firstName, lastName)
                add_data_result = addUserData(user)
            }

            if (auth_result.isFailure) return auth_result
            else return add_data_result
        }
    }

    suspend fun logInUser(loginData: User.LoginData): Result<String> {
        return authManager.logInUser(loginData.email, loginData.password)
    }

    suspend fun logOutCurrentUser(): Result<String> {
        return authManager.logOutCurrentUser()
    }
}
