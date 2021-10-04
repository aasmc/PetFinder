package ru.aasmc.petfinder.common.data.cache

import io.reactivex.Flowable
import io.reactivex.Single
import ru.aasmc.petfinder.common.data.cache.model.cachedanimal.CachedAnimalAggregate
import ru.aasmc.petfinder.common.data.cache.model.cachedorganization.CachedOrganization

interface Cache {
    fun storeOrganizations(organizations: List<CachedOrganization>)
    fun getOrganization(organizationId: String): Single<CachedOrganization>

    fun getNearbyAnimals(): Flowable<List<CachedAnimalAggregate>>

    fun storeNearbyAnimals(animals: List<CachedAnimalAggregate>)

    fun getAnimal(animalId: Long): Single<CachedAnimalAggregate>

    suspend fun getAllTypes(): List<String>

    fun searchAnimalsBy(
        name: String,
        age: String,
        type: String
    ): Flowable<List<CachedAnimalAggregate>>

}