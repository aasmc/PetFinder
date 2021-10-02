package ru.aasmc.common.data.di

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.aasmc.petfinder.common.data.cache.Cache
import ru.aasmc.petfinder.common.data.cache.PetFinderDatabase
import ru.aasmc.petfinder.common.data.cache.RoomCache
import ru.aasmc.petfinder.common.data.cache.daos.AnimalsDao
import ru.aasmc.petfinder.common.data.cache.daos.OrganizationsDao
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class TestCacheModule {

    @Binds
    abstract fun bindCache(cache: RoomCache): Cache

    companion object {

        @Provides
        @Singleton
        fun provideRoomDatabase(): PetFinderDatabase {
            return Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().context,
                PetFinderDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()
        }

        @Provides
        fun provideAnimalsDao(
            petSaveDatabase: PetFinderDatabase
        ): AnimalsDao = petSaveDatabase.animalsDao()

        @Provides
        fun provideOrganizationsDao(petSaveDatabase: PetFinderDatabase): OrganizationsDao =
            petSaveDatabase.organizationsDao()
    }
}