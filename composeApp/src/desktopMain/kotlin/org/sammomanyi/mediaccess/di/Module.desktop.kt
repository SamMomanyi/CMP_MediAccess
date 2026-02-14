package org.sammomanyi.mediaccess.di

import androidx.room.Room
import androidx.room.RoomDatabase
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.initialize
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.bind
import org.koin.dsl.module
import org.sammomanyi.mediaccess.DesktopIdentityRepositoryImpl
import org.sammomanyi.mediaccess.core.data.database.MediAccessDatabase
import org.sammomanyi.mediaccess.features.admin.data.AdminRepository
import org.sammomanyi.mediaccess.features.admin.presentation.AdminAuthViewModel
import org.sammomanyi.mediaccess.features.cover.data.CoverRepository
import org.sammomanyi.mediaccess.features.cover.presentation.AdminCoverViewModel
import org.sammomanyi.mediaccess.features.identity.data.repository.DesktopAppointmentRepositoryImpl
import org.sammomanyi.mediaccess.features.identity.data.repository.DesktopHospitalRepositoryImpl
import org.sammomanyi.mediaccess.features.identity.data.repository.DesktopRecordsRepositoryImpl
import org.sammomanyi.mediaccess.features.identity.domain.repository.AppointmentRepository
import org.sammomanyi.mediaccess.features.identity.domain.repository.HospitalRepository
import org.sammomanyi.mediaccess.features.identity.domain.repository.IdentityRepository
import org.sammomanyi.mediaccess.features.identity.domain.repository.RecordsRepository
import java.io.File

actual val platformModule = module {

    single<RoomDatabase.Builder<MediAccessDatabase>> {
        val dbFile = File(System.getProperty("java.io.tmpdir"), "mediaccess.db")
        Room.databaseBuilder<MediAccessDatabase>(
            name = dbFile.absolutePath
        )
    }


    single { get<MediAccessDatabase>().adminDao }
    single { get<MediAccessDatabase>().medicalRecordDao }
    single { get<MediAccessDatabase>().hospitalDao }
    single { get<MediAccessDatabase>().appointmentDao }


    // Stub Firebase Auth - throws error when used
    single<FirebaseAuth> {
        throw UnsupportedOperationException(
            "Firebase Auth is not available on Desktop. " +
                    "Please build and run the Android version for authentication features."
        )
    }

    // ── ADD: cover DAO + repository ──
    single { get<MediAccessDatabase>().coverLinkRequestDao }

    // Stub Firebase Firestore - throws error when used
    single { Firebase.firestore }

    // CoverRepository on desktop: no Firestore, pass null or a no-op
    single {
        CoverRepository(
            dao = get(),
            firestore = get()  // no !! needed — it's now nullable
        )
    }

    // Desktop-specific repository (no Firebase)
    singleOf(::DesktopIdentityRepositoryImpl).bind<IdentityRepository>()
    singleOf(::DesktopRecordsRepositoryImpl).bind<RecordsRepository>()
    singleOf(::DesktopHospitalRepositoryImpl).bind<HospitalRepository>()
    singleOf(::DesktopAppointmentRepositoryImpl).bind<AppointmentRepository>()

    singleOf(::AdminRepository)
    viewModelOf(::AdminCoverViewModel)
    viewModelOf(::AdminAuthViewModel)

    
}