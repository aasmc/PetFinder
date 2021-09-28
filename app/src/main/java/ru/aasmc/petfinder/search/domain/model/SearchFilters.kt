package ru.aasmc.petfinder.search.domain.model

data class SearchFilters(
    val ages: List<String>,
    val types: List<String>
) {
}