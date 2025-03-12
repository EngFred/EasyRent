package com.engineerfred.easyrent.data.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.engineerfred.easyrent.data.local.db.CacheDatabase
import com.engineerfred.easyrent.domain.repository.PreferencesRepository
import io.github.jan.supabase.SupabaseClient
import javax.inject.Inject

class WorkerFactory @Inject constructor(
    private val cache: CacheDatabase,
    private val client: SupabaseClient,
    private val prefsRepository: PreferencesRepository
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when(workerClassName) {
            TenantsSyncWorker::class.java.name -> TenantsSyncWorker(
                appContext,
                workerParameters,
                client,
                cache,
                prefsRepository
            )

            RoomsSyncWorker::class.java.name -> RoomsSyncWorker(
                appContext,
                workerParameters,
                client,
                cache,
                prefsRepository
            )

            PaymentsSyncWorker::class.java.name -> PaymentsSyncWorker(
                appContext,
                workerParameters,
                cache,
                client,
                prefsRepository
            )

            UnpaidTenantsWorker::class.java.name -> UnpaidTenantsWorker(
                appContext,
                workerParameters,
                cache,
                prefsRepository
            )

            EndOfMonthBalanceSyncWorker::class.java.name -> EndOfMonthBalanceSyncWorker(
                appContext,
                workerParameters,
                cache,
                client,
                prefsRepository
            )

            ExpensesSyncWorker::class.java.name -> ExpensesSyncWorker(
                appContext,
                workerParameters,
                client,
                cache,
                prefsRepository
            )

            else -> null
        }
    }
}