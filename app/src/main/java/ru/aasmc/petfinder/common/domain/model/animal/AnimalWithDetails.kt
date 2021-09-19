package ru.aasmc.petfinder.common.domain.model.animal

import org.threeten.bp.LocalDateTime
import ru.aasmc.petfinder.common.domain.model.animal.details.Details

data class AnimalWithDetails(
    val id: Long,
    val name: String,
    val type: String,
    val details: Details,
    val media: Media,
    val tags: List<String>,
    val adoptionStatus: AdoptionStatus,
    val publishedApi: LocalDateTime
)