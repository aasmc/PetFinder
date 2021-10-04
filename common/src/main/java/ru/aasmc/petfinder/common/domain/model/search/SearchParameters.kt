package ru.aasmc.petfinder.common.domain.model.search

import java.util.*

data class SearchParameters(
    val name: String,
    val age: String,
    val type: String
) {
    val uppercaseName get() = name.uppercase(Locale.ROOT)
    val uppercaseAge get() = age.uppercase(Locale.ROOT)
    val uppercaseType get() = type.uppercase(Locale.ROOT)
}
