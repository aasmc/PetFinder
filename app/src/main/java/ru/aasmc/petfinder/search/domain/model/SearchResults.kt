package ru.aasmc.petfinder.search.domain.model

import ru.aasmc.petfinder.common.domain.model.animal.Animal

data class SearchResults(
    val animals: List<Animal>,
    val searchParameters: SearchParameters
)
