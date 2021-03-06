package ru.aasmc.petfinder.search.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.*
import ru.aasmc.logging.Logger
import ru.aasmc.petfinder.common.domain.model.NoMoreAnimalsException
import ru.aasmc.petfinder.common.domain.model.animal.Animal
import ru.aasmc.petfinder.common.domain.model.pagination.Pagination
import ru.aasmc.petfinder.common.domain.model.search.SearchParameters
import ru.aasmc.petfinder.common.domain.model.search.SearchResults
import ru.aasmc.petfinder.common.presentation.model.mappers.UiAnimalMapper
import ru.aasmc.petfinder.common.utils.DispatchersProvider
import ru.aasmc.petfinder.common.utils.createExceptionHandler
import ru.aasmc.petfinder.search.domain.usecases.GetSearchFilters
import ru.aasmc.petfinder.search.domain.usecases.SearchAnimals
import ru.aasmc.petfinder.search.domain.usecases.SearchAnimalsRemotely
import javax.inject.Inject

@HiltViewModel
class SearchFragmentViewModel @Inject constructor(
    private val searchAnimals: SearchAnimals,
    private val searchAnimalsRemotely: SearchAnimalsRemotely,
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

    /**
     * Job that tracks a remote search and cancels it, if a new one is attempted while
     * the old one is in progress. We cancel it when we change any of the search parameters.
     */
    private var remoteSearchJob: Job = Job()
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
        remoteSearchJob.cancel(
            CancellationException("New search parameters incoming!")
        )
        when (event) {
            is SearchEvent.QueryInput -> updateQuery(event.input)
            is SearchEvent.AgeValueSelected -> updateAgeValue(event.age)
            is SearchEvent.TypeValueSelected -> updateTypeValue(event.type)
        }
    }

    private fun updateTypeValue(type: String) {
        typeSubject.onNext(type)
    }

    private fun updateAgeValue(age: String) {
        ageSubject.onNext(age)
    }

    private fun updateQuery(input: String) {
        resetPagination()

        querySubject.onNext(input)

        if (input.isEmpty()) {
            setNoSearchQueryState()
        } else {
            setSearchingState()
        }
    }


    private fun prepareForSearch() {
        loadFilterValues()
        setupSearchSubscription()
    }

    private fun setupSearchSubscription() {
        searchAnimals(querySubject, ageSubject, typeSubject)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { onSearchResults(it) },
                { onFailure(it) }
            )
            .addTo(compositeDisposable)
    }

    private fun onSearchResults(searchResults: SearchResults) {
        val (animals, searchParameters) = searchResults

        if (animals.isEmpty()) {
            // search remotely
            onEmptyCacheResults(searchParameters)
        } else {
            onAnimalList(animals)
        }
    }

    private fun onEmptyCacheResults(searchParameters: SearchParameters) {
        // update the state to searching remotely, which shows a [ProgressBar]
        // and a warning message
        _state.value = state.value!!.updateToSearchingRemotely()
        searchRemotely(searchParameters)
    }

    /**
     * This is a one-shot operation, as any network operation should be. We have the
     * search results Flowable up and running. This operation will store any
     * results in the database, triggering the Flowable to display them.
     */
    private fun searchRemotely(searchParameters: SearchParameters) {
        val exceptionHandler = createExceptionHandler(message = "Failed to search remotely")

        remoteSearchJob = viewModelScope.launch(exceptionHandler) {
            val pagination = withContext(dispatchersProvider.io()) {
                Logger.d("Searching remotely...")

                searchAnimalsRemotely(++currentPage, searchParameters)
            }

            onPaginationInfoObtained(pagination)
        }

        // here we verify that we are indeed cancelling the coroutine on a new search
        // Cancelling a coroutine is successful because Retrofit supports coroutine cancellation.
        // If we used some other client that doesn't check for coroutine cancellation, then
        // no log would be printed because coroutines are cancelled cooperatively (i.e. they
        // need to check for cancellation).
        remoteSearchJob.invokeOnCompletion { it?.printStackTrace() }
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






























