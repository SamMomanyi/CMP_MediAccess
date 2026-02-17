package org.sammomanyi.mediaccess



import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration

import org.sammomanyi.mediaccess.features.auth.data.local.AdminAccountDao
import org.sammomanyi.mediaccess.features.auth.data.local.AdminAccountEntity
import org.sammomanyi.mediaccess.features.cover.data.local.CoverLinkRequestDao
import org.sammomanyi.mediaccess.features.cover.data.local.CoverLinkRequestEntity

@Database(
    entities = [AdminAccountEntity::class, CoverLinkRequestEntity::class],
    version = 3,                            // bumped from 2
    exportSchema = false
)
abstract class MediAccessAdminDatabase : RoomDatabase() {
    abstract val adminAccountDao: AdminAccountDao
    abstract val coverLinkRequestDao: CoverLinkRequestDao
}