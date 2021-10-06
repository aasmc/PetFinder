package ru.aasmc.petfinder.animalsnearyou.presentation.animaldetails.model.mappers

import ru.aasmc.petfinder.animalsnearyou.presentation.animaldetails.model.UIAnimalDetailed
import ru.aasmc.petfinder.common.domain.model.animal.AnimalWithDetails
import ru.aasmc.petfinder.common.presentation.model.mappers.UiMapper
import javax.inject.Inject

class UiAnimalDetailsMapper @Inject constructor() : UiMapper<AnimalWithDetails, UIAnimalDetailed> {

    override fun mapToView(input: AnimalWithDetails): UIAnimalDetailed {
        return UIAnimalDetailed(
            id = input.id,
            name = input.name,
            photo = input.media.getFirstSmallestAvailablePhoto(),
            description = input.details.description,
            sprayNeutered = input.details.healthDetails.isSpayedOrNeutered,
            specialNeeds = input.details.healthDetails.hasSpecialNeeds,
            declawed = input.details.healthDetails.isDeclawed,
            shotsCurrent = input.details.healthDetails.shotsAreCurrent,
            tags = input.tags,
            phone = input.details.organization.contact.phone
        )
    }
}