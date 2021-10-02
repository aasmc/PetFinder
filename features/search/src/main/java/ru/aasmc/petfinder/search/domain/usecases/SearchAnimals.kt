package ru.aasmc.petfinder.search.domain.usecases

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import ru.aasmc.petfinder.common.domain.model.search.SearchParameters
import ru.aasmc.petfinder.common.domain.model.search.SearchResults
import ru.aasmc.petfinder.common.domain.repositories.AnimalRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import io.reactivex.functions.Function3

class SearchAnimals @Inject constructor(
    private val animalRepository: AnimalRepository
) {

    /**
     * Joins the latest results of each stream, using the combining function.
     * Every time a stream emits something new, this function creates an updates
     * [SearchParameters] instance.
     */
    private val combiningFunction: Function3<String, String, String, SearchParameters>
        get() = Function3 { query, age, type ->
            SearchParameters(query, age, type)
        }

    /**
     * Returns a flowable that emits new values every time one of
     * the behavior subjects emits something new.
     */
    operator fun invoke(
        querySubject: BehaviorSubject<String>,
        ageSubject: BehaviorSubject<String>,
        typeSubject: BehaviorSubject<String>
    ): Flowable<SearchResults> {

        val query = querySubject
            // no need to react instantly for every change in the query.
            // We wait half a second to retrieve some more info from the user
            .debounce(500L, TimeUnit.MILLISECONDS)
            // remove unnecessary spaces from the user input
            .map { it.trim() }
            // avoid events with a single character or less
            .filter { it.length >= 2 }

        // for filter we need to remove Any (first parameter among the filters)
        val age = ageSubject.replaceUIEmptyValue()
        val type = typeSubject.replaceUIEmptyValue()

        return Observable.combineLatest(query, age, type, combiningFunction)
                // discard any previous event in favour of the new one
            .toFlowable(BackpressureStrategy.LATEST)
            .switchMap { parameters: SearchParameters ->
                animalRepository.searchCachedAnimalsBy(parameters)
            }
    }

    private fun BehaviorSubject<String>.replaceUIEmptyValue() = map {
        if (it == GetSearchFilters.NO_FILTER_SELECTED) "" else it
    }

}




















