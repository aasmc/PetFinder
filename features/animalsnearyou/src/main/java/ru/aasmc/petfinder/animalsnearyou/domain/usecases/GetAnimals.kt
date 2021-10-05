package ru.aasmc.petfinder.animalsnearyou.domain.usecases

import ru.aasmc.petfinder.common.domain.repositories.AnimalRepository
import javax.inject.Inject

class GetAnimals @Inject constructor(
    private val animalRepository: AnimalRepository
) {

    operator fun invoke() = animalRepository.getAnimals()

}