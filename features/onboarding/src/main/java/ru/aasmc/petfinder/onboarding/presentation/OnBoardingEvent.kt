package ru.aasmc.petfinder.onboarding.presentation

sealed class OnBoardingEvent {
    data class PostCodeChanged(val newPostCode: String): OnBoardingEvent()
    data class DistanceChanged(val newDistance: String): OnBoardingEvent()
    object SubmitButtonClicked: OnBoardingEvent()
}