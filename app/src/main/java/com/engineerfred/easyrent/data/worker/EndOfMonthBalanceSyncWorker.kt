package com.engineerfred.easyrent.data.worker


import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.engineerfred.easyrent.constants.Constants.TENANTS
import com.engineerfred.easyrent.data.local.db.CacheDatabase
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
                    val result = supabase.from(TENANTS).update(
                        {
                            set("balance", newBalance)
                            set("is_synced", true)
                        }
                    ){
                        select()
                        filter {
                            eq("id", tenant.id)
                            eq("user_id", userId)
                        }
                    }.decodeSingleOrNull<TenantDto>()

                    result?.let { tenant ->
                        cache.tenantsDao().updateTenant(tenant.toTenantEntity())
                    }
                }
            }

            Log.d(TAG, "Success!")
            return Result.success()

        }catch (ex: Exception) {
            Log.e(TAG, "Error checking balances: ${ex.message}")
            return Result.failure()
        }
    }

    private fun isEndOfMonth(): Boolean {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        return today == lastDay
    }
}
