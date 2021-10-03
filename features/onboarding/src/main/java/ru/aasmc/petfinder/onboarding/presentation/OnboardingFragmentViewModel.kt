package ru.aasmc.petfinder.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.aasmc.petfinder.common.utils.DispatchersProvider
import ru.aasmc.petfinder.common.utils.createExceptionHandler
import ru.aasmc.petfinder.onboarding.R
import ru.aasmc.petfinder.onboarding.domain.usecases.StoreOnboardingData
import javax.inject.Inject

@HiltViewModel
class OnboardingFragmentViewModel @Inject constructor(
    private val storeOnboardingData: StoreOnboardingData,
    private val dispatchersProvider: DispatchersProvider
) : ViewModel() {

    companion object {
        private const val MAX_POST_CODE_LENGTH = 5
    }

    private val _viewState = MutableStateFlow(OnboardingViewState())
    val viewState: StateFlow<OnboardingViewState>
        get() = _viewState

    private val _viewEffects = MutableSharedFlow<OnboardingViewEffect>()
    val viewEffects: SharedFlow<OnboardingViewEffect>
        get() = _viewEffects

    fun onEvent(event: OnBoardingEvent) {
        when (event) {
            is OnBoardingEvent.PostCodeChanged -> validateNewPostcodeValue(event.newPostCode)
            is OnBoardingEvent.DistanceChanged -> validateNewDistanceValue(event.newDistance)
            is OnBoardingEvent.SubmitButtonClicked -> wrapUpOnboarding()
        }
    }

    private fun validateNewPostcodeValue(newPostcode: String) {
        val validPostcode = newPostcode.length == MAX_POST_CODE_LENGTH
        val postcodeError = if (validPostcode || newPostcode.isEmpty()) {
            R.string.no_error
        } else {
            R.string.postcode_error
        }

        _viewState.value = viewState.value.copy(
            postCode = newPostcode,
            postcodeError = postcodeError
        )
    }

    private fun validateNewDistanceValue(newDistance: String) {
        val distanceError = when {
            newDistance.isNotEmpty() && newDistance.toInt() > 500 -> {
                R.string.distance_error
            }
            newDistance.toInt() == 0 -> {
                R.string.distance_error_cannot_be_zero
            }
            else -> R.string.no_error
        }

        _viewState.value = viewState.value.copy(
            distance = newDistance,
            distanceError = distanceError
        )
    }

    private fun wrapUpOnboarding() {
        val errorMessage = "Failed to store onboarding data"
        val exceptionHandler = viewModelScope.createExceptionHandler(errorMessage) {
            onFailure(it)
        }

        val (postcode, distance) = viewState.value
        viewModelScope.launch(exceptionHandler) {
            withContext(dispatchersProvider.io()) {
                storeOnboardingData(postcode, distance)
            }
            _viewEffects.emit(OnboardingViewEffect.NavigateToAnimalsNearYou)
        }
    }

    private fun onFailure(throwable: Throwable) {
        // todo handle failures
    }
}

























