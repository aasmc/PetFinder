package ru.aasmc.petfinder.common.domain.model.animal

import org.threeten.bp.LocalDateTime
import ru.aasmc.petfinder.common.domain.model.animal.details.Details
import ru.aasmc.petfinder.common.domain.model.organization.Organization

data class AnimalWithDetails(
    val id: Long,
    val name: String,
    val type: String,
    val details: Details,
    val media: Media,
    val tags: List<String>,
    val adoptionStatus: AdoptionStatus,
    val publishedAt: LocalDateTime
) {
    val description: String = details.description
    val organizationContact: Organization.Contact = details.organizationContact
}