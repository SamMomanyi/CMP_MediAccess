package org.sammomanyi.mediaccess.di


import GenerateVisitCodeUseCase
import VerifyVisitCodeUseCase
import androidx.room.RoomDatabase
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import org.koin.core.module.Module // Import the actual Koin Module type
import org.koin.dsl.module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.sammomanyi.mediaccess.BuildKonfig
import org.sammomanyi.mediaccess.core.data.database.MediAccessDatabase
import org.sammomanyi.mediaccess.core.data.database.getRoomDatabase
import org.sammomanyi.mediaccess.features.identity.data.repository.AppointmentRepositoryImpl
import org.sammomanyi.mediaccess.features.identity.data.repository.HospitalRepositoryImpl
import org.sammomanyi.mediaccess.features.identity.data.repository.IdentityRepositoryImpl
import org.sammomanyi.mediaccess.features.identity.data.repository.RecordsRepositoryImpl
import org.sammomanyi.mediaccess.features.identity.domain.repository.AppointmentRepository
import org.sammomanyi.mediaccess.features.identity.domain.repository.HospitalRepository
import org.sammomanyi.mediaccess.features.identity.domain.repository.IdentityRepository
import org.sammomanyi.mediaccess.features.identity.domain.repository.RecordsRepository
import org.sammomanyi.mediaccess.features.identity.domain.use_case.BookAppointmentUseCase
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
import org.sammomanyi.mediaccess.features.identity.presentation.home.HomeViewModel
import org.sammomanyi.mediaccess.features.identity.presentation.hospitals.HospitalsViewModel
import org.sammomanyi.mediaccess.features.identity.presentation.login.LoginViewModel
import org.sammomanyi.mediaccess.features.identity.presentation.profile.ProfileViewModel
import org.sammomanyi.mediaccess.features.identity.presentation.records.RecordsViewModel
import org.sammomanyi.mediaccess.features.identity.presentation.registration.RegistrationViewModel

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
    // 1. Database & Dao
    // We use get() here because Room initialization involves complex builders
    single {
        getRoomDatabase(get<RoomDatabase.Builder<MediAccessDatabase>>())
    }

    single { get<MediAccessDatabase>().userDao }
    single { get<MediAccessDatabase>().medicalRecordDao }
    single { get<MediAccessDatabase>().hospitalDao }
    single { get<MediAccessDatabase>().appointmentDao }


    // 2. Repositories (Add the implementation here if not in platformModule)
    singleOf(::HospitalRepositoryImpl).bind<HospitalRepository>()
    singleOf(::RecordsRepositoryImpl).bind<RecordsRepository>()
    singleOf(::AppointmentRepositoryImpl).bind<AppointmentRepository>()
    singleOf(::IdentityRepositoryImpl).bind<IdentityRepository>()

    // 3. Firebase Services
    single { Firebase.auth }
    single { Firebase.firestore }

    // 4. Using singleOf and bind for the Repository
    // This matches: singleOf(::KtorRemoteBookDataSource).bind<RemoteBookDataSource>()
    // Repository moved to platformModule
    // singleOf(::IdentityRepositoryImpl).bind<IdentityRepository>() // REMOVE THIS LINE

    // 5. Use Cases
    singleOf(::RegisterUserUseCase)
    singleOf(::LoginUserUseCase)
    singleOf(::GetProfileUseCase)
    singleOf(::GenerateVisitCodeUseCase)
    singleOf(::GetRecordsUseCase)
    singleOf(::LogoutUseCase)
    singleOf(::SyncRecordsUseCase)
    singleOf(::GetHospitalsUseCase) // ADD THIS LINE
    singleOf(::GetCurrentUserUseCase)
    singleOf(::SyncHospitalsUseCase)
    singleOf(::VerifyVisitCodeUseCase)
    singleOf(::BookAppointmentUseCase)
    // Use Cases
    singleOf(::RegisterUserUseCase)
    singleOf(::LoginUserUseCase)
    singleOf(::GoogleSignInUseCase)  // ADD THIS
    singleOf(::GetProfileUseCase)
    // 6. ViewModels using viewModelOf
    // This is much cleaner and avoids multiple get() calls
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegistrationViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::RecordsViewModel)
    viewModelOf(::HospitalsViewModel)
    viewModelOf(::ProfileViewModel)

}


