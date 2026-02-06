package org.sammomanyi.MediAccess.core.data.database

import androidx.room.Room
import androidx.room.RoomDatabase
import org.sammomanyi.mediaccess.core.data.database.DatabaseFactory

import org.sammomanyi.mediaccess.core.data.database.MediAccessDatabase
import java.io.File

class DesktopDatabaseFactory : DatabaseFactory {
    override fun create(): RoomDatabase.Builder<MediAccessDatabase> {
        val userHome = System.getProperty("user.home")
        val appDataDir = File(userHome, ".mediaccess")
        if (!appDataDir.exists()) appDataDir.mkdirs()

        val dbFile = File(appDataDir, "mediaccess.db")
        return Room.databaseBuilder<MediAccessDatabase>(name = dbFile.absolutePath)
    }
}