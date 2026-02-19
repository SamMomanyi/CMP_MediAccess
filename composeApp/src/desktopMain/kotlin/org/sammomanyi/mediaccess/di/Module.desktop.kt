package org.sammomanyi.mediaccess.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
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
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.bind
import org.koin.dsl.module
import org.sammomanyi.mediaccess.BootstrapAdminCallback
import org.sammomanyi.mediaccess.DesktopIdentityRepositoryImpl
import org.sammomanyi.mediaccess.MediAccessAdminDatabase
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
import org.sammomanyi.mediaccess.features.pharmacy.data.PharmacyQueueRepository
import org.sammomanyi.mediaccess.features.pharmacy.data.PrescriptionRepository
import org.sammomanyi.mediaccess.features.pharmacy.data.desktop.PharmacyDesktopRepository
import org.sammomanyi.mediaccess.features.queue.data.desktop.QueueDesktopRepository
import org.sammomanyi.mediaccess.features.queue.data.desktop.StaffFirestoreRepository
import org.sammomanyi.mediaccess.features.queue.presentation.DoctorQueueViewModel
import org.sammomanyi.mediaccess.features.queue.presentation.StaffManagementViewModel
import org.sammomanyi.mediaccess.features.verification.data.desktop.VisitVerificationRestClient
import org.sammomanyi.mediaccess.features.verification.presentation.desktop.VisitVerificationViewModel
import java.io.File

actual val platformModule = module {

    // ── Desktop Room Database ─────────────────────────────────
    single {
        val dbFile = File(System.getProperty("java.io.tmpdir"), "mediaccess_admin.db")

        // ✅ NO DELETE - database persists between runs
        Room.databaseBuilder<MediAccessAdminDatabase>(
            name = dbFile.absolutePath
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .fallbackToDestructiveMigration(dropAllTables = true)  // Only deletes if migration fails
            .addCallback(BootstrapAdminCallback())  // Only runs on CREATE, not every launch
            .build()
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
    // ✅ Get DAOs from MediAccessAdminDatabase
    single { get<MediAccessAdminDatabase>().adminAccountDao }
    single { get<MediAccessAdminDatabase>().coverLinkRequestDao }


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
    single { PharmacyDesktopRepository(firestoreClient = get()) }
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