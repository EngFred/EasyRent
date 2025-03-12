package com.engineerfred.easyrent.data.mappers

import com.engineerfred.easyrent.data.local.entity.ExpenseEntity
import com.engineerfred.easyrent.data.remote.dto.ExpenseDto
import com.engineerfred.easyrent.domain.modals.Expense

fun ExpenseDto.toExpenseEntity(): ExpenseEntity =
    ExpenseEntity(
        id = id,
        userId = userId,
        title = title,
        amount = amount,
        category = category,
        date = date,
        notes = notes,
        isSynced = isSynced,
        isDeleted = isDeleted
    )

fun Expense.toExpenseEntity(): ExpenseEntity =
    ExpenseEntity(
        id = id,
        userId = userId,
        title = title,
        amount = amount,
        category = category,
        date = date,
        notes = notes,
        isSynced = isSynced,
        isDeleted = isDeleted
    )

fun Expense.toExpenseDTO(): ExpenseDto =
    ExpenseDto(
        id = id,
        userId = userId,
        title = title,
        amount = amount,
        category = category,
        date = date,
        notes = notes,
        isSynced = isSynced,
        isDeleted = isDeleted
    )

//fun ExpenseDto.toExpense(): Expense =
//    Expense(
//        id = id,
//        userId = userId,
//        title = title,
//        amount = amount,
//        category = category,
//        date = date,
//        notes = notes,
//        isSynced = isSynced,
//        isDeleted = isDeleted
//    )

fun ExpenseEntity.toExpense(): Expense =
    Expense(
        id = id,
        userId = userId,
        title = title,
        amount = amount,
        category = category,
        date = date,
        notes = notes,
        isSynced = isSynced,
        isDeleted = isDeleted
    )

fun ExpenseEntity.toExpenseDTO(): ExpenseDto =
    ExpenseDto(
        id = id,
        userId = userId,
        title = title,
        amount = amount,
        category = category,
        date = date,
        notes = notes,
        isSynced = isSynced,
        isDeleted = isDeleted
    )