package ru.scaik.scammaxdisabler.state

import android.content.ComponentName
import android.content.Context
import android.os.PowerManager
import android.provider.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.scaik.scammaxdisabler.service.AppMonitorService

data class PermissionState(
    val hasAccessibilityPermission: Boolean,
    val hasOverlayPermission: Boolean,
    val isBatteryOptimizationIgnored: Boolean,
    val hasAutoloadPermission: Boolean
) {
    val hasAllRequiredPermissions: Boolean
        get() = hasAccessibilityPermission && hasOverlayPermission
}

class PermissionStateManager private constructor(private val context: Context) {

    private val applicationContext = context.applicationContext
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _accessibilityPermissionState = MutableStateFlow(false)
    private val _overlayPermissionState = MutableStateFlow(false)
    private val _batteryOptimizationIgnoredState = MutableStateFlow(true)
    private val _autoloadPermissionState = MutableStateFlow(true)

    val permissionState: StateFlow<PermissionState> =
        combine(
            _accessibilityPermissionState,
            _overlayPermissionState,
            _batteryOptimizationIgnoredState,
            _autoloadPermissionState
        ) { accessibility, overlay, battery, autoload ->
            PermissionState(
                hasAccessibilityPermission = accessibility,
                hasOverlayPermission = overlay,
                isBatteryOptimizationIgnored = battery,
                hasAutoloadPermission = autoload
            )
        }
            .stateIn(
                scope = coroutineScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = PermissionState(false, false, true, true)
            )

    private var monitoringJob: Job? = null

    init {
        refreshPermissionStates()
        startMonitoring()
    }

    fun startMonitoring() {
        monitoringJob?.cancel()
        monitoringJob =
            coroutineScope.launch {
                while (isActive) {
                    refreshPermissionStates()
                    delay(PERMISSION_CHECK_INTERVAL_MS)
                }
            }
    }

    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
    }

    fun refreshPermissionStates() {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val accessibility = checkAccessibilityPermission()
                val overlay = checkOverlayPermission()
                val battery = checkBatteryOptimizationIgnored()
                val autoload = checkAutoloadPermission()

                withContext(Dispatchers.Main) {
                    _accessibilityPermissionState.value = accessibility
                    _overlayPermissionState.value = overlay
                    _batteryOptimizationIgnoredState.value = battery
                    _autoloadPermissionState.value = autoload
                }
            }
        }
    }

    private fun checkAccessibilityPermission(): Boolean {
        val enabledServices =
            Settings.Secure.getString(
                applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
                ?: return false

        val serviceComponent = ComponentName(applicationContext, AppMonitorService::class.java)
        val expectedIdentifiers =
            listOf(
                serviceComponent.flattenToString(),
                "${applicationContext.packageName}/.AppMonitorService",
                "${applicationContext.packageName}/${AppMonitorService::class.java.name}"
            )

        return enabledServices.split(':').any { enabledService ->
            expectedIdentifiers.any { identifier -> enabledService == identifier }
        }
    }

    private fun checkOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(applicationContext)
    }

    private fun checkAutoloadPermission(): Boolean {
        // For most devices, if the app can receive BOOT_COMPLETED broadcasts
        // and start foreground services, autoload permission is effectively granted.
        // Manufacturer-specific autostart permissions are hard to detect programmatically.

        return try {
            // Check if we can potentially start services on boot
            val packageManager = applicationContext.packageManager
            val bootPermission =
                packageManager.checkPermission(
                    android.Manifest.permission.RECEIVE_BOOT_COMPLETED,
                    applicationContext.packageName
                )

            // Basic check - if we have boot permission and foreground service permission,
            // we assume autoload is available (manufacturer-specific restrictions may still apply)
            bootPermission == android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            // Default to true if we can't check properly
            true
        }
    }

    private fun checkBatteryOptimizationIgnored(): Boolean {
        val powerManager =
            applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(applicationContext.packageName)
    }

    companion object {
        private const val PERMISSION_CHECK_INTERVAL_MS = 2_000L

        @Volatile
        private var instance: PermissionStateManager? = null

        fun getInstance(context: Context): PermissionStateManager {
            return instance
                ?: synchronized(this) {
                    instance ?: PermissionStateManager(context).also { instance = it }
                }
        }
    }
}
