package com.precisevolumedial.app

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import com.precisevolumedial.app.audio.AudioFxEngine
import com.precisevolumedial.app.audio.AudioFxService
import com.precisevolumedial.app.audio.VolumeController
import com.precisevolumedial.app.audio.eq.EqController
import com.precisevolumedial.app.data.AutoEqClient
import com.precisevolumedial.app.data.ProfileRepository
import com.precisevolumedial.app.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Manual dependency container; swap for Hilt/Koin if the app grows. */
class AppContainer(private val application: Application) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val settings = SettingsRepository(application)
    val fx = AudioFxEngine()
    val volume = VolumeController(application, fx)
    val profiles = ProfileRepository(application)
    val eq = EqController(settings, profiles, AutoEqClient(application), fx, scope)

    fun start() {
        eq.start()
        // The master switch decides whether the effects exist at all.
        scope.launch {
            settings.settings.map { it.processingEnabled }.distinctUntilChanged().collect { on ->
                if (on) fx.start() else fx.stop()
            }
        }
        // Hold a foreground service while the effect must stay alive, so the system doesn't reclaim the process.
        scope.launch {
            fx.keepAlive.collect { needed ->
                val intent = Intent(application, AudioFxService::class.java)
                try {
                    if (needed) ContextCompat.startForegroundService(application, intent) else application.stopService(intent)
                } catch (_: IllegalStateException) {
                    // Started from the background; it will be retried on the next change from the UI.
                }
            }
        }
    }
}

class PreciseVolumeDialApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this).also { it.start() }
    }
}
