package org.sammomanyi.mediaccess.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AdminDao {

    @Query("SELECT * FROM admins WHERE email = :email LIMIT 1")
    suspend fun getAdminByEmail(email: String): AdminEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAdmin(admin: AdminEntity)

    @Query("SELECT COUNT(*) FROM admins")
    suspend fun getAdminCount(): Int
}