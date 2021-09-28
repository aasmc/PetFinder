package ru.aasmc.petfinder.search.domain.usecases

import ru.aasmc.petfinder.common.domain.model.animal.details.Age
import ru.aasmc.petfinder.common.domain.repositories.AnimalRepository
import ru.aasmc.petfinder.search.domain.model.SearchFilters
import java.util.*
import javax.inject.Inject

class GetSearchFilters @Inject constructor(
    private val animalRepository: AnimalRepository
) {
    companion object {
        const val NO_FILTER_SELECTED = "Any"
    }

    suspend operator fun invoke(): SearchFilters {
        val unknown = Age.UNKNOWN.name

        val types = listOf(NO_FILTER_SELECTED) + animalRepository.getAnimalTypes()

        val ages = animalRepository.getAnimalAges()
            .map {
                if (it.name == unknown) {
                    NO_FILTER_SELECTED
                } else {
                    it.name.lowercase(Locale.ROOT).replaceFirstChar { ch ->
                        ch.uppercase(Locale.ROOT)
                    }
                }
            }
        return SearchFilters(ages, types)
    }
}