package com.example.game.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkMonitor(private val context: Context) {

    companion object {
        private const val TAG = "NetworkMonitor"
    }

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _isConnected = MutableStateFlow(checkInitialConnectivity())
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    init {
        registerNetworkCallback()
    }

    private fun checkInitialConnectivity(): Boolean {
        return try {
            val cm = connectivityManager ?: return false
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            true // assume connected if query fails
        }
    }

    private fun registerNetworkCallback() {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager?.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.i(TAG, "Network is available")
                    _isConnected.value = true
                }

                override fun onLost(network: Network) {
                    Log.w(TAG, "Network connection lost")
                    _isConnected.value = false
                }
            })
        } catch (e: Exception) {
            Log.w(TAG, "Failed registering network callback: ${e.message}")
        }
    }

    suspend fun <T> executeWithRetry(
        maxAttempts: Int = 3,
        initialDelayMs: Long = 500L,
        factor: Double = 2.0,
        operation: suspend () -> T
    ): Result<T> {
        var currentDelay = initialDelayMs
        repeat(maxAttempts) { attempt ->
            try {
                val result = operation()
                return Result.success(result)
            } catch (e: Exception) {
                if (attempt == maxAttempts - 1) {
                    return Result.failure(e)
                }
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong()
            }
        }
        return Result.failure(IllegalStateException("Exhausted retries"))
    }
}
