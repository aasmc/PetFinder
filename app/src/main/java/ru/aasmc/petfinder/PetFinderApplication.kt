package ru.aasmc.petfinder

import android.app.Application
import ru.aasmc.logging.Logger

class PetFinderApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initLogger()
    }

    private fun initLogger() {
        Logger.init()
    }
}