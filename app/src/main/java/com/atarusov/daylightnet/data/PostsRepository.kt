package com.atarusov.daylightnet.data

import com.atarusov.daylightnet.model.Post
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface PostsRepository {
    val posts: StateFlow<List<Post>>
    suspend fun fetchAllPosts(): Result<String>
    suspend fun addPost(post: Post): Result<String>
    suspend fun likePost(post: Post, userId: String): Result<String>
    suspend fun unlikePost(post: Post, userId: String): Result<String>
    suspend fun deletePost(postId: String): Result<String>
}

@Singleton
class PostsRepositoryImpl @Inject constructor(
    private val postsRemoteDataSource: PostsRemoteDataSource
): PostsRepository {
    override val posts: StateFlow<List<Post>> = postsRemoteDataSource.posts

    override suspend fun fetchAllPosts(): Result<String> {
        return postsRemoteDataSource.fetchAllPosts()
    }

    override suspend fun addPost(post: Post): Result<String> {
        return postsRemoteDataSource.addOrUpdatePost(post)
    }

    override suspend fun likePost(post: Post, userId: String): Result<String> {
        val updatedIdsOfUsersLiked = post.idsOfUsersLiked.toMutableList()
        updatedIdsOfUsersLiked.add(userId)
        val updatedPost = post.copy(idsOfUsersLiked = updatedIdsOfUsersLiked)
        return postsRemoteDataSource.addOrUpdatePost(updatedPost)
    }

    override suspend fun unlikePost(post: Post, userId: String): Result<String> {
        val updatedIdsOfUsersLiked = post.idsOfUsersLiked.toMutableList()
        updatedIdsOfUsersLiked.remove(userId)
        val updatedPost = post.copy(idsOfUsersLiked = updatedIdsOfUsersLiked)
        return postsRemoteDataSource.addOrUpdatePost(updatedPost)
    }

    override suspend fun deletePost(postId: String): Result<String> {
        return postsRemoteDataSource.deletePost(postId)
    }

    companion object {
        val TAG = PostsRepository::class.java.simpleName
    }
}