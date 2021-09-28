package ru.aasmc.petfinder.common.data

import io.reactivex.Flowable
import retrofit2.HttpException
import ru.aasmc.petfinder.common.data.api.PetFinderApi
import ru.aasmc.petfinder.common.data.api.model.mappers.ApiAnimalMapper
import ru.aasmc.petfinder.common.data.api.model.mappers.ApiPaginationMapper
import ru.aasmc.petfinder.common.data.cache.Cache
import ru.aasmc.petfinder.common.data.cache.model.cachedanimal.CachedAnimalAggregate
import ru.aasmc.petfinder.common.data.cache.model.cachedorganization.CachedOrganization
import ru.aasmc.petfinder.common.domain.model.NetworkException
import ru.aasmc.petfinder.common.domain.model.animal.Animal
import ru.aasmc.petfinder.common.domain.model.animal.AnimalWithDetails
import ru.aasmc.petfinder.common.domain.model.animal.details.Age
import ru.aasmc.petfinder.common.domain.model.pagination.PaginatedAnimals
import ru.aasmc.petfinder.common.domain.repositories.AnimalRepository
import ru.aasmc.petfinder.search.domain.model.SearchParameters
import ru.aasmc.petfinder.search.domain.model.SearchResults
import javax.inject.Inject

class PetFinderAnimalRepository @Inject constructor(
    private val api: PetFinderApi,
    private val cache: Cache,
    private val apiAnimalMapper: ApiAnimalMapper,
    private val apiPaginationMapper: ApiPaginationMapper
) : AnimalRepository {

    private val postcode = "07097"
    private val maxDistanceMiles = 100

    override fun getAnimals(): Flowable<List<Animal>> {
        return cache.getNearbyAnimals()
            // ensures that events only with new information get to the subscriber
            .distinctUntilChanged()
            .map { animalsList ->
                animalsList.map {
                    it.animal.toAnimalDomain(
                        it.photos,
                        it.videos,
                        it.tags
                    )
                }
            }
    }

    override suspend fun requestMoreAnimals(pageToLoad: Int, numberOfItems: Int): PaginatedAnimals {

        try {
            val (apiAnimals, apiPagination) = api.getNearbyAnimals(
                pageToLoad = pageToLoad,
                pageSize = numberOfItems,
                postcode = postcode,
                maxDistance = maxDistanceMiles
            )

            return PaginatedAnimals(
                apiAnimals?.map { apiAnimalMapper.mapToDomain(it) }.orEmpty(),
                apiPaginationMapper.mapToDomain(apiPagination)
            )
        } catch (exception: HttpException) {
            throw NetworkException(exception.message ?: "Code ${exception.code()}")
        }
    }

    override suspend fun storeAnimals(animals: List<AnimalWithDetails>) {
        // organizations have a 1-to-many relations with animals, so we need to insert them first
        // in order for Room not to complain about foreign keys being invalid (since we have the
        // organizationId as a foreign key in the animals table
        val organizations = animals.map { CachedOrganization.fromDomain(it.details.organization) }

        cache.storeOrganizations(organizations)
        cache.storeNearbyAnimals(animals.map { CachedAnimalAggregate.fromDomain(it) })
    }

    override suspend fun getAnimalTypes(): List<String> {
        return cache.getAllTypes()
    }

    override fun getAnimalAges(): List<Age> {
        return Age.values().toList()
    }

    override fun searchCachedAnimalsBy(searchParameters: SearchParameters): Flowable<SearchResults> {
        val (name, age, type) = searchParameters

        return cache.searchAnimalsBy(name, age, type)
            .distinctUntilChanged()
            .map { animalsList ->
                animalsList.map { it.animal.toAnimalDomain(it.photos, it.videos, it.tags) }
            }
            .map { SearchResults(it, searchParameters) }
    }

    override suspend fun searchAnimalsRemotely(
        pageToLoad: Int,
        searchParameters: SearchParameters,
        numberOfItems: Int
    ): PaginatedAnimals {
        val (apiAnimals, apiPagination) = api.searchAnimalsBy(
            name = searchParameters.name,
            age = searchParameters.age,
            type = searchParameters.type,
            pageToLoad = pageToLoad,
            pageSize = numberOfItems,
            postCode = postcode,
            maxDistance = maxDistanceMiles
        )

        return PaginatedAnimals(
            apiAnimals?.map { apiAnimalMapper.mapToDomain(it) }.orEmpty(),
            apiPaginationMapper.mapToDomain(apiPagination)
        )
    }

}


















