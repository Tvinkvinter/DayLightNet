package com.atarusov.daylightnet.data

import android.util.Log
import com.atarusov.daylightnet.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface PostsRemoteDataSource {
    val posts: StateFlow<List<Post>>
    suspend fun fetchAllPosts(): Result<String>
    suspend fun addOrUpdatePost(post: Post): Result<String>
    suspend fun deletePost(postId: String): Result<String>
}

@Singleton
class PostsRemoteDataSourceImpl @Inject constructor(private val firestore: FirebaseFirestore) :
    PostsRemoteDataSource {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    override val posts: StateFlow<List<Post>> = _posts

    override suspend fun fetchAllPosts(): Result<String> {
        return try {
            val querySnapshot = firestore.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING).get().await()
            _posts.value = querySnapshot.documents.mapNotNull { document ->
                document.toObject<Post>()
            }
            Result.success("${_posts.value.size} posts fetched from firestore").also {
                Log.d(TAG, it.toString())
            }
        } catch (e: Exception) {
            Result.failure<String>(e).also {
                Log.e(TAG, "Error fetching posts from firestore", e)
            }
        }
    }

    override suspend fun addOrUpdatePost(post: Post): Result<String> {
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

    override suspend fun deletePost(postId: String): Result<String> {
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