package ru.aasmc.petfinder.common.presentation.model.mappers

import ru.aasmc.petfinder.common.domain.model.animal.Animal
import ru.aasmc.petfinder.common.presentation.model.UIAnimal
import javax.inject.Inject

class UiAnimalMapper @Inject constructor(): UiMapper<Animal, UIAnimal> {

    override fun mapToView(input: Animal): UIAnimal {
        return UIAnimal(
            id = input.id,
            name = input.name,
            photo = input.media.getFirstSmallestAvailablePhoto()
        )
    }
}
