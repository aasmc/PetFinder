package ru.aasmc.petfinder.common.data.api.interceptors

import com.squareup.moshi.Moshi
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.threeten.bp.Instant
import ru.aasmc.petfinder.common.data.api.ApiConstants
import ru.aasmc.petfinder.common.data.api.ApiConstants.AUTH_ENDPOINT
import ru.aasmc.petfinder.common.data.api.ApiParameters.AUTH_HEADER
import ru.aasmc.petfinder.common.data.api.ApiParameters.CLIENT_ID
import ru.aasmc.petfinder.common.data.api.ApiParameters.CLIENT_SECRET
import ru.aasmc.petfinder.common.data.api.ApiParameters.GRANT_TYPE_KEY
import ru.aasmc.petfinder.common.data.api.ApiParameters.GRANT_TYPE_VALUE
import ru.aasmc.petfinder.common.data.api.ApiParameters.TOKEN_TYPE
import ru.aasmc.petfinder.common.data.api.model.ApiToken
import ru.aasmc.petfinder.common.data.preferences.Preferences
import javax.inject.Inject

/**
 * Checks for token expiry, then requests a new one if needed and stores it.
 * If a valid token already exists it adds it to the request headers.
 */
class AuthenticationInterceptor @Inject constructor(
    private val preferences: Preferences
) : Interceptor {

    companion object {
        const val UNAUTHORIZED = 401
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = preferences.getToken()
        val tokenExpirationTime = Instant.ofEpochSecond(preferences.getTokenExpirationTime())
        val request = chain.request()

        // we don't need this here. It is just for educational purpuses here.
        // if (chain.request().headers[NO_AUTH_HEADER] != null) return chain.proceed(request)

        val interceptedRequest: Request = if (tokenExpirationTime.isAfter(Instant.now())) {
            chain.createAuthenticatedRequest(token)
        } else {
            // try to refresh the token
            val tokenRefreshResponse = chain.refreshToken()
            if (tokenRefreshResponse.isSuccessful) {
                val newToken = mapToken(tokenRefreshResponse)
                // try to save new token to preferences
                if (newToken.isValid()) {
                    storeNewToken(newToken)
                    // create authenticated request with the new token
                    chain.createAuthenticatedRequest(newToken.accessToken!!)
                } else {
                    request
                }
            } else {
                request
            }
        }

        return chain.proceedDeletingTokenIfUnauthorized(interceptedRequest)
    }

    private fun Interceptor.Chain.createAuthenticatedRequest(token: String): Request {
        return request()
            .newBuilder()
            .addHeader(AUTH_HEADER, TOKEN_TYPE + token)
            .build()
    }

    private fun Interceptor.Chain.refreshToken(): Response {
        val url = request()
            .url
            .newBuilder(AUTH_ENDPOINT)!!
            .build()

        val body = FormBody.Builder()
            .add(GRANT_TYPE_KEY, GRANT_TYPE_VALUE)
            .add(CLIENT_ID, ApiConstants.KEY)
            .add(CLIENT_SECRET, ApiConstants.SECRET)
            .build()

        val tokenRefresh = request()
            .newBuilder()
            .post(body)
            .url(url)
            .build()

        return proceedDeletingTokenIfUnauthorized(tokenRefresh)
    }

    private fun Interceptor.Chain.proceedDeletingTokenIfUnauthorized(request: Request): Response {
        val response = proceed(request)
        if (response.code == UNAUTHORIZED) {
            preferences.deleteTokenInfo()
        }
        return response
    }

    private fun mapToken(tokenRefreshResponse: Response): ApiToken {
        val moshi = Moshi.Builder().build()
        val tokenAdapter = moshi.adapter<ApiToken>(ApiToken::class.java)
        val responseBody = tokenRefreshResponse.body!! // if successful, this should be good:)

        return tokenAdapter.fromJson(responseBody.string()) ?: ApiToken.INVALID
    }

    private fun storeNewToken(apiToken: ApiToken) {
        with(preferences) {
            putTokenType(apiToken.tokenType!!)
            putTokenExpirationTime(apiToken.expiresAt)
            putToken(apiToken.accessToken!!)
        }
    }
}


























