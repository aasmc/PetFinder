package ru.aasmc.petfinder.search.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.aasmc.petfinder.common.domain.model.NoMoreAnimalsException
import ru.aasmc.petfinder.common.domain.model.animal.Animal
import ru.aasmc.petfinder.common.domain.model.pagination.Pagination
import ru.aasmc.petfinder.common.presentation.model.mappers.UiAnimalMapper
import ru.aasmc.petfinder.common.utils.DispatchersProvider
import ru.aasmc.petfinder.common.utils.createExceptionHandler
import ru.aasmc.petfinder.search.domain.usecases.GetSearchFilters
import javax.inject.Inject

@HiltViewModel
class SearchFragmentViewModel @Inject constructor(
    private val getSearchFilters: GetSearchFilters,
    private val uiAnimalMapper: UiAnimalMapper,
    private val dispatchersProvider: DispatchersProvider,
    private val compositeDisposable: CompositeDisposable
) : ViewModel() {

    private val _state: MutableLiveData<SearchViewState> = MutableLiveData()
    val state: LiveData<SearchViewState>
        get() = _state

    private val querySubject = BehaviorSubject.create<String>()
    private val ageSubject = BehaviorSubject.createDefault("")
    private val typeSubject = BehaviorSubject.createDefault("")

    private var currentPage = 0

    init {
        _state.value = SearchViewState()
    }

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.PrepareForSearch -> {
                prepareForSearch()
            }
            else -> {
                onSearchParametersUpdate(event)
            }
        }
    }

    private fun onSearchParametersUpdate(event: SearchEvent) {

    }

    private fun prepareForSearch() {
        loadFilterValues()
    }

    private fun loadFilterValues() {
        val exceptionHandler =
            createExceptionHandler(
                message = "Failed to get filter values"
            )

        viewModelScope.launch(exceptionHandler) {
            val (ages, types) = withContext(dispatchersProvider.io()) {
                getSearchFilters()
            }

            updateStateWithFilterValues(ages, types)
        }
    }

    private fun updateStateWithFilterValues(
        ages: List<String>,
        types: List<String>
    ) {
        _state.value = state.value!!.updateToReadyToSearch(
            ages = ages,
            types = types
        )
    }

    private fun createExceptionHandler(message: String): CoroutineExceptionHandler {
        return viewModelScope.createExceptionHandler(message) {
            onFailure(it)
        }
    }

    private fun setSearchingState() {
        _state.value = _state.value!!.updateToSearching()
    }

    private fun setNoSearchQueryState() {
        _state.value = _state.value!!.updateToNoSearchQuery()
    }

    private fun onAnimalList(animals: List<Animal>) {
        _state.value =
            state.value!!.updateToHasSearchResults(animals.map { uiAnimalMapper.mapToView(it) })
    }

    private fun resetPagination() {
        currentPage = 0
    }

    private fun onPaginationInfoObtained(pagination: Pagination) {
        currentPage = pagination.currentPage
    }

    private fun onFailure(throwable: Throwable) {
        _state.value = if (throwable is NoMoreAnimalsException) {
            _state.value!!.updateToNoResultsAvailable()
        } else {
            state.value!!.updateToHasFailure(throwable)
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

}






























