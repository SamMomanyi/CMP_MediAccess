package org.sammomanyi.mediaccess.di

import androidx.room.Room
import androidx.room.RoomDatabase
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.app
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.initialize
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.bind
import org.koin.dsl.module
import org.sammomanyi.mediaccess.core.data.database.MediAccessDatabase
import org.sammomanyi.mediaccess.features.identity.data.repository.IdentityRepositoryImpl
import org.sammomanyi.mediaccess.features.identity.domain.repository.IdentityRepository

actual val platformModule = module {
    single<RoomDatabase.Builder<MediAccessDatabase>> {
        val context = androidContext()
        val dbFile = context.getDatabasePath("mediaccess.db")
        Room.databaseBuilder<MediAccessDatabase>(
            context = context,
            name = dbFile.absolutePath
        )
    }

    single<FirebaseApp> {
        val context = androidContext()
        try {
            // 1. Try to get the already initialized default app
            Firebase.app
        } catch (e: Exception) {
            // 2. If it's not initialized (e.g., during tests or first run), initialize it
            Firebase.initialize(context, firebaseOptions)
        }
    } withOptions {
        createdAtStart()
    }

    // Firebase Services
    single { Firebase.auth }
    single { Firebase.firestore }

    // Real repository with Firebase
    singleOf(::IdentityRepositoryImpl).bind<IdentityRepository>()
}