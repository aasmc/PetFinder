package ru.aasmc.petfinder

import com.google.android.play.core.splitcompat.SplitCompatApplication
import dagger.hilt.android.HiltAndroidApp
import ru.aasmc.logging.Logger
import ru.aasmc.petfinder.remoteconfig.RemoteConfigUtil

@HiltAndroidApp
class PetFinderApplication : SplitCompatApplication() {

    override fun onCreate() {
        super.onCreate()

        initLogger()
        RemoteConfigUtil.init(BuildConfig.DEBUG)
    }

    private fun initLogger() {
        Logger.init()
    }
}