package org.sammomanyi.mediaccess.di

import GoogleSignInProvider
import android.content.Context
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
import org.sammomanyi.mediaccess.features.identity.data.auth.GoogleSignInHelper
import org.sammomanyi.mediaccess.features.identity.data.repository.AppointmentRepositoryImpl
import org.sammomanyi.mediaccess.features.identity.data.repository.HospitalRepositoryImpl
import org.sammomanyi.mediaccess.features.identity.data.repository.IdentityRepositoryImpl
import org.sammomanyi.mediaccess.features.identity.data.repository.RecordsRepositoryImpl
import org.sammomanyi.mediaccess.features.identity.domain.repository.AppointmentRepository
import org.sammomanyi.mediaccess.features.identity.domain.repository.HospitalRepository
import org.sammomanyi.mediaccess.features.identity.domain.repository.IdentityRepository
import org.sammomanyi.mediaccess.features.identity.domain.repository.RecordsRepository

actual val platformModule = module {
    single<RoomDatabase.Builder<MediAccessDatabase>> {
        val context = androidContext()
        val dbFile = context.getDatabasePath("mediaccess.db")

        if (!dbFile.parentFile.exists()) {
            dbFile.parentFile.mkdirs()
        }

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
    // Google Sign-In Helper
    single {
        GoogleSignInHelper(
            context = get<Context>(),
            firebaseAuth = get()
        )
    }
    single<GoogleSignInProvider> { GoogleSignInHelper(get(), get()) }

    // Real repository with Firebase
    singleOf(::IdentityRepositoryImpl).bind<IdentityRepository>()
    singleOf(::RecordsRepositoryImpl).bind<RecordsRepository>()
    singleOf(::HospitalRepositoryImpl).bind<HospitalRepository>()
    singleOf(::AppointmentRepositoryImpl).bind<AppointmentRepository>()
}