package com.atarusov.daylightnet.di

import com.atarusov.daylightnet.MainActivity
import com.atarusov.daylightnet.fragments.HomeFragment
import com.atarusov.daylightnet.fragments.LoginFragment
import com.atarusov.daylightnet.fragments.ProfileFragment
import com.atarusov.daylightnet.fragments.RegisterFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Singleton
@Component(modules = [FirebaseModule::class])
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

@Module
class FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore
}
