package com.engineerfred.easyrent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.engineerfred.easyrent.data.local.entity.TenantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TenantsDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTenant(tenant: TenantEntity)

    @Upsert
    suspend fun upsertTenants(tenants: List<TenantEntity>)

    @Update
    suspend fun updateTenant(tenant: TenantEntity)

    @Query("SELECT * FROM TENANTS WHERE id = :tenantId AND isDeleted = 0")
    suspend fun getTenantById(tenantId: String) : TenantEntity?

    @Query("SELECT * FROM TENANTS WHERE roomId = :roomId AND isDeleted = 0")
    suspend fun getTenantInRoom(roomId: String) : TenantEntity?

    @Query("SELECT * FROM TENANTS WHERE isDeleted = 0 AND userId = :userId")
    fun getAllTenants(userId: String) : Flow<List<TenantEntity>>

    @Upsert
    suspend fun insertRemoteTenants( tenants: List<TenantEntity> )

    @Query("UPDATE TENANTS SET balance = :newBalance, isSynced = 0 WHERE id = :tenantId")
    suspend fun updateTenantBalance(newBalance: Float, tenantId: String)

    @Query("DELETE FROM TENANTS WHERE id = :tenantId")
    suspend fun deleteTenant(tenantId: String)

    @Query("SELECT * FROM TENANTS WHERE isSynced = 0 AND isDeleted = 0 AND isDeleted = 0")
    suspend fun getUnsyncedTenants(): List<TenantEntity>

    @Query("SELECT * FROM TENANTS WHERE isDeleted = 1")
    suspend fun getAllLocallyDeletedTenants(): List<TenantEntity>

    @Query("UPDATE TENANTS SET isDeleted = 1, isSynced = 0 WHERE id = :tenantId")
    suspend fun markTenantAsDeleted(tenantId: String)

    //deleting everything on sign out ( clearing cache )
    @Query("DELETE FROM TENANTS")
    suspend fun deleteAllCachedTenants()

    @Query("DELETE FROM TENANTS WHERE isDeleted = 1")
    suspend fun deleteTrashedTenantsPermanently()

    @Query("SELECT * FROM tenants WHERE balance > 0 AND isDeleted = 0 AND userId = :userId")
    suspend fun getUnpaidTenants(userId: String): List<TenantEntity>

}