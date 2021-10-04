package ru.aasmc.petfinder.sharing.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.aasmc.petfinder.di.SharingModuleDependencies
import ru.aasmc.petfinder.sharing.presentation.SharingFragment

@Component(
    dependencies = [SharingModuleDependencies::class],
    modules = [SharingModule::class]
)
interface SharingComponent {

    fun inject(fragment: SharingFragment)

    @Component.Builder
    interface Builder {
        fun context(@BindsInstance context: Context): Builder
        fun moduleDependencies(sharingModuleDependencies: SharingModuleDependencies): Builder
        fun build(): SharingComponent
    }

}