package com.engineerfred.easyrent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.engineerfred.easyrent.data.local.entity.UserInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserInfo(userInfo: UserInfoEntity)

    @Query("SELECT * FROM userInfo WHERE id = :userId")
    fun getUserInfo(userId: String) : Flow<UserInfoEntity?>

    @Query("UPDATE userInfo SET firstName = :firstName, lastName = :lastName WHERE id = :userId")
    suspend fun updateUserNames(userId: String, firstName: String, lastName: String)

    @Query("UPDATE userInfo SET hostelName = :hostelName WHERE id = :userId")
    suspend fun updateHostelName(userId: String, hostelName: String)

    @Query("UPDATE userInfo SET telNo = :telNo WHERE id = :userId")
    suspend fun updateTelephoneNumber(userId: String, telNo: String)

    @Query("UPDATE userInfo SET imageUrl = :imageUrl WHERE id = :userId")
    suspend fun updateUserProfileImage(userId: String, imageUrl: String)

    @Query("DELETE FROM userInfo")
    suspend fun deleteUserInfo()

}