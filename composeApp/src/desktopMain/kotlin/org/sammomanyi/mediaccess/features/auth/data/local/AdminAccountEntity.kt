package org.sammomanyi.mediaccess.features.auth.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admin_accounts")
data class AdminAccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: String = "ADMIN"              // NEW: ADMIN | RECEPTIONIST | DOCTOR | PHARMACIST
)