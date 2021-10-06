package ru.aasmc.petfinder.animalsnearyou.presentation.animaldetails

import ru.aasmc.petfinder.animalsnearyou.presentation.animaldetails.model.UIAnimalDetailed

sealed class AnimalDetailsViewState {
    object Loading : AnimalDetailsViewState()

    data class AnimalDetails(
        val animal: UIAnimalDetailed,
        val adopted: Boolean = false
    ) : AnimalDetailsViewState()

    object Failure : AnimalDetailsViewState()
}