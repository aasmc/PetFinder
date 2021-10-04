package ru.aasmc.petfinder

import com.google.android.play.core.splitcompat.SplitCompatApplication
import dagger.hilt.android.HiltAndroidApp
import ru.aasmc.logging.Logger

@HiltAndroidApp
class PetFinderApplication : SplitCompatApplication() {

    override fun onCreate() {
        super.onCreate()

        initLogger()
    }

    private fun initLogger() {
        Logger.init()
    }
}