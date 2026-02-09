package org.sammomanyi.mediaccess.di


import androidx.room.RoomDatabase
import org.koin.compose.viewmodel.dsl.viewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.app
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.initialize
import org.koin.core.module.Module // Import the actual Koin Module type
import org.koin.dsl.module
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.bind
import org.sammomanyi.mediaccess.BuildKonfig
import org.sammomanyi.mediaccess.core.data.database.MediAccessDatabase
import org.sammomanyi.mediaccess.core.data.database.MediAccessDatabaseConstructor
import org.sammomanyi.mediaccess.core.data.database.getRoomDatabase
import org.sammomanyi.mediaccess.features.identity.data.local.UserDao
import org.sammomanyi.mediaccess.features.identity.data.repository.IdentityRepositoryImpl
import org.sammomanyi.mediaccess.features.identity.domain.repository.IdentityRepository
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GetProfileUseCase
import org.sammomanyi.mediaccess.features.identity.domain.use_case.LoginUserUseCase
import org.sammomanyi.mediaccess.features.identity.domain.use_case.RegisterUserUseCase
import org.sammomanyi.mediaccess.features.identity.presentation.login.LoginViewModel
import org.sammomanyi.mediaccess.features.identity.presentation.profile.ProfileViewModel
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
    singleOf(::LoginUserUseCase)
    singleOf(::GetProfileUseCase)

    // 6. ViewModels using viewModelOf
    // This is much cleaner and avoids multiple get() calls
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegistrationViewModel)
    viewModelOf(::ProfileViewModel)
}


