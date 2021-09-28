package ru.aasmc.petfinder.animalsnearyou.presentation

sealed class AnimalsNearYouEvent {
    object RequestInitialAnimalsList : AnimalsNearYouEvent()
    object RequestMoreAnimals : AnimalsNearYouEvent()
}