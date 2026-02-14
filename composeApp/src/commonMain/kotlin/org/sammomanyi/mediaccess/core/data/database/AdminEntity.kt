package org.sammomanyi.mediaccess.core.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admins")
data class AdminEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: String = "ADMIN",
    val createdAt: Long
)