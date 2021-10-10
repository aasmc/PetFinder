package ru.aasmc.petfinder.common.data.di

import com.babylon.certificatetransparency.certificateTransparencyInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import ru.aasmc.petfinder.common.data.api.ApiConstants
import ru.aasmc.petfinder.common.data.api.PetFinderApi
import ru.aasmc.petfinder.common.data.api.interceptors.AuthenticationInterceptor
import ru.aasmc.petfinder.common.data.api.interceptors.LoggingInterceptor
import ru.aasmc.petfinder.common.data.api.interceptors.NetworkStatusInterceptor
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ApiModule {

    /**
     * [PetFinderApi] is a stateless interface that is used to make requests and
     * return responses. So a [Singleton] is a perfect choice.
     *
     * Besides, each OkHttp instance has its own thread pool, which is expensive to create
     * AND it also has a request cache on disk. So different OkHttp instances will have
     * different caches. So [Singleton] is indeed a perfect choice.
     */
    @Provides
    @Singleton
    fun provideApi(builder: Retrofit.Builder): PetFinderApi {
        return builder
            .build()
            .create(PetFinderApi::class.java)
    }

    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit.Builder {
        return Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_ENDPOINT)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
    }

    @Provides
    fun provideOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        networkStatusInterceptor: NetworkStatusInterceptor,
        authenticationInterceptor: AuthenticationInterceptor
    ): OkHttpClient {

        val hostName = "**.petfinder.com"
        val certificatePinner = CertificatePinner.Builder()
            .add(hostName, "sha256/jx6sz/faeVkZtFfGl9r3BIxIkhOKqffMsK0iEi+1FX8=")
            .add(hostName, "sha256/JSMzqOOrtyOT1kmau6zKhgT676hGgczD5VMdRMyJZFA=")
            .build()

        val ctInterceptor = certificateTransparencyInterceptor {
            +"*.petfinder.com" // for subdomains
            +"petfinder.com" // the asterisk doesn't cover base domain, so need to put it as well
            // "*.*" // this will add all hosts
            // -"legacy.petfinder.com"  // this will exclude specific hosts
        }

        // need to add interceptors in order:
        return OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .addNetworkInterceptor(ctInterceptor)
            // first check network
            .addInterceptor(networkStatusInterceptor)
            // second check authentication
            .addInterceptor(authenticationInterceptor)
            // third add logging capabilities
            .addInterceptor(httpLoggingInterceptor)
            // prevent caching data sent over the network
//            .cache(null)
            .build()
    }

    @Provides
    fun provideHttpLoggingInterceptor(loggingInterceptor: LoggingInterceptor): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor(loggingInterceptor)

        interceptor.level = HttpLoggingInterceptor.Level.BODY

        return interceptor
    }
}