package com.atarusov.daylightnet.data

import com.atarusov.daylightnet.model.Post
import kotlinx.coroutines.flow.StateFlow

class PostsRepository(
    private val postsRemoteDataSource: PostsRemoteDataSource
) {
    val posts: StateFlow<List<Post>> = postsRemoteDataSource.posts

    suspend fun addPost(post: Post): Result<String> {
        return postsRemoteDataSource.addOrUpdatePost(post)
    }

    suspend fun updatePost(post: Post): Result<String> {
        return postsRemoteDataSource.addOrUpdatePost(post)
    }

    suspend fun likePost(post: Post, userId: String): Result<String> {
        val updated_post = post.copy(
            likes = post.likes + 1
        )
        updated_post.idsOfUsersLiked.add(userId)
        return updatePost(updated_post)
    }

    suspend fun unlikePost(post: Post, userId: String): Result<String> {
        val updated_post = post.copy(
            likes = post.likes - 1
        )
        updated_post.idsOfUsersLiked.remove(userId)
        return updatePost(updated_post)
    }

    suspend fun deletePost(postId: String): Result<String> {
        return postsRemoteDataSource.deletePost(postId)
    }
}