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

    suspend fun isInternetAvailable(context: Context): Boolean = withContext(Dispatchers.IO) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return@withContext false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return@withContext false

        if (!activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            return@withContext false // No internet capability
        }

        // Perform a simple network request to check for actual internet connectivity
        try {
            val socket = Socket()
            socket.connect(InetSocketAddress("8.8.8.8", 53), 2000) // Google's public DNS
            socket.close()
            return@withContext true
        } catch (e: IOException) {
            return@withContext false // Connection failed
        }
    }

//    fun isInternetAvailable(context: Context): Boolean {
//        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val network = connectivityManager.activeNetwork ?: return false
//        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
//
//
//        if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
//            return false
//        }
//
//        // Try connecting to a real server to verify internet access
//        return try {
//            val url = URL("https://www.google.com")
//            val connection = url.openConnection() as HttpURLConnection
//            connection.connectTimeout = 2000 // 2 seconds timeout
//            connection.connect()
//            connection.responseCode == 200 // HTTP OK
//        } catch (e: IOException) {
//            false // No internet access
//        }
//    }
}