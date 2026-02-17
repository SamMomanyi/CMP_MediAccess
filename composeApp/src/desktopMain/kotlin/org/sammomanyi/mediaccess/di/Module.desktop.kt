package org.sammomanyi.mediaccess.di

import androidx.room.Room
import androidx.room.RoomDatabase
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.initialize
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType.Application.Json
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
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
import org.sammomanyi.mediaccess.features.auth.presentation.AdminLoginViewModel
import org.sammomanyi.mediaccess.features.cover.data.CoverRepository
import org.sammomanyi.mediaccess.features.cover.data.desktop.DesktopCoverRepository
import org.sammomanyi.mediaccess.features.cover.data.desktop.FirestoreRestClient
import org.sammomanyi.mediaccess.features.cover.data.desktop.ServiceAccountCredentials
import org.sammomanyi.mediaccess.features.cover.presentation.AdminCoverViewModel
import org.sammomanyi.mediaccess.features.identity.data.repository.DesktopAppointmentRepositoryImpl
import org.sammomanyi.mediaccess.features.identity.data.repository.DesktopHospitalRepositoryImpl
import org.sammomanyi.mediaccess.features.identity.data.repository.DesktopRecordsRepositoryImpl
import org.sammomanyi.mediaccess.features.identity.domain.repository.AppointmentRepository
import org.sammomanyi.mediaccess.features.identity.domain.repository.HospitalRepository
import org.sammomanyi.mediaccess.features.identity.domain.repository.IdentityRepository
import org.sammomanyi.mediaccess.features.identity.domain.repository.RecordsRepository
import org.sammomanyi.mediaccess.features.queue.data.desktop.QueueDesktopRepository
import org.sammomanyi.mediaccess.features.queue.data.desktop.StaffFirestoreRepository
import org.sammomanyi.mediaccess.features.queue.presentation.StaffManagementViewModel
import org.sammomanyi.mediaccess.features.verification.data.desktop.VisitVerificationRestClient
import org.sammomanyi.mediaccess.features.verification.presentation.desktop.VisitVerificationViewModel
import java.io.File

actual val platformModule = module {

    single<RoomDatabase.Builder<MediAccessDatabase>> {
        val dbFile = File(System.getProperty("java.io.tmpdir"), "mediaccess.db")
        Room.databaseBuilder<MediAccessDatabase>(
            name = dbFile.absolutePath
        )
    }


    // ── Ktor HTTP client (OkHttp engine for desktop JVM) ─────
    single<HttpClient> {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }
    single { get<MediAccessDatabase>().adminDao }
    single { get<MediAccessDatabase>().medicalRecordDao }
    single { get<MediAccessDatabase>().hospitalDao }
    single { get<MediAccessDatabase>().appointmentDao }
    single { get<MediAccessDatabase>().coverLinkRequestDao }


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

    // ── Cover request management ──────────────────────────────
    single { DesktopCoverRepository(dao = get(), firestoreClient = get()) }
    viewModelOf(::AdminCoverViewModel)

    // ── Visit Verification ────────────────────────────────────
    single { VisitVerificationRestClient(firestoreClient = get()) }
    viewModelOf(::VisitVerificationViewModel)

    // ── Firestore REST (pure Ktor + Java crypto, no Firebase SDK) ──
    single { ServiceAccountCredentials(ServiceAccountCredentials.resolve()) }
    single { FirestoreRestClient(credentials = get(), httpClient = get()) }
    single { DesktopCoverRepository(dao = get(), firestoreClient = get()) }

    // ── Queue + Staff ─────────────────────────────────────────
    single {
        StaffFirestoreRepository(firestoreClient = get())
    }

    single {
        QueueDesktopRepository(firestoreClient = get())
    }

    // ── Visit Verification REST client ────────────────────────
    single {
        VisitVerificationRestClient(firestoreClient = get())
    }

    // Desktop-specific repository (no Firebase)
    singleOf(::DesktopIdentityRepositoryImpl).bind<IdentityRepository>()
    singleOf(::DesktopRecordsRepositoryImpl).bind<RecordsRepository>()
    singleOf(::DesktopHospitalRepositoryImpl).bind<HospitalRepository>()
    singleOf(::DesktopAppointmentRepositoryImpl).bind<AppointmentRepository>()

    singleOf(::AdminRepository)
    viewModelOf(::AdminCoverViewModel)
    viewModelOf(::AdminAuthViewModel)
    viewModelOf(::AdminLoginViewModel)
    viewModelOf(::AdminCoverViewModel)
    viewModelOf(::StaffManagementViewModel)
    viewModelOf(::VisitVerificationViewModel)

}