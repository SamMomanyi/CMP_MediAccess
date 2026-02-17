package org.sammomanyi.mediaccess.features.auth.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminAccountDao {
    @Query("SELECT * FROM admin_accounts WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): AdminAccountEntity?

    @Query("SELECT * FROM admin_accounts ORDER BY name ASC")
    fun getAll(): Flow<List<AdminAccountEntity>>

    @Query("SELECT * FROM admin_accounts WHERE role = :role ORDER BY name ASC")
    fun getByRole(role: String): Flow<List<AdminAccountEntity>>

    @Upsert
    suspend fun upsert(entity: AdminAccountEntity)

    @Delete
    suspend fun delete(entity: AdminAccountEntity)

    @Query("SELECT COUNT(*) FROM admin_accounts")
    suspend fun count(): Int
}