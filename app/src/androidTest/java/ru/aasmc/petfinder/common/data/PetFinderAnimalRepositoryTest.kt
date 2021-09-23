package ru.aasmc.petfinder.common.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.Instant
import retrofit2.Retrofit
import ru.aasmc.petfinder.common.data.api.PetFinderApi
import ru.aasmc.petfinder.common.data.api.model.mappers.ApiAnimalMapper
import ru.aasmc.petfinder.common.data.api.model.mappers.ApiPaginationMapper
import ru.aasmc.petfinder.common.data.api.utils.FakeServer
import ru.aasmc.petfinder.common.data.cache.Cache
import ru.aasmc.petfinder.common.data.cache.PetFinderDatabase
import ru.aasmc.petfinder.common.data.cache.RoomCache
import ru.aasmc.petfinder.common.data.di.CacheModule
import ru.aasmc.petfinder.common.data.di.PreferencesModule
import ru.aasmc.petfinder.common.data.preferences.FakePreferences
import ru.aasmc.petfinder.common.data.preferences.Preferences
import ru.aasmc.petfinder.common.domain.repositories.AnimalRepository
import javax.inject.Inject

@HiltAndroidTest // Hilt will know it has to inject some dependencies here
@UninstallModules(
    PreferencesModule::class,
    CacheModule::class
) // Tell Hilt not to load original Preferences dependency
class PetFinderAnimalRepositoryTest {

    private val fakeServer = FakeServer()
    private lateinit var repository: AnimalRepository
    private lateinit var api: PetFinderApi

    /**
     * Hilt rule that tells Hilt when to inject the dependencies.
     * It gives a leeway to handle any configuration we might need before the injection.
     */
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    /**
     * Swaps the background executor used by Architecture Components with the one
     * that is synchronous. It allows Room to execute all its operations instantly.
     */
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    lateinit var cache: Cache

    @Inject
    lateinit var petFinderDatabase: PetFinderDatabase

    @Inject
    lateinit var retrofitBuilder: Retrofit.Builder

    @Inject
    lateinit var apiAnimalMapper: ApiAnimalMapper

    @Inject
    lateinit var apiPaginationMapper: ApiPaginationMapper

    @BindValue // handles the replacement and injection
    @JvmField // need to add this annotation due to Hilt limitations. Possibly will be excluded in future
    val preferences: Preferences = FakePreferences()

    @Module
    @InstallIn(SingletonComponent::class)
    object TestCacheModule {

        @Provides
        fun provideRoomDatabase(): PetFinderDatabase {
            return Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().context,
                PetFinderDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()
        }
    }

    @Before
    fun setup() {
        fakeServer.start()

        preferences.deleteTokenInfo()
        preferences.putToken("validToken")
        preferences.putTokenExpirationTime(Instant.now().plusSeconds(3600).epochSecond)
        preferences.putTokenType("Bearer")

        // after configuring all the dependencies we tell Hilt to inject them all
        hiltRule.inject()

        api = retrofitBuilder
            .baseUrl(fakeServer.baseEndpoint)
            .build()
            .create(PetFinderApi::class.java)

        cache = RoomCache(petFinderDatabase.animalsDao(), petFinderDatabase.organizationsDao())

        repository = PetFinderAnimalRepository(
            api,
            cache,
            apiAnimalMapper,
            apiPaginationMapper
        )
    }

    @After
    fun teardown() {
        fakeServer.shutdown()
    }

    @Test
    fun requestMoreAnimals_success() = runBlocking {
        // given
        val expectedAnimalId = 124L
        fakeServer.setHappyPathDispatcher()
        // when
        val paginatedAnimals = repository.requestMoreAnimals(1, 100)

        // then
        val animal = paginatedAnimals.animals.first()
        assertEquals(animal.id, expectedAnimalId)
    }

    @Test
    fun insertAnimals_success() {
        // given
        val expectedAnimalId = 124L

        runBlocking {
            fakeServer.setHappyPathDispatcher()

            val paginatedAnimals = repository.requestMoreAnimals(1, 100)
            val animal = paginatedAnimals.animals.first()

            // when
            repository.storeAnimals(listOf(animal))
        }
        // then
        // calling test() on a Flowable object returns a special testObserver
        val testObserver = repository.getAnimals().test()

        testObserver.assertNoErrors()
        testObserver.assertNotComplete()
        testObserver.assertValue { it.first().id == expectedAnimalId }
    }
}

















