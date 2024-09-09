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

    suspend fun likePost(post: Post, userId: String): Result<String> {
        post.idsOfUsersLiked.add(userId)
        return postsRemoteDataSource.addOrUpdatePost(post)
    }

    suspend fun unlikePost(post: Post, userId: String): Result<String> {
        post.idsOfUsersLiked.remove(userId)
        return postsRemoteDataSource.addOrUpdatePost(post)
    }

    suspend fun deletePost(postId: String): Result<String> {
        return postsRemoteDataSource.deletePost(postId)
    }
}