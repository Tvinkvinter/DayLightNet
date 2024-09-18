package com.atarusov.daylightnet.data

import android.util.Log
import com.atarusov.daylightnet.data.PostsRemoteDataSource.Companion
import com.atarusov.daylightnet.model.Post
import com.atarusov.daylightnet.model.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class UsersRemoteDataSource {
    private val firestore = Firebase.firestore

    suspend fun addOrUpdateUser(user: User): Result<String> {
        return try {
            Log.d(TAG, "Attempt to set user with ID ${user.uid} to firestore")
            firestore.collection("users").document(user.uid).set(user).await()
            Result.success("Data about user with ID ${user.uid} sucessfully set").also {
                Log.d(TAG, it.toString())
            }
        } catch (e: Exception) {
            return Result.failure<String>(e).also {
                Log.d(TAG, it.toString())
            }
        }
    }

    suspend fun getUserDataOrNullById(userId: String): User? {
        Log.d(TAG, "Attempt to retrieve data of user with ID $userId")
        val documentSnapshot = firestore.collection("users").document(userId).get().await()
        if (documentSnapshot.data == null) Log.w(
            TAG,
            "There is no data for the user with ID $userId"
        ) else Log.d(TAG, "Data about user with ID $userId sucessfully retrieved")
        return documentSnapshot.data?.let { User.fromMap(it) }
    }

    suspend fun deleteUserById(userId: String): Result<String> {
        return try {
            Log.d(TAG, "Attempt to delete data of user with ID $userId")
            firestore.collection("users").document(userId).delete().await()
            Result.success("Data about user with ID $userId sucessfully deleted").also {
                Log.d(TAG, it.toString())
            }
        } catch (e: Exception) {
            Result.failure<String>(e).also {
                Log.d(TAG, it.toString())
            }
        }
    }

    companion object {
        val TAG = UsersRemoteDataSource::class.java.simpleName
    }
}
