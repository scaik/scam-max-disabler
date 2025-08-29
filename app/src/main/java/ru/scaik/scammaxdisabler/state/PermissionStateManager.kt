package ru.scaik.scammaxdisabler.state

import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ru.scaik.scammaxdisabler.service.AppMonitorService

data class PermissionState(
    val hasAccessibilityPermission: Boolean,
    val hasOverlayPermission: Boolean,
    val isBatteryOptimizationIgnored: Boolean
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

    val permissionState: StateFlow<PermissionState> = combine(
        _accessibilityPermissionState,
        _overlayPermissionState,
        _batteryOptimizationIgnoredState
    ) { accessibility, overlay, battery ->
        PermissionState(
            hasAccessibilityPermission = accessibility,
            hasOverlayPermission = overlay,
            isBatteryOptimizationIgnored = battery
        )
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PermissionState(false, false, true)
    )

    private var monitoringJob: Job? = null

    init {
        refreshPermissionStates()
        startMonitoring()
    }

    fun startMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = coroutineScope.launch {
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

                withContext(Dispatchers.Main) {
                    _accessibilityPermissionState.value = accessibility
                    _overlayPermissionState.value = overlay
                    _batteryOptimizationIgnoredState.value = battery
                }
            }
        }
    }

    private fun checkAccessibilityPermission(): Boolean {
        val enabledServices = Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val serviceComponent = ComponentName(applicationContext, AppMonitorService::class.java)
        val expectedIdentifiers = listOf(
            serviceComponent.flattenToString(),
            "${applicationContext.packageName}/.AppMonitorService",
            "${applicationContext.packageName}/${AppMonitorService::class.java.name}"
        )

        return enabledServices.split(':').any { enabledService ->
            expectedIdentifiers.any { identifier ->
                enabledService == identifier
            }
        }
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(applicationContext)
        } else {
            true
        }
    }

    private fun checkBatteryOptimizationIgnored(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(applicationContext.packageName)
        } else {
            true
        }
    }

    companion object {
        private const val PERMISSION_CHECK_INTERVAL_MS = 2_000L

        @Volatile
        private var instance: PermissionStateManager? = null

        fun getInstance(context: Context): PermissionStateManager {
            return instance ?: synchronized(this) {
                instance ?: PermissionStateManager(context).also { instance = it }
            }
        }
    }
}
