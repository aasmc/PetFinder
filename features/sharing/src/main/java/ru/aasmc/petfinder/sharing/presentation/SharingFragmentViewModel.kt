package ru.aasmc.petfinder.sharing.presentation

import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.aasmc.petfinder.common.domain.usecases.GetAnimalDetails
import ru.aasmc.petfinder.sharing.presentation.model.mappers.UiAnimalToShareMapper
import javax.inject.Inject

class SharingFragmentViewModel @Inject constructor(
    private val getAnimalDetails: GetAnimalDetails,
    private val uiAnimalToShareMapper: UiAnimalToShareMapper,
    private val compositeDisposable: CompositeDisposable
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
        getAnimalDetails(animalId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    _viewState.value = viewState.value.copy(
                        animalToShare = uiAnimalToShareMapper.mapToView(it)
                    )
                },
                {
                    // ignore throwable
                }
            ).addTo(compositeDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}