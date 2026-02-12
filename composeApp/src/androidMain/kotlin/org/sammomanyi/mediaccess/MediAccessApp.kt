package org.sammomanyi.mediaccess

import android.app.Application
import org.koin.android.ext.koin.androidContext
//import org.koin.android.ext.koin.koinContext
import org.sammomanyi.mediaccess.core.data.preferences.initDataStore
import org.koin.core.context.startKoin
import org.sammomanyi.mediaccess.di.platformModule
import org.sammomanyi.mediaccess.di.sharedModule

class MediAccessApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize DataStore with context BEFORE Koin
        initDataStore(this)

        startKoin {
            // Android-specific Koin context
            androidContext(this@MediAccessApp)
            modules(sharedModule, platformModule)
        }
    }
}