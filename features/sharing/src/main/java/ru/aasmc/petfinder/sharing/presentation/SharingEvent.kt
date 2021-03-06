package ru.aasmc.petfinder.sharing.presentation

sealed class SharingEvent {
    data class GetAnimalToShare(val animalId: Long): SharingEvent()
}