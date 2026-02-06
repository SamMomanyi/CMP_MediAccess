package org.sammomanyi.mediaccess.di


import org.koin.core.module.Module // Import the actual Koin Module type
import org.koin.dsl.module
import org.sammomanyi.mediaccess.core.data.database.MediAccessDatabase
import org.sammomanyi.mediaccess.core.data.database.getRoomDatabase

val sharedModule = module {
    single {
        getRoomDatabase(get())
    }
    // Make sure you provide the class type for the Dao getter
    single { get<MediAccessDatabase>().userDao }
}

// Corrected expect declaration
expect val platformModule: Module