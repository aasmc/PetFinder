package ru.aasmc.petfinder.common.data

import io.reactivex.Flowable
import ru.aasmc.petfinder.common.data.api.PetFinderApi
import ru.aasmc.petfinder.common.data.api.model.mappers.ApiAnimalMapper
import ru.aasmc.petfinder.common.data.api.model.mappers.ApiPaginationMapper
import ru.aasmc.petfinder.common.data.cache.Cache
import ru.aasmc.petfinder.common.data.cache.model.cachedanimal.CachedAnimalAggregate
import ru.aasmc.petfinder.common.data.cache.model.cachedorganization.CachedOrganization
import ru.aasmc.petfinder.common.domain.model.animal.Animal
import ru.aasmc.petfinder.common.domain.model.animal.AnimalWithDetails
import ru.aasmc.petfinder.common.domain.model.pagination.PaginatedAnimals
import ru.aasmc.petfinder.common.domain.repositories.AnimalRepository
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
    }

    override suspend fun storeAnimals(animals: List<AnimalWithDetails>) {
        // organizations have a 1-to-many relations with animals, so we need to insert them first
        // in order for Room not to complain about foreign keys being invalid (since we have the
        // organizationId as a foreign key in the animals table
        val organizations = animals.map { CachedOrganization.fromDomain(it.details.organization) }

        cache.storeOrganizations(organizations)
        cache.storeNearbyAnimals(animals.map { CachedAnimalAggregate.fromDomain(it) })
    }

}


















