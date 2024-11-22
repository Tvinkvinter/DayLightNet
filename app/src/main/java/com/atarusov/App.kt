package com.atarusov

import android.app.Application
import android.content.Context
import com.atarusov.daylightnet.di.AppComponent
import com.atarusov.daylightnet.di.DaggerAppComponent

class App : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.create()
    }
}

val Context.appComponent: AppComponent
    get() = when (this) {
        is App -> appComponent
        else -> this.applicationContext.appComponent
    }