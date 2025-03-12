package com.engineerfred.easyrent.data.mappers

import com.engineerfred.easyrent.data.local.entity.PaymentEntity
import com.engineerfred.easyrent.data.remote.dto.PaymentDto
import com.engineerfred.easyrent.domain.modals.Payment

fun PaymentEntity.toPayment() = Payment(
    id = id,
    amount = amount,
    newBalance = newBalance,
    paymentDate = paymentDate,
    tenantId = tenantId,
    tenantName = tenantName,
    roomNumber = roomNumber,
    paymentMethod = paymentMethod,
    isSynced = isSynced,
    isDeleted = isDeleted,
    userId = userId
)

fun PaymentEntity.toPaymentDto() = PaymentDto(
    id = id,
    amount = amount,
    newBalance = newBalance,
    paymentDate = paymentDate,
    tenantId = tenantId,
    tenantName = tenantName,
    roomNumber = roomNumber,
    paymentMethod = paymentMethod,
    isSynced = isSynced,
    isDeleted = isDeleted,
    userId = userId
)

fun Payment.toPaymentEntity() = PaymentEntity(
    id = id,
    amount = amount,
    newBalance = newBalance,
    paymentDate = paymentDate,
    tenantId = tenantId,
    tenantName = tenantName,
    roomNumber = roomNumber,
    paymentMethod = paymentMethod,
    isSynced = isSynced,
    isDeleted = isDeleted,
    userId = userId
)

fun Payment.toPaymentDto() = PaymentDto(
    id = id,
    amount = amount,
    newBalance = newBalance,
    paymentDate = paymentDate,
    tenantId = tenantId,
    tenantName = tenantName,
    roomNumber = roomNumber,
    paymentMethod = paymentMethod,
    isSynced = isSynced,
    isDeleted = isDeleted,
    userId = userId
)

fun PaymentDto.toPaymentEntity() = PaymentEntity(
    id = id,
    amount = amount,
    newBalance = newBalance,
    paymentDate = paymentDate,
    tenantId = tenantId,
    tenantName = tenantName,
    roomNumber = roomNumber,
    paymentMethod = paymentMethod,
    isSynced = isSynced,
    isDeleted = isDeleted,
    userId = userId
)