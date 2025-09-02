package ru.scaik.scammaxdisabler.state

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BlockerStateManager(appContext: Context) {

    private val sharedPrefs by lazy {
        appContext.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
    }

    private val _blockerEnabledState = MutableStateFlow(loadBlockerEnabledState())
    val blockerEnabledState: StateFlow<Boolean> = _blockerEnabledState.asStateFlow()

    private val _installationBlockingEnabledState =
            MutableStateFlow(loadInstallationBlockingEnabledState())
    val installationBlockingEnabledState: StateFlow<Boolean> =
            _installationBlockingEnabledState.asStateFlow()

    private val _isInstallationBlockingMode = MutableStateFlow(loadCurrentMode())
    val isInstallationBlockingMode: StateFlow<Boolean> = _isInstallationBlockingMode.asStateFlow()

    fun setBlockerEnabled(enabled: Boolean) {
        if (_blockerEnabledState.value == enabled) return

        _blockerEnabledState.value = enabled
        persistBlockerEnabledState(enabled)
    }

    fun isBlockerEnabled(): Boolean {
        return loadBlockerEnabledState()
    }

    fun setInstallationBlockingEnabled(enabled: Boolean) {
        if (_installationBlockingEnabledState.value == enabled) return

        _installationBlockingEnabledState.value = enabled
        persistInstallationBlockingEnabledState(enabled)
    }

    fun isInstallationBlockingEnabled(): Boolean {
        return loadInstallationBlockingEnabledState()
    }

    fun setInstallationBlockingMode(isInstallationMode: Boolean) {
        if (_isInstallationBlockingMode.value == isInstallationMode) return

        _isInstallationBlockingMode.value = isInstallationMode
        persistCurrentMode(isInstallationMode)
    }

    fun isInstallationBlockingMode(): Boolean {
        return loadCurrentMode()
    }

    fun refreshStateFromStorage() {
        _blockerEnabledState.value = loadBlockerEnabledState()
        _installationBlockingEnabledState.value = loadInstallationBlockingEnabledState()
        _isInstallationBlockingMode.value = loadCurrentMode()
    }

    private fun loadBlockerEnabledState(): Boolean {
        return sharedPrefs.getBoolean(KEY_BLOCKER_ENABLED, DEFAULT_BLOCKER_ENABLED)
    }

    private fun persistBlockerEnabledState(enabled: Boolean) {
        sharedPrefs.edit(commit = true) { putBoolean(KEY_BLOCKER_ENABLED, enabled) }
    }

    private fun loadInstallationBlockingEnabledState(): Boolean {
        return sharedPrefs.getBoolean(
            KEY_INSTALLATION_BLOCKING_ENABLED,
            DEFAULT_INSTALLATION_BLOCKING_ENABLED
        )
    }

    private fun persistInstallationBlockingEnabledState(enabled: Boolean) {
        sharedPrefs.edit(commit = true) {
            putBoolean(KEY_INSTALLATION_BLOCKING_ENABLED, enabled)
        }
    }

    private fun loadCurrentMode(): Boolean {
        return sharedPrefs.getBoolean(KEY_CURRENT_MODE, DEFAULT_CURRENT_MODE)
    }

    private fun persistCurrentMode(isInstallationMode: Boolean) {
        sharedPrefs.edit(commit = true) {
            putBoolean(KEY_CURRENT_MODE, isInstallationMode)
        }
    }

    companion object {
        private const val PREFERENCES_NAME = "scam_service_prefs"
        private const val KEY_BLOCKER_ENABLED = "scam_max_blocker_enabled"
        private const val KEY_INSTALLATION_BLOCKING_ENABLED = "installation_blocking_enabled"
        private const val KEY_CURRENT_MODE = "current_mode_installation_blocking"
        private const val DEFAULT_BLOCKER_ENABLED = true
        private const val DEFAULT_INSTALLATION_BLOCKING_ENABLED = false
        private const val DEFAULT_CURRENT_MODE =
                false // false = active blocking, true = installation blocking
    }
}
