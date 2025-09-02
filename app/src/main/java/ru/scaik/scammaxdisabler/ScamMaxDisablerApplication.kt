package ru.scaik.scammaxdisabler

import android.app.Application
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.scaik.scammaxdisabler.di.AppModule
import ru.scaik.scammaxdisabler.di.AppModuleImpl

class ScamMaxDisablerApplication : Application() {

    val applicationScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    lateinit var appModule: AppModule
        private set

    override fun onCreate() {
        super.onCreate()
        appContext = this
        appModule = AppModuleImpl(appContext = this)
        ensureServiceRunningAfterDelay()
    }

    private fun ensureServiceRunningAfterDelay() {
        applicationScope.launch {
            delay(APP_START_SERVICE_CHECK_DELAY_MS)
            appModule.serviceStateManager.ensureServiceRunning()
        }
    }

    companion object {
        private const val APP_START_SERVICE_CHECK_DELAY_MS = 500L

        // The application Context is safe to store in static field because it lives throughout
        // the entire application process and will not cause memory leaks.
        lateinit var appContext: Context
            private set
    }
}
