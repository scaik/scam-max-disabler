package ru.scaik.scammaxdisabler.manager

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.scaik.scammaxdisabler.model.IconPreset
import ru.scaik.scammaxdisabler.model.IconPresetId

class AppIconManager(context: Context) {

    private val packageManager = context.packageManager
    private val packageName = context.packageName

    suspend fun applyIconPreset(preset: IconPreset): Boolean =
            withContext(Dispatchers.IO) {
                try {
                    disableAllActivityAliases()
                    enableActivityAliasForPreset(preset)
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

    private fun disableAllActivityAliases() {
        IconPresetId.entries.forEach { presetId ->
            if (presetId != IconPresetId.DEFAULT) {
                val aliasName = buildActivityAliasName(presetId.value)
                setComponentEnabledState(aliasName, false)
            }
        }
        setComponentEnabledState(MAIN_ACTIVITY_NAME, false)
    }

    private fun enableActivityAliasForPreset(preset: IconPreset) {
        if (preset.isDefault) {
            setComponentEnabledState(MAIN_ACTIVITY_NAME, true)
        } else {
            val aliasName = buildActivityAliasName(preset.id)
            setComponentEnabledState(aliasName, true)
        }
    }

    private fun setComponentEnabledState(componentClassName: String, enabled: Boolean) {
        val componentName = ComponentName(packageName, componentClassName)
        val newState =
                if (enabled) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }
        packageManager.setComponentEnabledSetting(
                componentName,
                newState,
                PackageManager.DONT_KILL_APP
        )
    }

    private fun buildActivityAliasName(presetId: String): String {
        val capitalizedId = presetId.replaceFirstChar { it.uppercase() }
        return "$packageName.MainActivityAlias$capitalizedId"
    }

    fun isActivityAliasEnabled(preset: IconPreset): Boolean {
        val componentClassName =
                if (preset.isDefault) {
                    MAIN_ACTIVITY_NAME
                } else {
                    buildActivityAliasName(preset.id)
                }

        val componentName = ComponentName(packageName, componentClassName)
        val state = packageManager.getComponentEnabledSetting(componentName)

        return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
                state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
    }

    companion object {
        private const val MAIN_ACTIVITY_NAME = "ru.scaik.scammaxdisabler.MainActivity"
    }
}
