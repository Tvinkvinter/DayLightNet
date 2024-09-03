package com.atarusov

import android.app.Application
import com.atarusov.daylightnet.data.AuthManager
import com.atarusov.daylightnet.data.PostsRemoteDataSource
import com.atarusov.daylightnet.data.PostsRepository
import com.atarusov.daylightnet.data.UsersRemoteDataSource
import com.atarusov.daylightnet.data.UsersRepository
import com.google.firebase.Firebase

class App : Application() {

    lateinit var postsRepository: PostsRepository
    lateinit var usersRepository: UsersRepository
    lateinit var authManager: AuthManager

    override fun onCreate() {
        super.onCreate()

        authManager = AuthManager
        postsRepository = PostsRepository(PostsRemoteDataSource())
        usersRepository = UsersRepository(UsersRemoteDataSource(), authManager)
    }
}