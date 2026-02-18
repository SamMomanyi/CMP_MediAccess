package org.sammomanyi.mediaccess

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.use
import org.sammomanyi.mediaccess.features.auth.data.local.AdminAccountDao
import org.sammomanyi.mediaccess.features.auth.data.local.AdminAccountEntity
import org.sammomanyi.mediaccess.features.cover.data.local.CoverLinkRequestDao
import org.sammomanyi.mediaccess.features.cover.data.local.CoverLinkRequestEntity
import java.security.MessageDigest
import java.util.UUID

@Database(
    entities = [AdminAccountEntity::class, CoverLinkRequestEntity::class],
    version = 3,
    exportSchema = false
)
abstract class MediAccessAdminDatabase : RoomDatabase() {
    abstract val adminAccountDao: AdminAccountDao
    abstract val coverLinkRequestDao: CoverLinkRequestDao
}

// ✅ Bootstrap callback - creates default admin on first launch
class BootstrapAdminCallback : RoomDatabase.Callback() {
    override fun onCreate(connection: SQLiteConnection) {
        super.onCreate(connection)

        // Create default admin account: admin@hospital.com / Admin123
        val defaultPassword = "Admin123"
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(defaultPassword.toByteArray())
            .joinToString("") { "%02x".format(it) }

        val id = UUID.randomUUID().toString()

        connection.prepare("""
            INSERT INTO admin_accounts (id, name, email, passwordHash, role)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()).use { statement ->
            statement.bindText(1, id)
            statement.bindText(2, "System Administrator")
            statement.bindText(3, "admin@hospital.com")
            statement.bindText(4, hash)
            statement.bindText(5, "ADMIN")
            statement.step()
        }

        println("✅ Default admin account created:")
        println("   Email: admin@hospital.com")
        println("   Password: Admin123")
        println("   Role: ADMIN")
    }
}