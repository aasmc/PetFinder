package ru.aasmc.petfinder.common.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.aasmc.petfinder.common.data.preferences.PetSavePreferences
import ru.aasmc.petfinder.common.data.preferences.Preferences

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModule {

    @Binds
    abstract fun providePreferences(preferences: PetSavePreferences): Preferences
}