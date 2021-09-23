package ru.aasmc.petfinder.common.data.cache

import io.reactivex.Flowable
import ru.aasmc.petfinder.common.data.cache.model.cachedanimal.CachedAnimalAggregate
import ru.aasmc.petfinder.common.data.cache.model.cachedorganization.CachedOrganization

interface Cache {
    suspend fun storeOrganizations(organizations: List<CachedOrganization>)

    fun getNearbyAnimals(): Flowable<List<CachedAnimalAggregate>>

    suspend fun storeNearbyAnimals(animals: List<CachedAnimalAggregate>)

}