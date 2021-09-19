package ru.aasmc.petfinder.common.data.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.aasmc.petfinder.common.data.cache.daos.OrganizationsDao
import ru.aasmc.petfinder.common.data.cache.model.cachedanimal.CachedAnimalTagCrossRef
import ru.aasmc.petfinder.common.data.cache.model.cachedanimal.CachedPhoto
import ru.aasmc.petfinder.common.data.cache.model.cachedanimal.CachedTag
import ru.aasmc.petfinder.common.data.cache.model.cachedanimal.CachedVideo
import ru.aasmc.petfinder.common.data.cache.model.cachedorganization.CachedOrganization

@Database(
    entities = [
        CachedPhoto::class,
        CachedVideo::class,
        CachedTag::class,
        CachedAnimalTagCrossRef::class,
        CachedOrganization::class
    ],
    version = 1
)
abstract class PetFinderDatabase : RoomDatabase() {
    abstract fun organizationsDao(): OrganizationsDao
}