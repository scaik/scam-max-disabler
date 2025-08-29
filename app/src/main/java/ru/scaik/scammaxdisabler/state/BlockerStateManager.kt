package ru.scaik.scammaxdisabler.state

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BlockerStateManager private constructor(private val context: Context) {

    private val applicationContext = context.applicationContext

    private val _blockerEnabledState = MutableStateFlow(loadBlockerEnabledState())
    val blockerEnabledState: StateFlow<Boolean> = _blockerEnabledState.asStateFlow()

    fun setBlockerEnabled(enabled: Boolean) {
        if (_blockerEnabledState.value == enabled) return

        _blockerEnabledState.value = enabled
        persistBlockerEnabledState(enabled)
    }

    fun isBlockerEnabled(): Boolean {
        return loadBlockerEnabledState()
    }

    fun refreshStateFromStorage() {
        _blockerEnabledState.value = loadBlockerEnabledState()
    }

    private fun loadBlockerEnabledState(): Boolean {
        val preferences = applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        return preferences.getBoolean(KEY_BLOCKER_ENABLED, DEFAULT_BLOCKER_ENABLED)
    }

    private fun persistBlockerEnabledState(enabled: Boolean) {
        val preferences = applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        preferences.edit(commit = true) {
            putBoolean(KEY_BLOCKER_ENABLED, enabled)
        }
    }

    companion object {
        private const val PREFERENCES_NAME = "scam_service_prefs"
        private const val KEY_BLOCKER_ENABLED = "scam_max_blocker_enabled"
        private const val DEFAULT_BLOCKER_ENABLED = true

        @Volatile
        private var instance: BlockerStateManager? = null

        fun getInstance(context: Context): BlockerStateManager {
            return instance ?: synchronized(this) {
                instance ?: BlockerStateManager(context).also { instance = it }
            }
        }
    }
}
