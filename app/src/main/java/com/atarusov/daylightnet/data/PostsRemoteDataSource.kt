package com.atarusov.daylightnet.data

import android.util.Log
import com.atarusov.daylightnet.model.Post
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await


class PostsRemoteDataSource {
    private val firestore = Firebase.firestore

    private val _posts = MutableStateFlow<List<Post>?>(null)
    val posts: StateFlow<List<Post>?> = _posts

    init {
        firestore.collection("posts").addSnapshotListener { value, error ->
            Log.d(TAG, "Attempt to retrieve posts from firestore")
            if (error != null) {
                Log.e(TAG, "Error retrieving posts", error)
                return@addSnapshotListener
            }

            Log.d(TAG, "Posts retrieved successfully")
            val posts = value?.documents?.mapNotNull { it.toObject<Post>() } ?: emptyList()
            _posts.value = posts

            if (posts.isEmpty()) Log.w(TAG, "List of posts is empty")
        }
    }

    suspend fun addOrUpdatePost(post: Post): Result<String> {
        return try {
            Log.d(TAG, "Attempt to set post with ID ${post.id} to firestore")
            firestore.collection("posts").document(post.id).set(post).await()
            Result.success("Post with ID ${post.id} is successfully set").also {
                Log.d(TAG, it.toString())
            }
        } catch (e: Exception) {
            Result.failure<String>(e).also {
                Log.e(TAG, "Error setting post with ID ${post.id}", e)
            }
        }
    }

    suspend fun deletePost(postId: String): Result<String> {
        return try {
            Log.d(TAG, "Attempt to delete post with ID $postId from firestore")
            firestore.collection("posts").document(postId).delete().await()
            Result.success("Post with ID ${postId} is successfully deleted").also {
                Log.d(TAG, it.toString())
            }
        } catch (e: Exception) {
            Result.failure<String>(e).also {
                Log.d(TAG, it.toString())
            }
        }
    }

    companion object {
        val TAG = PostsRemoteDataSource::class.java.simpleName
    }
}