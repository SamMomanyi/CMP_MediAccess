package org.sammomanyi.mediaccess.core.data.database

import androidx.room.RoomDatabaseConstructor

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object MediAccessDatabaseConstructor : RoomDatabaseConstructor<MediAccessDatabase> {
    override fun initialize(): MediAccessDatabase
}