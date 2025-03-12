package com.engineerfred.easyrent.di

import com.engineerfred.easyrent.data.local.db.CacheDatabase
import com.engineerfred.easyrent.data.repository.AuthRepositoryImpl
import com.engineerfred.easyrent.data.repository.ExpensesRepositoryImpl
import com.engineerfred.easyrent.data.repository.PaymentsRepositoryImpl
import com.engineerfred.easyrent.data.repository.RoomsRepositoryImpl
import com.engineerfred.easyrent.data.repository.TenantsRepositoryImpl
import com.engineerfred.easyrent.data.repository.UserRepositoryImpl
import com.engineerfred.easyrent.domain.repository.AuthRepository
import com.engineerfred.easyrent.domain.repository.ExpensesRepository
import com.engineerfred.easyrent.domain.repository.PaymentsRepository
import com.engineerfred.easyrent.domain.repository.PreferencesRepository
import com.engineerfred.easyrent.domain.repository.RoomsRepository
import com.engineerfred.easyrent.domain.repository.TenantsRepository
import com.engineerfred.easyrent.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun providesTenantsRepository(
        supabaseClient: SupabaseClient,
        cacheDatabase: CacheDatabase,
        prefs: PreferencesRepository,
        storage: Storage
    ): TenantsRepository = TenantsRepositoryImpl(
        supabaseClient,
        cacheDatabase,
        storage,
        prefs
    )

    @Provides
    @Singleton
    fun providesRoomsRepository(
        supabaseClient: SupabaseClient,
        cacheDatabase: CacheDatabase,
        prefs: PreferencesRepository
    ): RoomsRepository = RoomsRepositoryImpl(
        supabaseClient,
        cacheDatabase,
        prefs
    )

    @Provides
    @Singleton
    fun providesPaymentsRepository(
        supabaseClient: SupabaseClient,
        cacheDatabase: CacheDatabase,
        prefs: PreferencesRepository
    ): PaymentsRepository = PaymentsRepositoryImpl(
        cacheDatabase,
        supabaseClient,
        prefs
    )

    @Provides
    @Singleton
    fun providesAuthRepository(
        supabaseClient: SupabaseClient,
        cache: CacheDatabase,
        prefs: PreferencesRepository
    ) : AuthRepository = AuthRepositoryImpl(
        supabaseClient,
        cache,
        prefs
    )

    @Provides
    @Singleton
    fun providesUserRepository(
        supabaseClient: SupabaseClient,
        cache: CacheDatabase,
        prefs: PreferencesRepository
    ) : UserRepository = UserRepositoryImpl(
        supabaseClient,
        cache,
        prefs
    )

    @Provides
    @Singleton
    fun providesExpensesRepository(
        supabaseClient: SupabaseClient,
        cache: CacheDatabase,
        prefs: PreferencesRepository
    ) : ExpensesRepository = ExpensesRepositoryImpl(
        cache,
        supabaseClient,
        prefs
    )

}