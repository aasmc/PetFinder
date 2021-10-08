package ru.aasmc.petfinder.common.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
        // need to add interceptors in order:
        return OkHttpClient.Builder()
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