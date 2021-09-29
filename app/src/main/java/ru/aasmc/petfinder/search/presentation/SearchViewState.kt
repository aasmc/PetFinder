package ru.aasmc.petfinder.search.presentation

import ru.aasmc.petfinder.common.presentation.Event
import ru.aasmc.petfinder.common.presentation.model.UIAnimal

data class SearchViewState(
    val noSearchQuery: Boolean = true,
    val searchResults: List<UIAnimal> = emptyList(),
    // filter values are used to populate ArrayAdapter of the AutoCompleteTextView
    // since we know that the values will not change, we only need them once, but the class is
    // immutable, so it will be recreated on every update of the state. Therefore we wrap the
    // values in the Event class to get them once if they were not handled. It prevents us from
    // creating the ArrayAdapter multiple times.
    val ageFilterValues: Event<List<String>> = Event(emptyList()),
    val typeFilterValues: Event<List<String>> = Event(emptyList()),
    val searchingRemotely: Boolean = false,
    val noRemoteResults: Boolean = false,
    val failure: Event<Throwable>? = null
) {

    fun updateToReadyToSearch(ages: List<String>, types: List<String>): SearchViewState {
        return copy(
            ageFilterValues = Event(ages),
            typeFilterValues = Event(types)
        )
    }

    fun updateToNoSearchQuery(): SearchViewState {
        return copy(
            noSearchQuery = true,
            searchResults = emptyList(),
            noRemoteResults = false
        )
    }

    fun updateToSearching(): SearchViewState {
        return copy(
            noSearchQuery = false,
            searchingRemotely = false,
            noRemoteResults = false
        )
    }

    fun updateToSearchingRemotely(): SearchViewState {
        return copy(
            searchingRemotely = true,
            searchResults = emptyList()
        )
    }

    fun updateToHasSearchResults(animals: List<UIAnimal>): SearchViewState {
        return copy(
            noSearchQuery = false,
            searchResults = animals,
            searchingRemotely = false,
            noRemoteResults = false
        )
    }

    fun updateToNoResultsAvailable(): SearchViewState {
        return copy(
            searchingRemotely = false,
            noRemoteResults = true
        )
    }

    fun updateToHasFailure(throwable: Throwable): SearchViewState {
        return copy(
            failure = Event(throwable)
        )
    }

}
























