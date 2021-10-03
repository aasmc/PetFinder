package ru.aasmc.petfinder.onboarding.domain.usecases

import ru.aasmc.petfinder.common.domain.repositories.AnimalRepository
import javax.inject.Inject

class StoreOnboardingData @Inject constructor(
    private val repository: AnimalRepository
) {
    suspend operator fun invoke(postcode: String, distance: String) {
        repository.storeOnBoardingData(postcode, distance.toInt())
    }
}