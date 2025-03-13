package com.engineerfred.easyrent.data.repository

import android.util.Log
import com.engineerfred.easyrent.constants.Constants.PAYMENTS
import com.engineerfred.easyrent.constants.Constants.TENANTS
import com.engineerfred.easyrent.data.local.db.CacheDatabase
import com.engineerfred.easyrent.data.mappers.toPayment
import com.engineerfred.easyrent.data.mappers.toPaymentDto
import com.engineerfred.easyrent.data.mappers.toPaymentEntity
import com.engineerfred.easyrent.data.mappers.toTenantEntity
import com.engineerfred.easyrent.data.remote.dto.PaymentDto
import com.engineerfred.easyrent.data.remote.dto.TenantDto
import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.domain.modals.Payment
import com.engineerfred.easyrent.domain.repository.PaymentsRepository
import com.engineerfred.easyrent.domain.repository.PreferencesRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PaymentsRepositoryImpl @Inject constructor(
    private val cache: CacheDatabase,
    private val supabaseClient: SupabaseClient,
    private val prefs: PreferencesRepository
) : PaymentsRepository {

    companion object {
        private const val TAG = "PaymentsRepositoryImpl"
    }

    override fun getAllPayments(): Flow<Resource<List<Payment>>> = flow {
        val userId = prefs.getUserId().firstOrNull() ?: run {
            emit(Resource.Error("User not logged in!"))
            return@flow
        }
        Log.i(TAG, "Fetching payments from cache.....")

        val paymentsFlow = cache.paymentsDao().getAllPayments(userId).map {  cachedPayments ->
            Log.v(TAG, "Found ${cachedPayments.size} payments in cache!")
            if ( cachedPayments.isEmpty() ) {
                Log.v(TAG, "No payments in cache, Fetching payments in supabase....")

                try {
                    val remotePayments = supabaseClient.from(PAYMENTS).select{
                        filter { eq("user_id", userId) }
                    }.decodeList<PaymentDto>()

                    if( remotePayments.isNotEmpty() ) {
                        Log.v(TAG, "Caching payments from supabase....")
                        cache.paymentsDao().insertPayments(remotePayments.map { it.toPaymentEntity() })
                        Log.v(TAG, "Payments cached successfully!!!.")
                    }

                }catch (ex: Exception) {
                    Log.e(TAG, "Supabase error: $ex")
                }
            }
            Resource.Success(cachedPayments.map { it.toPayment() })
        }
        emitAll(paymentsFlow)
    }.flowOn(Dispatchers.IO).catch {
        Log.e(TAG, "Error fetching payments: ${it.message}", it)
        emit(Resource.Error("Error fetching payments: ${it.message}"))
    }.distinctUntilChanged()

    override fun getPaymentsByTenant(tenantId: String): Flow<Resource<List<Payment>>>  = flow {

        val userId = prefs.getUserId().firstOrNull() ?: run {
            emit(Resource.Error("User not logged in!"))
            return@flow
        }

        Log.i(TAG, "Fetching payments by tenant from cache.....")
        val paymentsFlow = cache.paymentsDao().getPaymentsByTenant(tenantId).map {  cachedTenantPayments ->
            Log.v(TAG, "Found ${cachedTenantPayments.size} payments for this tenant in cache!")
            if ( cachedTenantPayments.isEmpty() ) {
                Log.v(TAG, "No payments for this tenant in cache, Fetching payments for the tenant in supabase....")
                try {
                    val remoteTenantPayments = supabaseClient.from(PAYMENTS).select {
                        filter {
                            eq("tenant_id", tenantId)
                            eq("user_id", userId)
                        }
                    }.decodeList<PaymentDto>()

                    if( remoteTenantPayments.isNotEmpty() ) {
                        Log.v(TAG, "Caching tenant payments from supabase....")
                        cache.paymentsDao().insertPayments(remoteTenantPayments.map { it.toPaymentEntity() })
                        Log.v(TAG, "Tenant Payments cached successfully!!!.")
                    }
                }catch (ex: Exception) {
                    Log.e(TAG, "Supabase error: $ex")
                }
            }
            Resource.Success(cachedTenantPayments.map { it.toPayment() })
        }
        emitAll(paymentsFlow)
    }.flowOn(Dispatchers.IO).catch {
        Log.e(TAG, "Error fetching payments: ${it.message}", it)
        emit(Resource.Error("Error fetching payments: ${it.message}"))
    }.distinctUntilChanged()

    override suspend fun getUnsyncedPayments(): Resource<List<Payment>> {
        try {
            val userId = prefs.getUserId().firstOrNull()

            if (userId == null) {
                Log.i(TAG, "User not logged in || user id from prefs is null")
                return Resource.Error("Not logged in!")
            }

            Log.i(TAG, "Getting unsynced Payments from cache.....")
            val unsyncedPayments = cache.paymentsDao().getAllUnsyncedPayments(userId)

            Log.i(TAG, "Found ${unsyncedPayments.size} unsynced payments!!")
            return Resource.Success(unsyncedPayments.map { it.toPayment() })
        }catch (ex: Exception) {
            Log.e(TAG, "Error fetching unsynced payments: ${ex.message}")
            return Resource.Error("Error fetching unsynced payments: ${ex.message}")
        }
    }

    override suspend fun getAllTrashedPayments(): Resource<List<Payment>> {
        try {
            val userId = prefs.getUserId().firstOrNull()

            if (userId == null) {
                Log.i(TAG, "User not logged in || user id from prefs is null")
                return Resource.Error("Not logged in!")
            }

            Log.i(TAG, "Getting trashed Payments from cache.....")
            val trashedPayments = cache.paymentsDao().getAllTrashedTenants(userId)

            Log.i(TAG, "Found ${trashedPayments.size} trashed payments!!")
            return Resource.Success(trashedPayments.map { it.toPayment() })
        }catch (ex: Exception) {
            Log.e(TAG, "Error fetching trashed payments: ${ex.message}")
            return Resource.Error("Error fetching trashed payments: ${ex.message}")
        }
    }

    override suspend fun insertPayment(payment: Payment): Resource<Unit> {
        try {

            val userId = prefs.getUserId().firstOrNull()
            if (userId == null) {
                Log.i(TAG, "User not logged in || user id from prefs is null")
                return Resource.Error("Not logged in!")
            }

            Log.i(TAG, "Inserting payment in cache.....")
            cache.paymentsDao().insertPayment(payment.copy(userId = userId).toPaymentEntity())

            Log.i(TAG, "Updating tenant balance in cache.....")
            cache.tenantsDao().updateTenantBalance(payment.newBalance, payment.tenantId)

            try {

                val remotePayment = supabaseClient.from(PAYMENTS).insert(
                    payment.copy(isSynced = true, userId = userId).toPaymentDto()
                ) {
                    select()
                }.decodeSingleOrNull<PaymentDto>()

                remotePayment?.let { cache.paymentsDao().updatePayment(remotePayment.toPaymentEntity()) }

                val result = supabaseClient.from(TENANTS).update(
                    {
                        set("balance", payment.newBalance)
                        set("is_synced", true)
                    }
                ){
                    select()
                    filter {
                        eq("id", payment.tenantId)
                        eq("user_id", userId)
                    }
                }.decodeSingleOrNull<TenantDto>()

                result?.let { cache.tenantsDao().updateTenant(result.toTenantEntity()) }

            }catch (ex: Exception) {
                Log.e(TAG, "Insertion error: ${ex.message}")
            }

            Log.i(TAG, "Payment inserted successfully!")
            return Resource.Success(Unit)
        }catch (ex: Exception) {
            Log.e(TAG, "Error inserting payment: ${ex.message}")
            return Resource.Error("Error inserting payment: ${ex.message}")
        }
    }

    override suspend fun deletePayment(payment: Payment): Resource<Unit> {
        try {

            val userId = prefs.getUserId().firstOrNull()

            if (userId == null) {
                Log.i(TAG, "User not logged in || user id from prefs is null")
                return Resource.Error("Not logged in!")
            }

            if ( userId != payment.userId ) {
                Log.i(TAG, "Can't delete payment")
                return Resource.Error("Can't delete payment!")
            }

            Log.i(TAG, "Marking payment as deleted.....")
            cache.paymentsDao().updatePayment(
                payment.copy(isDeleted = true, isSynced = false).toPaymentEntity()
            )
            Log.i(TAG,
                "Successfully marked payment as deleted! Deleting payment from supabase..."
            )

            try {
                supabaseClient.from(PAYMENTS).delete {
                    filter {
                        eq("id", payment.id)
                        eq("user_id", userId)
                    }
                }
                Log.i( TAG,
                    "Payment deleted successfully from supabase! deleting permanently from cache..."
                )
                cache.paymentsDao().deletePayment(payment.toPaymentEntity())
            } catch (ex: Exception) {
                Log.e(TAG, "Supabase error: ${ex.message}")
            }

            Log.i(TAG, "Payment deleted successfully!")
            return Resource.Success(Unit)

        } catch (ex: Exception) {
            Log.e(TAG, "Error deleting payment: ${ex.message}")
            return Resource.Error("Error deleting payment: ${ex.message}")
        }
    }
}