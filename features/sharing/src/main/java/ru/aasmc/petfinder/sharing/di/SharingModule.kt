package ru.aasmc.petfinder.sharing.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.migration.DisableInstallInCheck
import dagger.multibindings.IntoMap
import ru.aasmc.petfinder.common.data.PetFinderAnimalRepository
import ru.aasmc.petfinder.common.domain.repositories.AnimalRepository
import ru.aasmc.petfinder.common.utils.CoroutineDispatchersProvider
import ru.aasmc.petfinder.common.utils.DispatchersProvider
import ru.aasmc.petfinder.sharing.presentation.SharingFragmentViewModel

@Module
@DisableInstallInCheck
abstract class SharingModule {
    // these two are not scoped to SingletonComponent, so they can't be directly provided through
    // methods in SharingModuleDependencies
    @Binds
    abstract fun bindDispatchersProvider(dispatchersProvider: CoroutineDispatchersProvider): DispatchersProvider

    @Binds
    abstract fun bindRepository(repository: PetFinderAnimalRepository): AnimalRepository

    @Binds
    @IntoMap
    @ViewModelKey(SharingFragmentViewModel::class)
    abstract fun bindSharingFragmentViewModel(
        sharingFragmentViewModel: SharingFragmentViewModel
    ): ViewModel

    @Binds
    @Reusable
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}