package com.engineerfred.easyrent.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.engineerfred.easyrent.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentsDao {

    @Query("SELECT * FROM payments WHERE isDeleted = 0 AND userId = :userId")
    fun getAllPayments(userId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE tenantId = :tenantId AND isDeleted = 0")
    fun getPaymentsByTenant( tenantId: String ) : Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE isSynced = 0 AND isDeleted = 0 AND userId = :userId")
    suspend fun getAllUnsyncedPayments(userId: String): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE isDeleted = 1 AND userId = :userId")
    suspend fun getAllTrashedTenants(userId: String): List<PaymentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Upsert
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Query("UPDATE payments SET isSynced = 0 WHERE id = :paymentId")
    suspend fun updatePaymentStatus(paymentId: String)

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)

    @Query("UPDATE payments SET isDeleted = 1, isSynced = 0 WHERE id = :paymentId")
    suspend fun markPaymentAsDeleted(paymentId: String)

    //deleting everything on sign out ( clearing cache )
    @Query("DELETE FROM payments")
    suspend fun deleteAllCachedPayments()

}