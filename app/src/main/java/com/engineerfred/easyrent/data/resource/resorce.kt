package com.engineerfred.easyrent.data.resource

sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<X>(val data: X) : Resource<X>()
    data class Error(val msg: String) : Resource<Nothing>()
}