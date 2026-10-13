package com.miojo.cerebelo

import android.app.Application
import android.content.Context

class CerebeloApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

val Context.appContainer: AppContainer
    get() = (applicationContext as CerebeloApp).container
