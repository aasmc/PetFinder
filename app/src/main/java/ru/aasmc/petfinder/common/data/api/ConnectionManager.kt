package ru.aasmc.petfinder.common.data.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class ConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var _isConnected: AtomicBoolean = AtomicBoolean(false)

    val isConnected: Boolean
        get() = _isConnected.get()

    init {
        listenToConnectionChanges()
    }

    private fun listenToConnectionChanges() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // todo consider using atomic method set()
                _isConnected = AtomicBoolean(true)
            }

            override fun onLost(network: Network) {
                // todo consider using atomic method set()
                _isConnected = AtomicBoolean(false)
            }
        }
        cm.registerDefaultNetworkCallback(networkCallback)
    }
}