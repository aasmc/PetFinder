package ru.aasmc.petfinder.common.domain.usecases

import ru.aasmc.petfinder.common.domain.model.animal.AnimalWithDetails
import ru.aasmc.petfinder.common.domain.repositories.AnimalRepository
import javax.inject.Inject

class GetAnimalDetails @Inject constructor(
    private val animalRepository: AnimalRepository
) {
    suspend operator fun invoke(animalId: Long): AnimalWithDetails {
        return animalRepository.getAnimal(animalId)
    }
}