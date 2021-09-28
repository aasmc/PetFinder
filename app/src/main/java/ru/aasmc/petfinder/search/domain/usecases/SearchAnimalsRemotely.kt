package ru.aasmc.petfinder.search.domain.usecases

import ru.aasmc.petfinder.common.domain.repositories.AnimalRepository
import javax.inject.Inject

class SearchAnimalsRemotely @Inject constructor(
    private val animalRepository: AnimalRepository
) {

}