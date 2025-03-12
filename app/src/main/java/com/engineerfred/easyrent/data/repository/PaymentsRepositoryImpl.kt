package com.engineerfred.easyrent.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
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
import com.engineerfred.easyrent.data.resource.safeCall
import com.engineerfred.easyrent.domain.modals.Payment
import com.engineerfred.easyrent.domain.repository.PaymentsRepository
import com.engineerfred.easyrent.domain.repository.PreferencesRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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

    private val currentUser = supabaseClient.auth.currentUserOrNull()

    override fun getAllPayments(): Flow<Resource<List<Payment>>> = flow {
        if ( currentUser != null ) {
            Log.i(TAG, "Fetching payments from cache.....")
            val userIdInPrefs = prefs.getUserId().firstOrNull()
            val userId = if ( userIdInPrefs == currentUser.id ) currentUser.id else userIdInPrefs
            val paymentsFlow = cache.paymentsDao().getAllPayments(userId ?: currentUser.id).map {  cachedPayments ->
                Log.v(TAG, "Found ${cachedPayments.size} payments in cache!")
                if ( cachedPayments.isEmpty() ) {
                    Log.v(TAG, "No payments in cache, Fetching payments in supabase....")
                    try {
                        val remotePayments = supabaseClient.from(PAYMENTS).select{
                            filter { eq("user_id", userId ?: currentUser.id) }
                        }.decodeList<PaymentDto>()
                        if( remotePayments.isNotEmpty() ) {
                            Log.v(TAG, "Caching payments from supabase....")
                            cache.paymentsDao().insertPayments(remotePayments.map { it.toPaymentEntity() })
                            Log.v(TAG, "Payments cached successfully!!!.")
                        } else {
                            Log.v(TAG, "There aren't any payments in supabase too!!!")
                            //meaning we shall go with the cache list even if its empty
                        }
                    }catch (ex: Exception) {
                        Log.e(TAG, "Supabase error: $ex")
                    }
                }
                Resource.Success(cachedPayments.map { it.toPayment() })
            }
            emitAll(paymentsFlow)
        } else {
            Log.i(TAG, "User not logged in!")
            emit(Resource.Error("User not logged in!"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getPaymentsByTenant(tenantId: String): Flow<Resource<List<Payment>>>  = flow {
        if ( currentUser != null ) {
            Log.i(TAG, "Fetching payments by tenant from cache.....")
            val paymentsFlow = cache.paymentsDao().getPaymentsByTenant(tenantId).map {  cachedTenantPayments ->
                Log.v(TAG, "Found ${cachedTenantPayments.size} payments for this tenant in cache!")
                if ( cachedTenantPayments.isEmpty() ) {
                    Log.v(TAG, "No payments for this tenant in cache, Fetching payments for the tenant in supabase....")
                    try {
                        val remoteTenantPayments = supabaseClient.from(PAYMENTS).select(){
                            filter {
                                eq("tenant_id", tenantId)
                                eq("user_id", currentUser.id)
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
        } else {
            Log.i(TAG, "User not logged in!")
            emit(Resource.Error("User not logged in!"))
        }
    }.flowOn(Dispatchers.IO)

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun getUnsyncedPayments(): Resource<List<Payment>> = safeCall(currentUserId = currentUser?.id, logTag = TAG) {
        Log.i(TAG, "Getting unsynced Payments from cache.....")
        val unsyncedPayments = cache.paymentsDao().getAllUnsyncedPayments(currentUser!!.id)
        Log.i(TAG, "Found ${unsyncedPayments.size} unsynced payments!!")
        Resource.Success(unsyncedPayments.map { it.toPayment() })
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun getAllTrashedPayments(): Resource<List<Payment>> = safeCall(currentUserId = currentUser?.id, logTag = TAG) {
        Log.i(TAG, "Getting trashed Payments from cache.....")
        val trashedPayments = cache.paymentsDao().getAllTrashedTenants(currentUser!!.id)
        Log.i(TAG, "Found ${trashedPayments.size} trashed payments!!")
        Resource.Success(trashedPayments.map { it.toPayment() })
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun insertPayment(payment: Payment): Resource<Unit> = safeCall( currentUserId = currentUser?.id, logTag = TAG ) {
        Log.i(TAG, "Inserting payment in cache.....")
        val userIdInPrefs = prefs.getUserId().firstOrNull()
        val userId = if ( userIdInPrefs == currentUser!!.id ) currentUser.id else userIdInPrefs
        cache.paymentsDao().insertPayment(payment.copy(userId = userId ?: currentUser.id).toPaymentEntity())
        Log.i(TAG, "Updating tenant balance in cache.....")
        cache.tenantsDao().updateTenantBalance(payment.newBalance, payment.tenantId)

        try {
            Log.i(TAG, "Payment Inserted successfully in cache!...Now inserting payment in supabase...")
            val remotePayment = supabaseClient.from(PAYMENTS).insert(
                payment.copy(isSynced = true, userId = userId ?: currentUser.id).toPaymentDto()
            ) {
                select()
            }.decodeSingleOrNull<PaymentDto>()
            remotePayment?.let {
                Log.i(TAG, "Payment Inserted successfully in supabase! Updating cache...")
                cache.paymentsDao().updatePayment(remotePayment.toPaymentEntity())
            }
            Log.i(TAG, "Lastly updating tenant's balance in supabase....!")
            val result = supabaseClient.from(TENANTS).update(
                {
                    set("balance", payment.newBalance)
                    set("is_synced", true)
                }
            ){
                select()
                filter {
                    eq("id", payment.tenantId)
                    eq("user_id", userId ?: currentUser.id)
                }
            }.decodeSingleOrNull<TenantDto>()
            result?.let {
                Log.i(TAG, "Done! Updating cache......")
                cache.tenantsDao().updateTenant(result.toTenantEntity())
                Log.i(TAG, "Both local and remote process were successfully!")
            }
            Log.i(TAG, "Done!")
        }catch (ex: Exception) {
            Log.e(TAG, "Insertion error: ${ex.message}")
        }
        Resource.Success(Unit)
    }

//    override suspend fun updatePaymentStatus(paymentId: Int): Resource<Unit> {
//        TODO("Not yet implemented")
//    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun deletePayment(payment: Payment): Resource<Unit> = safeCall(currentUserId = currentUser?.id, logTag = TAG) {
        Log.i(TAG, "Marking payment as deleted.....")
        val userIdInPrefs = prefs.getUserId().firstOrNull()
        if (currentUser!!.id == userIdInPrefs && payment.userId == currentUser.id  ) {
            cache.paymentsDao().updatePayment(payment.copy(isDeleted = true, isSynced = false).toPaymentEntity())
            Log.i(TAG, "Successfully marked payment as deleted! Deleting payment from supabase...")
            supabaseClient.from(PAYMENTS).delete{
                filter {
                    eq("id", payment.id)
                    eq("user_id", currentUser.id)
                }
            }
            Log.i(TAG, "Payment deleted successfully from supabase! deleting permanently from cache...")
            cache.paymentsDao().deletePayment(payment.toPaymentEntity())
            Log.i(TAG, "Payment deleted from cache successfully!")
            Resource.Success(Unit)
        } else {
            Resource.Error("Can't delete that payment!")
        }
    }
}