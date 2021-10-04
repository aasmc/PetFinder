package ru.aasmc.petfinder.common.data.cache

import io.reactivex.Flowable
import io.reactivex.Single
import ru.aasmc.petfinder.common.data.cache.daos.AnimalsDao
import ru.aasmc.petfinder.common.data.cache.daos.OrganizationsDao
import ru.aasmc.petfinder.common.data.cache.model.cachedanimal.CachedAnimalAggregate
import ru.aasmc.petfinder.common.data.cache.model.cachedorganization.CachedOrganization
import javax.inject.Inject

class RoomCache @Inject constructor(
    private val animalsDao: AnimalsDao,
    private val organizationsDao: OrganizationsDao
) : Cache {

    override fun storeOrganizations(organizations: List<CachedOrganization>) {
        organizationsDao.insert(organizations)
    }

    override fun getOrganization(organizationId: String): Single<CachedOrganization> {
        return organizationsDao.getOrganization(organizationId)
    }

    override fun getNearbyAnimals(): Flowable<List<CachedAnimalAggregate>> {
        return animalsDao.getAllAnimals()
    }

    override fun storeNearbyAnimals(animals: List<CachedAnimalAggregate>) {
        animalsDao.insertAnimalsWithDetails(animals)
    }

    override fun getAnimal(animalId: Long): Single<CachedAnimalAggregate> {
        return animalsDao.getAnimal(animalId)
    }

    override suspend fun getAllTypes(): List<String> {
        return animalsDao.getAllTypes()
    }

    override fun searchAnimalsBy(
        name: String,
        age: String,
        type: String
    ): Flowable<List<CachedAnimalAggregate>> {
        return animalsDao.searchAnimalsBy(name, age, type)
    }
}