package ru.scaik.scammaxdisabler

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.scaik.scammaxdisabler.state.BlockerStateManager
import ru.scaik.scammaxdisabler.state.PermissionStateManager
import ru.scaik.scammaxdisabler.state.ServiceStateManager

class ScamMaxDisablerApplication : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    lateinit var serviceStateManager: ServiceStateManager
        private set

    lateinit var blockerStateManager: BlockerStateManager
        private set

    lateinit var permissionStateManager: PermissionStateManager
        private set

    override fun onCreate() {
        super.onCreate()
        initializeManagers()
        ensureServiceRunningAfterDelay()
    }

    private fun initializeManagers() {
        serviceStateManager = ServiceStateManager.getInstance(this)
        blockerStateManager = BlockerStateManager.getInstance(this)
        permissionStateManager = PermissionStateManager.getInstance(this)
    }

    private fun ensureServiceRunningAfterDelay() {
        applicationScope.launch {
            delay(APP_START_SERVICE_CHECK_DELAY_MS)
            serviceStateManager.ensureServiceRunning()
        }
    }

    companion object {
        private const val APP_START_SERVICE_CHECK_DELAY_MS = 500L

        fun getInstance(context: android.content.Context): ScamMaxDisablerApplication? {
            val appContext = context.applicationContext
            return if (appContext is ScamMaxDisablerApplication) {
                appContext
            } else {
                null
            }
        }
    }
}
