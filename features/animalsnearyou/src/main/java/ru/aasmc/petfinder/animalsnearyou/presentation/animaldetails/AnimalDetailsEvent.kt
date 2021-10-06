package ru.aasmc.petfinder.animalsnearyou.presentation.animaldetails

sealed class AnimalDetailsEvent {
    data class LoadAnimalDetails(val animalId: Long) : AnimalDetailsEvent()
    object AdoptAnimal: AnimalDetailsEvent()
}