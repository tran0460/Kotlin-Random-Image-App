package com.example.assignment1_son_tran

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities


class InternetConnection (private val context: Context) {

    val isConnected: Boolean
        get() = checkNetworkConnectivity()

    private fun checkNetworkConnectivity(): Boolean {

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val connection = connectivityManager.getNetworkCapabilities(network)

        return (connection?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false ||
                connection?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false)
    }

}