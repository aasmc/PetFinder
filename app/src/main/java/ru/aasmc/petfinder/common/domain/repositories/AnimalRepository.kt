package ru.aasmc.petfinder.common.domain.repositories

import io.reactivex.Flowable
import ru.aasmc.petfinder.common.domain.model.animal.Animal
import ru.aasmc.petfinder.common.domain.model.animal.AnimalWithDetails
import ru.aasmc.petfinder.common.domain.model.animal.details.Age
import ru.aasmc.petfinder.common.domain.model.pagination.PaginatedAnimals
import ru.aasmc.petfinder.search.domain.model.SearchParameters
import ru.aasmc.petfinder.search.domain.model.SearchResults

interface AnimalRepository {

    fun getAnimals(): Flowable<List<Animal>>

    suspend fun requestMoreAnimals(pageToLoad: Int, numberOfItems: Int): PaginatedAnimals

    suspend fun storeAnimals(animals: List<AnimalWithDetails>)

    suspend fun getAnimalTypes(): List<String>

    fun getAnimalAges(): List<Age>

    fun searchCachedAnimalsBy(searchParameters: SearchParameters): Flowable<SearchResults>

    suspend fun searchAnimalsRemotely(
        pageToLoad: Int,
        searchParameters: SearchParameters,
        numberOfItems: Int
    ): PaginatedAnimals
}