package ru.aasmc.petfinder.sharing.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.aasmc.petfinder.common.domain.usecases.GetAnimalDetails
import ru.aasmc.petfinder.common.utils.DispatchersProvider
import ru.aasmc.petfinder.sharing.presentation.model.mappers.UiAnimalToShareMapper
import javax.inject.Inject

@HiltViewModel
class SharingFragmentViewModel @Inject constructor(
    private val getAnimalDetails: GetAnimalDetails,
    private val uiAnimalToShareMapper: UiAnimalToShareMapper,
    private val dispatchersProvider: DispatchersProvider
) : ViewModel() {

    private val _viewState = MutableStateFlow(SharingViewState())
    val viewState: StateFlow<SharingViewState>
        get() = _viewState

    fun onEvent(event: SharingEvent) {
        when (event) {
            is SharingEvent.GetAnimalToShare -> getAnimalToShare(event.animalId)
        }
    }

    private fun getAnimalToShare(animalId: Long) {
        viewModelScope.launch {
            val animal = withContext(dispatchersProvider.io()) {
                getAnimalDetails(animalId)
            }

            _viewState.value = viewState.value.copy(
                animalToShare = uiAnimalToShareMapper.mapToView(animal)
            )
        }
    }
}