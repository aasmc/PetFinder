package ru.aasmc.petfinder.common.data.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.aasmc.petfinder.common.data.cache.Cache
import ru.aasmc.petfinder.common.data.cache.PetFinderDatabase
import ru.aasmc.petfinder.common.data.cache.RoomCache
import ru.aasmc.petfinder.common.data.cache.daos.AnimalsDao
import ru.aasmc.petfinder.common.data.cache.daos.OrganizationsDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CacheModule {

    /**
     * Since we use @Binds, we need to make CacheModule class abstract.
     */
    @Binds
    abstract fun bindCache(cache: RoomCache): Cache

    companion object {

        @Singleton
        @Provides
        fun provideDatabase(@ApplicationContext context: Context): PetFinderDatabase {
            return Room.databaseBuilder(context, PetFinderDatabase::class.java, "petfinder.db")
                .build()
        }

        @Provides
        fun provideOrganizationsDao(petSaveDatabase: PetFinderDatabase): OrganizationsDao =
            petSaveDatabase.organizationsDao()

        @Provides
        fun provideAnimalsDao(petSaveDatabase: PetFinderDatabase): AnimalsDao =
            petSaveDatabase.animalsDao()
    }
}