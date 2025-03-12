package com.engineerfred.easyrent.di

import android.content.Context
import androidx.room.Room
import com.engineerfred.easyrent.data.local.db.CacheDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CacheModule {

    @Provides
    @Singleton
    fun providesCacheDatabase(
        @ApplicationContext
        context: Context
    ) = Room.databaseBuilder(
        context,
        CacheDatabase::class.java,
        "cache.db"
    ).fallbackToDestructiveMigration().build()

    @Provides
    @Singleton
    fun providesTenantsDao( cacheDatabase: CacheDatabase) = cacheDatabase.tenantsDao()

    @Provides
    @Singleton
    fun providesRoomsDao( cacheDatabase: CacheDatabase) = cacheDatabase.roomsDao()

}