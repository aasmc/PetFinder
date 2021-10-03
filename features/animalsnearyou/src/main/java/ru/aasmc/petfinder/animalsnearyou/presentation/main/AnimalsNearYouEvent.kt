package ru.aasmc.petfinder.animalsnearyou.presentation.main

sealed class AnimalsNearYouEvent {
    object RequestMoreAnimals : AnimalsNearYouEvent()
}