package ru.aasmc.petfinder.common.data.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.aasmc.petfinder.common.data.cache.daos.AnimalsDao
import ru.aasmc.petfinder.common.data.cache.daos.OrganizationsDao
import ru.aasmc.petfinder.common.data.cache.model.cachedanimal.*
import ru.aasmc.petfinder.common.data.cache.model.cachedorganization.CachedOrganization

@Database(
    entities = [
        CachedPhoto::class,
        CachedVideo::class,
        CachedTag::class,
        CachedAnimalTagCrossRef::class,
        CachedOrganization::class,
        CachedAnimalWithDetails::class
    ],
    version = 1
)
abstract class PetFinderDatabase : RoomDatabase() {
    abstract fun organizationsDao(): OrganizationsDao

    abstract fun animalsDao(): AnimalsDao
}