package org.sammomanyi.mediaccess.core.data.database



import androidx.room.RoomDatabase

interface DatabaseFactory {
    fun create(): RoomDatabase.Builder<MediAccessDatabase>
}