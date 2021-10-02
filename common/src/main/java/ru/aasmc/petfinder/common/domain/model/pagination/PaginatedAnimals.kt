package ru.aasmc.petfinder.common.domain.model.pagination

import ru.aasmc.petfinder.common.domain.model.animal.AnimalWithDetails

/**
 * Value object that associates a list of animas with a specific page.
 */
data class PaginatedAnimals(
    val animals: List<AnimalWithDetails>,
    val pagination: Pagination
)
