package com.atarusov.daylightnet.data

import com.atarusov.daylightnet.model.Post
import kotlinx.coroutines.flow.StateFlow

class PostsRepository(
    private val postsRemoteDataSource: PostsRemoteDataSource
) {
    val posts: StateFlow<List<Post>> = postsRemoteDataSource.posts

    suspend fun fetchAllPosts(): Result<String> {
        return postsRemoteDataSource.fetchAllPosts()
    }

    suspend fun addPost(post: Post): Result<String> {
        return postsRemoteDataSource.addOrUpdatePost(post)
    }

    suspend fun likePost(post: Post, userId: String): Result<String> {
        val updatedIdsOfUsersLiked = post.idsOfUsersLiked.toMutableList()
        updatedIdsOfUsersLiked.add(userId)
        val updatedPost = post.copy(idsOfUsersLiked = updatedIdsOfUsersLiked)
        return postsRemoteDataSource.addOrUpdatePost(updatedPost)
    }

    suspend fun unlikePost(post: Post, userId: String): Result<String> {
        val updatedIdsOfUsersLiked = post.idsOfUsersLiked.toMutableList()
        updatedIdsOfUsersLiked.remove(userId)
        val updatedPost = post.copy(idsOfUsersLiked = updatedIdsOfUsersLiked)
        return postsRemoteDataSource.addOrUpdatePost(updatedPost)
    }

    suspend fun deletePost(postId: String): Result<String> {
        return postsRemoteDataSource.deletePost(postId)
    }

    companion object {
        val TAG = PostsRepository::class.java.simpleName
    }
}