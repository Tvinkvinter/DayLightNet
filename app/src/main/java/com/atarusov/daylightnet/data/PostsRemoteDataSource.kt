package com.atarusov.daylightnet.data

import com.atarusov.daylightnet.model.Post
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await


class PostsRemoteDataSource {
    private val firestore = Firebase.firestore

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    init {
        firestore.collection("posts").addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            val posts = value?.documents?.mapNotNull { it.toObject<Post>() } ?: emptyList()
            _posts.value = posts
        }
    }

    suspend fun addOrUpdatePost(post: Post) {
        firestore.collection("posts").document(post.id).set(post).await()
    }

    suspend fun deletePost(postId: String) {
        firestore.collection("posts").document(postId).delete().await()
    }
}