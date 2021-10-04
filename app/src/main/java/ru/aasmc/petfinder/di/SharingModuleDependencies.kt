package ru.aasmc.petfinder.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.aasmc.petfinder.common.data.api.PetFinderApi
import ru.aasmc.petfinder.common.data.cache.Cache
import ru.aasmc.petfinder.common.data.preferences.Preferences

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SharingModuleDependencies {
    fun PetFinderApi(): PetFinderApi

    fun cache(): Cache

    fun preferences(): Preferences
}