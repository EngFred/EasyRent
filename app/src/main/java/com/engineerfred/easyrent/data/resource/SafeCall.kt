package com.engineerfred.easyrent.data.resource

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.exceptions.HttpRequestException
import java.io.IOException

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
inline fun <T> safeCall(currentUserId: String?, logTag: String = "KoTlin", action: () -> Resource<T> ) : Resource<T> {
    if ( currentUserId != null ) {
        return try {
            action()
        } catch (ex: Exception) {
            val errorMessage = when (ex) {
                is IOException, is HttpRequestException -> "Network error!"
                else -> "An unexpected error occurred: ${ex.message}"
            }
            Log.e(logTag, "Error in safeCall: $errorMessage", ex)
            Resource.Error(errorMessage) // Return an error Resource
        }
    } else {
        Log.e(logTag, "User is not logged in!")
        return Resource.Error("User is not logged in!")
    }
}

suspend inline fun <T> authSafeCall(logTag: String = "KoTlin", auth: Auth, action: () -> Resource<T> ) : Resource<T> {
    return try {
        action()
    } catch (ex: Exception) {
        auth.signOut()
        val errorMessage = when (ex) {
            is IOException, is HttpRequestException -> "Network error!"
            else -> "An unexpected error occurred: ${ex.message}"
        }
        Log.e(logTag, "Error in safeCall: $errorMessage", ex)
        Resource.Error(errorMessage) // Return an error Resource
    }
}

//inline fun <T> safeCalll( action: () -> Resource<T> ) : Resource<T> {
//    return try {
//        action()
//    } catch (ex: Exception) {
//        Log.e("TAG", "Error: ${ex.message}", ex)
//        Resource.Error(ex.message ?: "Unknown error occurred")
//    }
//}