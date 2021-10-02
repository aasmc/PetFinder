package ru.aasmc.common.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import io.reactivex.disposables.CompositeDisposable
import ru.aasmc.petfinder.common.data.FakeRepository
import ru.aasmc.petfinder.common.domain.repositories.AnimalRepository
import ru.aasmc.petfinder.common.utils.CoroutineDispatchersProvider
import ru.aasmc.petfinder.common.utils.DispatchersProvider

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class TestActivityRetainedModule {

    @Binds
    @ActivityRetainedScoped
    abstract fun bindAnimalRepository(repository: FakeRepository): AnimalRepository

    @Binds
    abstract fun bindDispatchersProvider(dispatchersProvider: CoroutineDispatchersProvider): DispatchersProvider

    companion object {
        @Provides
        fun provideCompositeDisposable() = CompositeDisposable()
    }
}