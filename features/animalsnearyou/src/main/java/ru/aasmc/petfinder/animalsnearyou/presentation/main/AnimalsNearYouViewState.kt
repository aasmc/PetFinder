package ru.aasmc.petfinder.animalsnearyou.presentation.main

import ru.aasmc.petfinder.common.presentation.Event
import ru.aasmc.petfinder.common.presentation.model.UIAnimal


data class AnimalsNearYouViewState(
    val loading: Boolean = true,
    val animals: List<UIAnimal> = emptyList(),
    val noMoreAnimalsNearby: Boolean = false,
    val failure: Event<Throwable>? = null
)