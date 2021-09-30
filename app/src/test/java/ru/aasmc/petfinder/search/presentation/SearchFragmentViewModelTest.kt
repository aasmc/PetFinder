package ru.aasmc.petfinder.search.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.reactivex.disposables.CompositeDisposable
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ru.aasmc.petfinder.RxImmediateSchedulerRule
import ru.aasmc.petfinder.TestCoroutineRule
import ru.aasmc.petfinder.common.data.FakeRepository
import ru.aasmc.petfinder.common.presentation.Event
import ru.aasmc.petfinder.common.presentation.model.mappers.UiAnimalMapper
import ru.aasmc.petfinder.common.utils.DispatchersProvider
import ru.aasmc.petfinder.search.domain.usecases.GetSearchFilters
import ru.aasmc.petfinder.search.domain.usecases.SearchAnimals
import ru.aasmc.petfinder.search.domain.usecases.SearchAnimalsRemotely

@ExperimentalCoroutinesApi
class SearchFragmentViewModelTest {

    /**
     * Overrides the main [Looper] that we need to test code with [LiveData].
     */
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    /**
     * Set RxJava Schedulers to execute immediately.
     */
    @get:Rule
    val rxImmediateSchedulerRule = RxImmediateSchedulerRule()

    private lateinit var viewModel: SearchFragmentViewModel
    private lateinit var repository: FakeRepository
    private lateinit var getSearchFilters: GetSearchFilters
    private val uiAnimalsMapper = UiAnimalMapper()

    @Before
    fun setup() {
        val dispatchersProvider = object : DispatchersProvider {
            override fun io(): CoroutineDispatcher {
                return Dispatchers.Main
            }
        }

        repository = FakeRepository()
        getSearchFilters = GetSearchFilters(repository)
        viewModel = SearchFragmentViewModel(
            SearchAnimals(repository),
            SearchAnimalsRemotely(repository),
            getSearchFilters,
            uiAnimalsMapper,
            dispatchersProvider,
            CompositeDisposable()
        )
    }

    @Test
    fun `SearchFragmentViewModel remote search with success`() = testCoroutineRule.runBlockingTest {
        // given
        val (name, age, type) = repository.remotelySearchableAnimal
        val (ages, types) = getSearchFilters()

        val expectedRemoteAnimals = repository.remoteAnimals.map { uiAnimalsMapper.mapToView(it) }

        viewModel.state.observeForever { }

        val expectedViewState = SearchViewState(
            noSearchQuery = false,
            searchResults = expectedRemoteAnimals,
            ageFilterValues = Event(ages),
            typeFilterValues = Event(types),
            searchingRemotely = false,
            noRemoteResults = false
        )
        // when
        viewModel.onEvent(SearchEvent.PrepareForSearch)
        viewModel.onEvent(SearchEvent.TypeValueSelected(type))
        viewModel.onEvent(SearchEvent.AgeValueSelected(age))
        viewModel.onEvent(SearchEvent.QueryInput(name))

        // then

        val viewState = viewModel.state.value!!

        assertThat(viewState).isEqualTo(expectedViewState)
    }
}






























