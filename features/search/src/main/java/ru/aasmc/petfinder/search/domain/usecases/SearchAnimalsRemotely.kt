package ru.aasmc.petfinder.search.domain.usecases

import ru.aasmc.petfinder.common.domain.model.NoMoreAnimalsException
import ru.aasmc.petfinder.common.domain.model.pagination.Pagination
import ru.aasmc.petfinder.common.domain.model.pagination.Pagination.Companion.DEFAULT_PAGE_SIZE
import ru.aasmc.petfinder.common.domain.model.search.SearchParameters
import ru.aasmc.petfinder.common.domain.repositories.AnimalRepository
import javax.inject.Inject

class SearchAnimalsRemotely @Inject constructor(
    private val animalRepository: AnimalRepository
) {

    suspend operator fun invoke(
        pageToLoad: Int,
        searchParameters: SearchParameters,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): Pagination {
        val (animals, pagination) =
            animalRepository.searchAnimalsRemotely(pageToLoad, searchParameters, pageSize)

        if (animals.isEmpty()) {
            throw NoMoreAnimalsException("Couldn't find more animals that match the search parameters.")
        }
        animalRepository.storeAnimals(animals)

        return pagination
    }
}