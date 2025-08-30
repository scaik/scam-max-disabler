package ru.scaik.scammaxdisabler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.scaik.scammaxdisabler.service.ServiceRestartHelper
import ru.scaik.scammaxdisabler.state.BlockerStateManager
import ru.scaik.scammaxdisabler.state.PermissionStateManager
import ru.scaik.scammaxdisabler.state.ServiceStateManager
import ru.scaik.scammaxdisabler.ui.screens.BlockerScreen
import ru.scaik.scammaxdisabler.ui.theme.ScamMaxDisablerTheme

class MainActivity : ComponentActivity() {

    internal lateinit var serviceStateManager: ServiceStateManager
    internal lateinit var blockerStateManager: BlockerStateManager
    internal lateinit var permissionStateManager: PermissionStateManager
    private lateinit var serviceRestartHelper: ServiceRestartHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeManagers()
        ensureServiceRunning()
        scheduleServiceChecks()
        enableEdgeToEdge()
        setContent {
            val isBlockerEnabled by blockerStateManager.blockerEnabledState.collectAsState()

            ScamMaxDisablerTheme(
                    isBlockerActive = isBlockerEnabled,
                    content = { BlockerScreen(context = this) }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        refreshAllStates()
        serviceStateManager.startMonitoring()
        permissionStateManager.startMonitoring()
    }

    override fun onStop() {
        super.onStop()
        serviceStateManager.stopMonitoring()
        permissionStateManager.stopMonitoring()
    }

    private fun initializeManagers() {
        val application = ScamMaxDisablerApplication.getInstance(this)
        if (application != null) {
            serviceStateManager = application.serviceStateManager
            blockerStateManager = application.blockerStateManager
            permissionStateManager = application.permissionStateManager
        } else {
            serviceStateManager = ServiceStateManager.getInstance(this)
            blockerStateManager = BlockerStateManager.getInstance(this)
            permissionStateManager = PermissionStateManager.getInstance(this)
        }
        serviceRestartHelper = ServiceRestartHelper(this)
    }

    private fun ensureServiceRunning() {
        lifecycleScope.launch { serviceStateManager.ensureServiceRunning() }
    }

    private fun scheduleServiceChecks() {
        serviceRestartHelper.schedulePeriodicServiceCheck()
    }

    private fun refreshAllStates() {
        permissionStateManager.refreshPermissionStates()
        blockerStateManager.refreshStateFromStorage()
    }
}
