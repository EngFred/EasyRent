package com.engineerfred.easyrent.domain.repository

import android.content.ContentResolver
import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.domain.modals.Tenant
import kotlinx.coroutines.flow.Flow

interface TenantsRepository {
    suspend fun insertTenant(tenant: Tenant, contentResolver: ContentResolver) : Resource<Any>
    //suspend fun updateTenant(tenant: Tenant) : Resource<Any>
    suspend fun deleteTenant(tenant: Tenant) : Resource<Any>
    suspend fun getTenantInRoom(roomId: String) : Resource<Tenant?>
    fun getAllTenants() : Flow<Resource<List<Tenant>>>
    //suspend fun getTenantById(tenantId: Int) : Resource<Tenant?>
    //suspend fun uploadImage(contentResolver: ContentResolver, imageUrl: String): Resource<String>
}