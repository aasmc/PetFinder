package ru.aasmc.petfinder.common.domain.model.search

data class SearchFilters(
    val ages: List<String>,
    val types: List<String>
) {
}