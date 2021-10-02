package ru.aasmc.petfinder.onboarding.presentation

import androidx.annotation.StringRes
import ru.aasmc.petfinder.R

data class OnboardingViewState(
    val postCode: String = "",
    val distance: String = "",
    @StringRes val postcodeError: Int = R.string.no_error,
    @StringRes val distanceError: Int = R.string.no_error
) {
    val submitButtonActive: Boolean
        get() {
            return postCode.isNotEmpty() &&
                    distance.isNotEmpty() &&
                    postcodeError == R.string.no_error &&
                    distanceError == R.string.no_error
        }
}