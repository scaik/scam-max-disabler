package ru.scaik.scammaxdisabler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.scaik.scammaxdisabler.di.AppComponent
import ru.scaik.scammaxdisabler.ui.screens.BlockerScreen
import ru.scaik.scammaxdisabler.ui.theme.ScamMaxDisablerTheme

class MainActivity : ComponentActivity() {

    private val blockerStateManager = AppComponent.blockerStateManager
    private val serviceStateManager = AppComponent.serviceStateManager
    private val permissionStateManager = AppComponent.permissionStateManager
    private val serviceRestartHelper = AppComponent.serviceRestartHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensureServiceRunning()
        scheduleServiceChecks()
        enableEdgeToEdge()
        setContent {
            val isBlockerEnabled by blockerStateManager.blockerEnabledState.collectAsState()

            ScamMaxDisablerTheme(
                    isBlockerActive = isBlockerEnabled,
                    content = { BlockerScreen() }
            )
        }
    }

    // Should re-monitor the status when the user returns to activity
    override fun onResume() {
        super.onResume()
        refreshAllStates()
        serviceStateManager.startMonitoring()
        permissionStateManager.startMonitoring()
    }

    override fun onStop() {
        super.onStop()
        serviceStateManager.stopMonitoring()
        permissionStateManager.stopMonitoring()
    }

    private fun ensureServiceRunning() {
        lifecycleScope.launch {
            serviceStateManager.ensureServiceRunning()
        }
    }

    private fun scheduleServiceChecks() {
        serviceRestartHelper.schedulePeriodicServiceCheck()
    }

    private fun refreshAllStates() {
        permissionStateManager.refreshPermissionStates()
        blockerStateManager.refreshStateFromStorage()
    }
}
