package org.sammomanyi.MediAccess.core.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.sammomanyi.mediaccess.core.data.database.DatabaseFactory
import org.sammomanyi.mediaccess.core.data.database.MediAccessDatabase

class AndroidDatabaseFactory(private val context: Context) : DatabaseFactory {
    override fun create(): RoomDatabase.Builder<MediAccessDatabase> {
        val appContext = context.applicationContext
        val dbFile = appContext.getDatabasePath("mediaccess.db")
        return Room.databaseBuilder<MediAccessDatabase>(
            context = appContext,
            name = dbFile.absolutePath
        )
    }
}