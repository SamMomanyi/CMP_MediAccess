package org.sammomanyi.mediaccess.di

import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    // Android specific injections (like the DatabaseFactory)
}