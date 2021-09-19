package ru.aasmc.petfinder.common.domain.model.animal.details

data class HealthDetails(
    val isSpayedOrNeutered: Boolean,
    val isDeclawed: Boolean,
    val hasSpecialNeeds: Boolean,
    val shotsAreCurrent: Boolean
)