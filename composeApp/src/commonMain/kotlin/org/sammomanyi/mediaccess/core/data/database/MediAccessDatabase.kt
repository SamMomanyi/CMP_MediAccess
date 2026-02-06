package org.sammomanyi.mediaccess.core.data.database



import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import org.sammomanyi.mediaccess.features.identity.data.local.UserDao
import org.sammomanyi.mediaccess.features.identity.data.local.UserEntity

@Database(entities = [UserEntity::class], version = 1)
@ConstructedBy(MediAccessDatabaseConstructor::class)
abstract class MediAccessDatabase : RoomDatabase() {
    abstract val userDao: UserDao
}


// Room KMP requirement for code generation


/**
 * Helper function to build the database instance.
 * We use the BundledSQLiteDriver to ensure compatibility across Linux and Android.
 */
fun getRoomDatabase(
    builder: RoomDatabase.Builder<MediAccessDatabase>
): MediAccessDatabase {
    return builder
        .setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver())
        .setQueryCoroutineContext(kotlinx.coroutines.Dispatchers.IO)
        .build()
}