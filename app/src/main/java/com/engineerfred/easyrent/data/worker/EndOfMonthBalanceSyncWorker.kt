package com.engineerfred.easyrent.data.worker


import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.engineerfred.easyrent.constants.Constants.TENANTS
import com.engineerfred.easyrent.data.local.db.CacheDatabase
import com.engineerfred.easyrent.data.mappers.toTenantDto
import com.engineerfred.easyrent.data.mappers.toTenantEntity
import com.engineerfred.easyrent.data.remote.dto.TenantDto
import com.engineerfred.easyrent.domain.repository.PreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

@HiltWorker
class EndOfMonthBalanceSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    @Assisted val cache: CacheDatabase,
    @Assisted val supabase: SupabaseClient,
    @Assisted val prefs: PreferencesRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "EndOfMonthBalanceSyncWorker"
    }

    override suspend fun doWork(): Result {

        try {
            Log.wtf("MyWorker", "EndOfMonthBalanceSyncWorker started!")
            val userId = prefs.getUserId().firstOrNull()

            if ( userId == null ) {
                Log.e(TAG, "User ID is null. Cannot sync tenants.")
                return Result.failure()
            }

            if( !isEndOfMonth() ) {
                Log.d(TAG, "Not yet the end of the month!")
                return Result.success()
            }

            updateTenantsBalanceInCache(userId)
            Log.d(TAG, "Updated tenants balance in cache!")


            updateTenantsBalanceInCloud()
            Log.d(TAG, "Updated tenants balance in cloud!")

            Log.d(TAG, "Balance updated successfully!")
            return Result.success()

        }catch (ex: Exception) {
            Log.e(TAG, "Error checking balances: ${ex.message}")
            return Result.failure()
        }
    }

    private suspend fun updateTenantsBalanceInCloud() {
        try {
            val tenantsToSync = cache.tenantsDao().getUnsyncedTenants()
            val syncedTenants = supabase.from(TENANTS)
                .upsert(tenantsToSync.map { it.copy(isSynced = true).toTenantDto() }) {
                    select()
                }.decodeList<TenantDto>()
            cache.tenantsDao().insertRemoteTenants(syncedTenants.map { it.toTenantEntity() })
        } catch (ex: Exception){
            Log.e(TAG, "Error updating tenants balance in cloud: ${ex.message}")
        }
    }

    private suspend fun updateTenantsBalanceInCache(userId: String) {
        val tenants = cache.tenantsDao().getAllTenants(userId).firstOrNull()
        tenants?.forEach { tenant ->
            val room = cache.roomsDao().getRoomById(tenant.roomId).firstOrNull()
            room?.let {
                val newBalance = if (tenant.balance == 0f) {
                    it.monthlyRent
                } else {
                    tenant.balance + it.monthlyRent
                }
                cache.tenantsDao().updateTenantBalance(newBalance, tenant.id)
            }
        }
    }

    private fun isEndOfMonth(): Boolean {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        return today == lastDay
    }
}
