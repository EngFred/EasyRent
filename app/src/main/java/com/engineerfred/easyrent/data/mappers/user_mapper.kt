package com.engineerfred.easyrent.data.mappers

import com.engineerfred.easyrent.data.local.entity.UserInfoEntity
import com.engineerfred.easyrent.data.remote.dto.UserDto
import com.engineerfred.easyrent.domain.modals.User

fun User.toUserDto() = UserDto(
    id = id,
    firstName = firstName,
    lastName = lastName,
    imageUrl = imageUrl,
    email = email,
    telNo = telNo,
    createdAt = createdAt,
    hostelName = hostelName
)

fun UserDto.toUser() = User(
    id = id,
    firstName = firstName,
    lastName = lastName,
    imageUrl = imageUrl,
    email = email,
    telNo = telNo,
    createdAt = createdAt,
    hostelName = hostelName
)

fun UserInfoEntity.toUser() = User(
    id = id,
    firstName = firstName,
    lastName = lastName,
    imageUrl = imageUrl,
    email = email,
    telNo = telNo,
    createdAt = createdAt,
    hostelName = hostelName
)

fun UserDto.toUserInfoEntity() = UserInfoEntity(
    id = id,
    firstName = firstName,
    lastName = lastName,
    imageUrl = imageUrl,
    email = email,
    telNo = telNo,
    createdAt = createdAt,
    hostelName = hostelName
)