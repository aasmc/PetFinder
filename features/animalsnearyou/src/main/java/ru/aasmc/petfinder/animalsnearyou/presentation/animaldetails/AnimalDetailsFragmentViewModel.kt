package ru.aasmc.petfinder.animalsnearyou.presentation.animaldetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import ru.aasmc.petfinder.animalsnearyou.presentation.animaldetails.model.mappers.UiAnimalDetailsMapper
import ru.aasmc.petfinder.common.domain.model.animal.AnimalWithDetails
import ru.aasmc.petfinder.common.domain.usecases.GetAnimalDetails
import javax.inject.Inject

@HiltViewModel
class AnimalDetailsFragmentViewModel @Inject constructor(
    private val uiAnimalDetailsMapper: UiAnimalDetailsMapper,
    private val getAnimalDetails: GetAnimalDetails,
    private val compositeDisposable: CompositeDisposable
) : ViewModel() {

    private val _state = MutableLiveData<AnimalDetailsViewState>()
    val state: LiveData<AnimalDetailsViewState>
        get() = _state

    init {
        _state.value = AnimalDetailsViewState.Loading
    }

    fun handleEvent(event: AnimalDetailsEvent) {
        when (event) {
            is AnimalDetailsEvent.LoadAnimalDetails -> subscribeToAnimalDetails(event.animalId)
        }
    }

    private fun subscribeToAnimalDetails(animalId: Long) {
        getAnimalDetails(animalId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { onAnimalDetails(it) },
                { onFailure(it) }
            )
            .addTo(compositeDisposable)

    }

    private fun onAnimalDetails(animal: AnimalWithDetails) {
        val animalDetails = uiAnimalDetailsMapper.mapToView(animal)
        _state.value = AnimalDetailsViewState.AnimalDetails(animalDetails)
    }

    private fun onFailure(throwable: Throwable) {
        _state.value = AnimalDetailsViewState.Failure
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}