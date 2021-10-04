package ru.aasmc.petfinder.common.domain.usecases

import io.reactivex.Single
import ru.aasmc.petfinder.common.domain.model.animal.AnimalWithDetails
import ru.aasmc.petfinder.common.domain.repositories.AnimalRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetAnimalDetails @Inject constructor(
    private val animalRepository: AnimalRepository
) {
    operator fun invoke(animalId: Long): Single<AnimalWithDetails> {
        return animalRepository.getAnimal(animalId).delay(2, TimeUnit.SECONDS)
    }
}