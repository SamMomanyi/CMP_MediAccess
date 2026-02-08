package org.sammomanyi.mediaccess.di

import androidx.room.Room
import androidx.room.RoomDatabase
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.initialize
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.bind
import org.koin.dsl.module
import org.sammomanyi.mediaccess.DesktopIdentityRepositoryImpl
import org.sammomanyi.mediaccess.core.data.database.MediAccessDatabase
import org.sammomanyi.mediaccess.features.identity.domain.repository.IdentityRepository
import java.io.File

actual val platformModule = module {
    single<RoomDatabase.Builder<MediAccessDatabase>> {
        val dbFile = File(System.getProperty("java.io.tmpdir"), "mediaccess.db")
        Room.databaseBuilder<MediAccessDatabase>(
            name = dbFile.absolutePath
        )
    }

    // Stub Firebase Auth - throws error when used
    single<FirebaseAuth> {
        throw UnsupportedOperationException(
            "Firebase Auth is not available on Desktop. " +
                    "Please build and run the Android version for authentication features."
        )
    }

    // Stub Firebase Firestore - throws error when used
    single<FirebaseFirestore> {
        throw UnsupportedOperationException(
            "Firebase Firestore is not available on Desktop. " +
                    "Please build and run the Android version for cloud database features."
        )
    }

    // Desktop-specific repository (no Firebase)
    singleOf(::DesktopIdentityRepositoryImpl).bind<IdentityRepository>()
}