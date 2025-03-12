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
        Log.d(TAG, "Calculating balances...")
        val userId = prefs.getUserId().firstOrNull()
        if (userId != null) {
            Log.d(TAG, "User id is null!")
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
            Log.d(TAG, "Done!")
            return Result.success()
        } else {
            Log.d(TAG, "User id is null!")
            return Result.failure()
        }
    }
}
