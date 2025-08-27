package ru.scaik.scammaxdisabler.domain

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.scaik.scammaxdisabler.data.Prefs
import ru.scaik.scammaxdisabler.domain.AppState
import ru.scaik.scammaxdisabler.domain.PermissionValidator
import ru.scaik.scammaxdisabler.domain.ServiceRunner

class StateCoordinator(private val context: Context) {

    private val permissionValidator = PermissionValidator(context)
    private val serviceRunner = ServiceRunner(context)

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    fun initialize() {
        refreshState()
    }

    fun refreshState() {
        val accessibilityGranted = permissionValidator.hasAccessibility()
        val overlayGranted = permissionValidator.hasOverlay()
        val batteryExempted = permissionValidator.hasBatteryExemption()
        val blockerEnabled = Prefs.isBlockerEnabled(context)

        val actuallyEnabled = blockerEnabled && accessibilityGranted

        if (blockerEnabled && !accessibilityGranted) {
            Prefs.setBlockerEnabled(context, false)
        }

        _state.value = AppState(
            blockerEnabled = actuallyEnabled,
            accessibilityGranted = accessibilityGranted,
            overlayGranted = overlayGranted,
            batteryExempted = batteryExempted,
            serviceHealthy = actuallyEnabled && accessibilityGranted,
            canToggle = accessibilityGranted && overlayGranted,
            lastUpdate = System.currentTimeMillis()
        )
    }

    fun toggleBlocker(): Boolean {
        val currentState = _state.value

        if (!currentState.canToggle) {
            updateError("Missing required permissions")
            return false
        }

        val newEnabled = !currentState.blockerEnabled
        Prefs.setBlockerEnabled(context, newEnabled)

        if (newEnabled) {
            val result = serviceRunner.startWarmUp()
            if (result.isFailure) {
                updateError("Failed to start service")
                return false
            }
        }

        refreshState()
        return true
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    private fun updateError(message: String) {
        _state.value = _state.value.copy(
            errorMessage = message,
            lastUpdate = System.currentTimeMillis()
        )
    }
}
