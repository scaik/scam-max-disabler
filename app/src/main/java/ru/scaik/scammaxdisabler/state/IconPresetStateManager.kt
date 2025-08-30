package ru.scaik.scammaxdisabler.state

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.scaik.scammaxdisabler.model.IconPresetId

class IconPresetStateManager private constructor(private val context: Context) {

    private val applicationContext = context.applicationContext

    private val _selectedPresetId = MutableStateFlow(loadSelectedPresetId())
    val selectedPresetId: StateFlow<String> = _selectedPresetId.asStateFlow()

    fun setSelectedPresetId(presetId: String) {
        if (_selectedPresetId.value == presetId) return

        _selectedPresetId.value = presetId
        persistSelectedPresetId(presetId)
    }

    fun getSelectedPresetId(): String {
        return loadSelectedPresetId()
    }

    fun refreshStateFromStorage() {
        _selectedPresetId.value = loadSelectedPresetId()
    }

    private fun loadSelectedPresetId(): String {
        val preferences =
                applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        return preferences.getString(KEY_SELECTED_PRESET_ID, DEFAULT_PRESET_ID) ?: DEFAULT_PRESET_ID
    }

    private fun persistSelectedPresetId(presetId: String) {
        val preferences =
                applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        preferences.edit(commit = true) { putString(KEY_SELECTED_PRESET_ID, presetId) }
    }

    companion object {
        private const val PREFERENCES_NAME = "icon_preset_prefs"
        private const val KEY_SELECTED_PRESET_ID = "selected_icon_preset_id"
        private val DEFAULT_PRESET_ID = IconPresetId.DEFAULT.value

        @Volatile private var instance: IconPresetStateManager? = null

        fun getInstance(context: Context): IconPresetStateManager {
            return instance
                    ?: synchronized(this) {
                        instance ?: IconPresetStateManager(context).also { instance = it }
                    }
        }
    }
}
