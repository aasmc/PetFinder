package ru.aasmc.petfinder.main.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.aasmc.petfinder.R
import ru.aasmc.petfinder.common.utils.DispatchersProvider
import ru.aasmc.petfinder.common.utils.createExceptionHandler
import ru.aasmc.petfinder.main.domain.usecases.OnboardingIsComplete
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val onboardingIsComplete: OnboardingIsComplete,
    private val dispatchersProvider: DispatchersProvider
) : ViewModel() {
    private val _viewEffect = MutableSharedFlow<MainActivityViewEffect>()
    val viewEffect: SharedFlow<MainActivityViewEffect> get() = _viewEffect

    fun onEvent(event: MainActivityEvent) {
        when (event) {
            is MainActivityEvent.DefineStartDestination -> defineStartDestination()
        }
    }

    private fun defineStartDestination() {
        val errorMessage = "Failed to check if onboarding is complete"
        val exceptionHandler = viewModelScope.createExceptionHandler(errorMessage) {
            onFailure(it)
        }

        viewModelScope.launch(exceptionHandler) {
            val destination = withContext(dispatchersProvider.io()) {
                if (onboardingIsComplete()) {
                    R.id.nav_animalsnearyou
                } else {
                    R.id.onboardingFragment
                }
            }

            _viewEffect.emit(MainActivityViewEffect.SetStartDestination(destination))
        }
    }

    private fun onFailure(throwable: Throwable) {
        // todo handle failures
    }
}

















