package org.sammomanyi.mediaccess.di



import VerifyVisitCodeUseCase
import androidx.room.RoomDatabase
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType.Application.Json
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module // Import the actual Koin Module type
import org.koin.dsl.module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.sammomanyi.mediaccess.BuildKonfig
import org.sammomanyi.mediaccess.core.data.database.MediAccessDatabase
import org.sammomanyi.mediaccess.core.data.database.getRoomDatabase
import org.sammomanyi.mediaccess.core.data.preferences.ThemeRepository
import org.sammomanyi.mediaccess.core.data.preferences.createDataStore
import org.sammomanyi.mediaccess.core.presentation.theme.ThemeViewModel
import org.sammomanyi.mediaccess.features.cover.data.CoverRepository
import org.sammomanyi.mediaccess.features.cover.presentation.CoverViewModel
import org.sammomanyi.mediaccess.features.identity.data.remote.NewsService
import org.sammomanyi.mediaccess.features.identity.data.repository.AppointmentRepositoryImpl
import org.sammomanyi.mediaccess.features.identity.data.repository.HospitalRepositoryImpl
import org.sammomanyi.mediaccess.features.identity.data.repository.IdentityRepositoryImpl
import org.sammomanyi.mediaccess.features.identity.data.repository.RecordsRepositoryImpl
import org.sammomanyi.mediaccess.features.identity.domain.repository.AppointmentRepository
import org.sammomanyi.mediaccess.features.identity.domain.repository.HospitalRepository
import org.sammomanyi.mediaccess.features.identity.domain.repository.IdentityRepository
import org.sammomanyi.mediaccess.features.identity.domain.repository.RecordsRepository
import org.sammomanyi.mediaccess.features.identity.domain.use_case.BookAppointmentUseCase
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GenerateVisitCodeUseCase
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GetCurrentUserUseCase
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GetHospitalsUseCase
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GetProfileUseCase
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GetRecordsUseCase
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GoogleSignInUseCase
import org.sammomanyi.mediaccess.features.identity.domain.use_case.LoginUserUseCase
import org.sammomanyi.mediaccess.features.identity.domain.use_case.LogoutUseCase
import org.sammomanyi.mediaccess.features.identity.domain.use_case.RegisterUserUseCase
import org.sammomanyi.mediaccess.features.identity.domain.use_case.SyncHospitalsUseCase
import org.sammomanyi.mediaccess.features.identity.domain.use_case.SyncRecordsUseCase
import org.sammomanyi.mediaccess.features.identity.presentation.care.CareViewModel
import org.sammomanyi.mediaccess.features.identity.presentation.checkin.CheckInViewModel
import org.sammomanyi.mediaccess.features.identity.presentation.home.HomeViewModel
import org.sammomanyi.mediaccess.features.identity.presentation.hospitals.HospitalsViewModel
import org.sammomanyi.mediaccess.features.identity.presentation.link_cover.LinkCoverViewModel
import org.sammomanyi.mediaccess.features.identity.presentation.login.LoginViewModel
import org.sammomanyi.mediaccess.features.identity.presentation.personal.PersonalViewModel
import org.sammomanyi.mediaccess.features.identity.presentation.profile.ProfileViewModel
import org.sammomanyi.mediaccess.features.identity.presentation.records.RecordsViewModel
import org.sammomanyi.mediaccess.features.identity.presentation.registration.RegistrationViewModel
import org.sammomanyi.mediaccess.features.identity.presentation.verification.VerificationViewModel
import org.sammomanyi.mediaccess.features.pharmacy.data.PharmacyQueueRepository
import org.sammomanyi.mediaccess.features.pharmacy.data.PrescriptionRepository
import org.sammomanyi.mediaccess.features.pharmacy.presentation.ExpenditureSummaryViewModel
import org.sammomanyi.mediaccess.features.queue.data.QueueRepository
import org.sammomanyi.mediaccess.features.wellness.data.WellnessRepository
import org.sammomanyi.mediaccess.features.wellness.presentation.WellnessViewModel

// ADD THIS AT THE BOTTOM OF YOUR FILE
expect val platformModule: Module

// 2. Firebase initialization

val firebaseOptions = FirebaseOptions(
    authDomain = "mediaccess-52e27.firebaseapp.com",
    applicationId = "1:588195843985:web:232ce4cd788a388b2b5430",
    storageBucket = "mediaccess-52e27.firebasestorage.app",
    apiKey = BuildKonfig.FIREBASE_API_KEY,
    projectId = BuildKonfig.FIREBASE_PROJECT_ID,
)

val sharedModule = module {
    // 1. Core Services (Networking & DB)
    single { getRoomDatabase(get<RoomDatabase.Builder<MediAccessDatabase>>()) }

    // DataStore & Theme
    single { createDataStore() }
    single { ThemeRepository(get()) }
    viewModelOf(::ThemeViewModel)

    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true // Useful for handling nulls from the News API
                })
            }
            install(HttpTimeout){
                requestTimeoutMillis = 15000L
                connectTimeoutMillis = 10000L
            }
        }
    }

    single { PrescriptionRepository(get()) }
    single { PharmacyQueueRepository(get()) }

    single { NewsService(get()) }
    // Register CoverRepository directly — no Impl needed
    single { CoverRepository(get(), get()) }
    single { QueueRepository(get()) }
    // 2. DAOs
    single { get<MediAccessDatabase>().userDao }
    single { get<MediAccessDatabase>().medicalRecordDao }
    single { get<MediAccessDatabase>().hospitalDao }
    single { get<MediAccessDatabase>().appointmentDao }
    single { get<MediAccessDatabase>().coverLinkRequestDao }

    // 3. Repositories
    singleOf(::HospitalRepositoryImpl).bind<HospitalRepository>()
    singleOf(::RecordsRepositoryImpl).bind<RecordsRepository>()
    singleOf(::AppointmentRepositoryImpl).bind<AppointmentRepository>()
    singleOf(::IdentityRepositoryImpl).bind<IdentityRepository>()

    // 4. Firebase Services
    single { Firebase.auth }
    single { Firebase.firestore }
    single { WellnessRepository(get()) }
    // 5. Use Cases (Cleaned duplicates)
    singleOf(::RegisterUserUseCase)
    singleOf(::LoginUserUseCase)
    singleOf(::GoogleSignInUseCase)
    singleOf(::GetProfileUseCase)
    singleOf(::GenerateVisitCodeUseCase)
    singleOf(::GetRecordsUseCase)
    singleOf(::LogoutUseCase)
    singleOf(::SyncRecordsUseCase)
    singleOf(::GetHospitalsUseCase)
    singleOf(::GetCurrentUserUseCase)
    singleOf(::SyncHospitalsUseCase)
    singleOf(::VerifyVisitCodeUseCase)
    singleOf(::BookAppointmentUseCase)

    // 6. ViewModels
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegistrationViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::RecordsViewModel)
    viewModelOf(::HospitalsViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::CareViewModel)
    viewModelOf(::PersonalViewModel)
    viewModelOf(::WellnessViewModel)
    // In Module.kt sharedModule, add alongside other viewModels:
    viewModelOf(::LinkCoverViewModel)
    // ✅ FIX: Add CoverViewModel here
    viewModelOf(::CoverViewModel)
    // In Module.kt sharedModule
    viewModelOf(::CheckInViewModel)
    viewModelOf(::VerificationViewModel)

    viewModelOf(::ExpenditureSummaryViewModel)

}