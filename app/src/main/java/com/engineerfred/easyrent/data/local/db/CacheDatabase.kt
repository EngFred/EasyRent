package com.engineerfred.easyrent.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Transaction
import com.engineerfred.easyrent.data.local.dao.ExpensesDao
import com.engineerfred.easyrent.data.local.dao.PaymentsDao
import com.engineerfred.easyrent.data.local.dao.RoomsDao
import com.engineerfred.easyrent.data.local.dao.TenantsDao
import com.engineerfred.easyrent.data.local.dao.UserInfoDao
import com.engineerfred.easyrent.data.local.entity.ExpenseEntity
import com.engineerfred.easyrent.data.local.entity.PaymentEntity
import com.engineerfred.easyrent.data.local.entity.RoomEntity
import com.engineerfred.easyrent.data.local.entity.TenantEntity
import com.engineerfred.easyrent.data.local.entity.UserInfoEntity

@Database(
    entities = [
        TenantEntity::class,
        RoomEntity::class,
        PaymentEntity::class,
        UserInfoEntity::class,
        ExpenseEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CacheDatabase : RoomDatabase() {
    abstract fun tenantsDao(): TenantsDao
    abstract fun roomsDao(): RoomsDao
    abstract fun paymentsDao(): PaymentsDao
    abstract fun userInfoDao() : UserInfoDao
    abstract fun expenseDao(): ExpensesDao

    @Transaction
    suspend fun updateCache(
        user: UserInfoEntity,
        rooms: List<RoomEntity>,
        tenants: List<TenantEntity>,
        payments: List<PaymentEntity>,
        expenses: List<ExpenseEntity>
    ) {
        userInfoDao().insertUserInfo(user)
        roomsDao().upsertRooms(rooms)
        tenantsDao().upsertTenants(tenants)
        paymentsDao().insertPayments(payments)
        expenseDao().cacheAllRemoteExpenses(expenses)
    }
}