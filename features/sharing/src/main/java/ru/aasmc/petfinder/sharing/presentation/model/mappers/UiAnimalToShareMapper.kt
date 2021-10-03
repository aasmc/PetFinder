package ru.aasmc.petfinder.sharing.presentation.model.mappers

import ru.aasmc.petfinder.common.domain.model.animal.AnimalWithDetails
import ru.aasmc.petfinder.common.presentation.model.mappers.UiMapper
import ru.aasmc.petfinder.sharing.presentation.model.UIAnimalToShare
import javax.inject.Inject

class UiAnimalToShareMapper @Inject constructor(): UiMapper<AnimalWithDetails, UIAnimalToShare> {

    override fun mapToView(input: AnimalWithDetails): UIAnimalToShare {
        val message = createMessage(input)

        return UIAnimalToShare(input.media.getFirstSmallestAvailablePhoto(), message)
    }

    private fun createMessage(input: AnimalWithDetails): String {
        val contact = input.organizationContact
        val formattedAddress = contact.formattedAddress
        val formattedContactInfo = contact.formattedContactInfo

        return "${input.description}\n\nOrganization info:\n$formattedAddress\n\n$formattedContactInfo"
    }
}
