package ru.aasmc.petfinder.common.data.api

import retrofit2.http.GET
import retrofit2.http.Query
import ru.aasmc.petfinder.common.data.api.model.ApiPaginatedAnimals

interface PetFinderApi {

    /**
     * A one shot operation that gets a page of animals from the API.
     * It is suspended, so its execution will be done in a coroutine.
     */
    @GET(ApiConstants.ANIMALS_ENDPOINT)
    suspend fun getNearbyAnimals(
        @Query(ApiParameters.PAGE) pageToLoad: Int,
        @Query(ApiParameters.LIMIT) pageSize: Int,
        @Query(ApiParameters.LOCATION) postcode: String,
        @Query(ApiParameters.DISTANCE) maxDistance: Int
    ): ApiPaginatedAnimals

    @GET(ApiConstants.ANIMALS_ENDPOINT)
    suspend fun searchAnimalsBy(
        @Query(ApiParameters.NAME) name: String,
        @Query(ApiParameters.AGE) age: String,
        @Query(ApiParameters.TYPE) type: String,
        @Query(ApiParameters.PAGE) pageToLoad: Int,
        @Query(ApiParameters.LIMIT) pageSize: Int,
        @Query(ApiParameters.LOCATION) postCode: String,
        @Query(ApiParameters.DISTANCE) maxDistance: Int,
        ): ApiPaginatedAnimals
}