package ru.aasmc.petfinder

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import ru.aasmc.logging.Logger

@HiltAndroidApp
class PetFinderApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initLogger()
    }

    private fun initLogger() {
        Logger.init()
    }
}