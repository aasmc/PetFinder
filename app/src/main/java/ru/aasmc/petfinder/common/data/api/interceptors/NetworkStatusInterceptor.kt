package ru.aasmc.petfinder.common.data.api.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import ru.aasmc.petfinder.common.data.api.ConnectionManager
import ru.aasmc.petfinder.common.domain.model.NetworkUnavailableException
import javax.inject.Inject

/**
 * Uses [ConnectionManager] to check the internet connection, then either throws a
 * [NetworkUnavailableException] or proceeds with the request.
 */
class NetworkStatusInterceptor @Inject constructor(
    private val connectionManager: ConnectionManager
): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return if (connectionManager.isConnected) {
            chain.proceed(chain.request())
        } else {
            throw NetworkUnavailableException()
        }
    }
}