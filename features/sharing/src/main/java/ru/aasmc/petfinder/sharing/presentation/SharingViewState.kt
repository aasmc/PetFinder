package ru.aasmc.petfinder.sharing.presentation

import ru.aasmc.petfinder.sharing.presentation.model.UIAnimalToShare

data class SharingViewState(
    val animalToShare: UIAnimalToShare = UIAnimalToShare(image = "", defaultMessage = "")
)