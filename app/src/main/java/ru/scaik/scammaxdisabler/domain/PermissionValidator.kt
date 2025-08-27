package ru.scaik.scammaxdisabler.domain

import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import ru.scaik.scammaxdisabler.service.AppMonitorService

class PermissionValidator(private val context: Context) {

    fun hasAccessibility(): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val component = ComponentName(context, AppMonitorService::class.java)
        val fullId = component.flattenToString()
        val shortId = "${context.packageName}/.AppMonitorService"

        return enabledServices.split(':').any { it == fullId || it == shortId }
    }

    fun hasOverlay(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun hasBatteryExemption(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
    }

    fun hasAllRequired(): Boolean {
        return hasAccessibility() && hasOverlay()
    }
}
