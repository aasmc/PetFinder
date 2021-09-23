package ru.aasmc.petfinder.common.domain.repositories

import io.reactivex.Flowable
import ru.aasmc.petfinder.common.domain.model.animal.Animal
import ru.aasmc.petfinder.common.domain.model.animal.AnimalWithDetails
import ru.aasmc.petfinder.common.domain.model.pagination.PaginatedAnimals

interface AnimalRepository {

    fun getAnimals(): Flowable<List<Animal>>

    suspend fun requestMoreAnimals(pageToLoad: Int, numberOfItems: Int): PaginatedAnimals

    suspend fun storeAnimals(animals: List<AnimalWithDetails>)
}