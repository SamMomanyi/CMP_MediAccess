package org.sammomanyi.mediaccess.core.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import org.sammomanyi.mediaccess.features.cover.data.local.CoverLinkRequestDao
import org.sammomanyi.mediaccess.features.identity.data.local.*

@Database(
    entities = [
        UserEntity::class,
        MedicalRecordEntity::class,
        HospitalEntity::class,
        AppointmentEntity::class
    ],
    version = 3, // Keep version 2 to handle the new tables
    exportSchema = true
)
@ConstructedBy(MediAccessDatabaseConstructor::class)
abstract class MediAccessDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val medicalRecordDao: MedicalRecordDao
    abstract val hospitalDao: HospitalDao
    abstract val appointmentDao: AppointmentDao

    abstract val coverLinkRequestDao: CoverLinkRequestDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object MediAccessDatabaseConstructor : RoomDatabaseConstructor<MediAccessDatabase>

fun getRoomDatabase(
    builder: RoomDatabase.Builder<MediAccessDatabase>
): MediAccessDatabase {
    return builder
        .setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver())
        .setQueryCoroutineContext(kotlinx.coroutines.Dispatchers.IO)
        .fallbackToDestructiveMigration(dropAllTables = true) // Important for dev
        .build()
}