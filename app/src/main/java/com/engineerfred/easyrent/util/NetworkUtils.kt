package com.engineerfred.easyrent.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

object NetworkUtils  {

//    suspend fun isInternetAvailable(context: Context): Boolean = withContext(Dispatchers.IO) {
//        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val network = connectivityManager.activeNetwork ?: return@withContext false
//        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return@withContext false
//
//        if (!activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
//            return@withContext false // No internet capability
//        }
//
//        // Perform a simple network request to check for actual internet connectivity
//        try {
//            val socket = Socket()
//            socket.connect(InetSocketAddress("8.8.8.8", 53), 2000) // Google's public DNS
//            socket.close()
//            return@withContext true
//        } catch (e: IOException) {
//            return@withContext false // Connection failed
//        }
//    }

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}