package com.atarusov.daylightnet.di

import com.atarusov.daylightnet.MainActivity
import com.atarusov.daylightnet.data.AuthManager
import com.atarusov.daylightnet.data.AuthManagerImpl
import com.atarusov.daylightnet.data.PostCardsRepository
import com.atarusov.daylightnet.data.PostCardsRepositoryImpl
import com.atarusov.daylightnet.data.PostsRemoteDataSource
import com.atarusov.daylightnet.data.PostsRemoteDataSourceImpl
import com.atarusov.daylightnet.data.PostsRepository
import com.atarusov.daylightnet.data.PostsRepositoryImpl
import com.atarusov.daylightnet.data.UserSessionManager
import com.atarusov.daylightnet.data.UserSessionManagerImpl
import com.atarusov.daylightnet.data.UsersRemoteDataSource
import com.atarusov.daylightnet.data.UsersRemoteDataSourceImpl
import com.atarusov.daylightnet.data.UsersRepository
import com.atarusov.daylightnet.data.UsersRepositoryImpl
import com.atarusov.daylightnet.fragments.HomeFragment
import com.atarusov.daylightnet.fragments.LoginFragment
import com.atarusov.daylightnet.fragments.ProfileFragment
import com.atarusov.daylightnet.fragments.RegisterFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Singleton
@Component(modules = [AppModule::class, FirebaseModule::class])
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(): AppComponent
    }

    fun inject(activity: MainActivity)
    fun inject(fragment: LoginFragment)
    fun inject(fragment: RegisterFragment)
    fun inject(fragment: HomeFragment)
    fun inject(fragment: ProfileFragment)
}

@Module(includes = [AppBindModule::class])
class AppModule

@Module
class FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore
}

@Module
interface AppBindModule {
    @Binds
    fun bindAuthManager(authManager: AuthManagerImpl): AuthManager

    @Binds
    fun bindUserSessionManager(userSessionManager: UserSessionManagerImpl): UserSessionManager

    @Binds
    fun bindPostCardsRepository(postCardsRepository: PostCardsRepositoryImpl): PostCardsRepository

    @Binds
    fun bindPostsRemoteDataSource(postsRemoteDataSource: PostsRemoteDataSourceImpl): PostsRemoteDataSource

    @Binds
    fun bindPostsRepository(postsRepository: PostsRepositoryImpl): PostsRepository

    @Binds
    fun bindUserRemoteDataSource(usersRemoteDataSource: UsersRemoteDataSourceImpl): UsersRemoteDataSource

    @Binds
    fun bindUsersRepository(usersRepository: UsersRepositoryImpl): UsersRepository
}
