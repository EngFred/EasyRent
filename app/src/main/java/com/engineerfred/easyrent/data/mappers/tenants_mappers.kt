package com.engineerfred.easyrent.data.mappers

import com.engineerfred.easyrent.data.local.entity.TenantEntity
import com.engineerfred.easyrent.data.remote.dto.TenantDto
import com.engineerfred.easyrent.domain.modals.Tenant

fun TenantDto.toTenantEntity() = TenantEntity(
    id = id,
    name = name,
    contact = contact,
    email = email,
    balance = balance,
    moveInDate = moveInDate,
    roomId = roomId,
    roomNumber = roomNumber,
    emergencyContact = emergencyContact,
    idDetails = idDetails,
    notes = notes,
    profilePic = profilePic,
    isSynced = isSynced,
    userId = userId
)

fun Tenant.toTenantDto() = TenantDto(
    id = id,
    name = name,
    contact = contact,
    email = email,
    balance = balance,
    moveInDate = moveInDate,
    roomId = roomId,
    roomNumber = roomNumber,
    emergencyContact = emergencyContact,
    idDetails = idDetails,
    notes = notes,
    profilePic = profilePic,
    isSynced = isSynced,
    isDeleted = isDeleted,
    userId = userId
)

fun TenantEntity.toTenant() = Tenant(
    id = id,
    name = name,
    contact = contact,
    email = email,
    balance = balance,
    moveInDate = moveInDate,
    roomId = roomId,
    roomNumber = roomNumber,
    emergencyContact = emergencyContact,
    idDetails = idDetails,
    notes = notes,
    profilePic = profilePic,
    isSynced = isSynced,
    isDeleted = isDeleted,
    userId = userId
)

fun TenantEntity.toTenantDto() = TenantDto(
    id = id,
    name = name,
    contact = contact,
    email = email,
    balance = balance,
    moveInDate = moveInDate,
    roomId = roomId,
    roomNumber = roomNumber,
    emergencyContact = emergencyContact,
    idDetails = idDetails,
    notes = notes,
    profilePic = profilePic,
    isSynced = isSynced,
    isDeleted = isDeleted,
    userId = userId
)

fun TenantDto.toTenant() = Tenant(
    id = id,
    name = name,
    contact = contact,
    email = email,
    balance = balance,
    moveInDate = moveInDate,
    roomId = roomId,
    roomNumber = roomNumber,
    emergencyContact = emergencyContact,
    idDetails = idDetails,
    notes = notes,
    profilePic = profilePic,
    isSynced = isSynced,
    isDeleted = isDeleted,
    userId = userId
)

fun Tenant.toTenantEntity() = TenantEntity(
    id = id,
    name = name,
    contact = contact,
    email = email,
    balance = balance,
    moveInDate = moveInDate,
    roomId = roomId,
    roomNumber = roomNumber,
    emergencyContact = emergencyContact,
    idDetails = idDetails,
    notes = notes,
    profilePic = profilePic,
    isSynced = isSynced,
    isDeleted = isDeleted,
    userId = userId
)