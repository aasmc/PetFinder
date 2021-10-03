package ru.aasmc.petfinder.onboarding.presentation

sealed class OnboardingViewEffect {
    object NavigateToAnimalsNearYou: OnboardingViewEffect()
}