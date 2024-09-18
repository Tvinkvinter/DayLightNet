package com.atarusov

import android.app.Application
import com.atarusov.daylightnet.data.AuthManager
import com.atarusov.daylightnet.data.PostCardsRepository
import com.atarusov.daylightnet.data.PostsRemoteDataSource
import com.atarusov.daylightnet.data.PostsRepository
import com.atarusov.daylightnet.data.UsersRemoteDataSource
import com.atarusov.daylightnet.data.UsersRepository

class App : Application() {

    lateinit var authManager: AuthManager
    lateinit var usersRepository: UsersRepository
    lateinit var postsRepository: PostsRepository
    lateinit var postCardsRepository: PostCardsRepository

    override fun onCreate() {
        super.onCreate()

        authManager = AuthManager
        usersRepository = UsersRepository(UsersRemoteDataSource(), authManager)
        postsRepository = PostsRepository(PostsRemoteDataSource())
        postCardsRepository = PostCardsRepository(postsRepository, usersRepository)
    }
}