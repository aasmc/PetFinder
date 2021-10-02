package ru.aasmc.petfinder.main.domain.usecases

import ru.aasmc.petfinder.common.domain.repositories.AnimalRepository
import javax.inject.Inject

class OnboardingIsComplete @Inject constructor(
    private val repository: AnimalRepository
) {
    suspend operator fun invoke() = repository.onboardingIsComplete()
}