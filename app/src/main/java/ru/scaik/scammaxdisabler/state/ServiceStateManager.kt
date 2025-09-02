package ru.scaik.scammaxdisabler.state

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.scaik.scammaxdisabler.service.AppMonitorService
import ru.scaik.scammaxdisabler.service.WarmUpService

class ServiceStateManager(private val appContext: Context) {
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val handler = Handler(Looper.getMainLooper())

    private val _serviceRunningState = MutableStateFlow(false)

    private val _accessibilityPermissionState = MutableStateFlow(false)

    private var monitoringJob: Job? = null

    init {
        startMonitoring()
    }

    fun ensureServiceRunning() {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                if (!isAccessibilityServiceEnabled()) {
                    return@withContext
                }

                if (!isServiceProcessRunning()) {
                    triggerServiceRestart()
                }
            }
        }
    }

    fun startMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = coroutineScope.launch {
            while (isActive) {
                updateServiceStates()
                delay(SERVICE_CHECK_INTERVAL_MS)
            }
        }
    }

    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
    }

    private suspend fun updateServiceStates() {
        withContext(Dispatchers.IO) {
            val accessibilityEnabled = isAccessibilityServiceEnabled()
            val serviceRunning = isServiceProcessRunning()

            withContext(Dispatchers.Main) {
                _accessibilityPermissionState.value = accessibilityEnabled
                _serviceRunningState.value = serviceRunning && accessibilityEnabled
            }
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            appContext.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val serviceComponent = ComponentName(appContext, AppMonitorService::class.java)
        val expectedIdentifiers = listOf(
            serviceComponent.flattenToString(),
            "${appContext.packageName}/.AppMonitorService",
            "${appContext.packageName}/${AppMonitorService::class.java.name}"
        )

        return enabledServices.split(':').any { enabledService ->
            expectedIdentifiers.any { identifier ->
                enabledService == identifier
            }
        }
    }

    private fun isServiceProcessRunning(): Boolean {
        return try {
            val serviceComponent = ComponentName(appContext, AppMonitorService::class.java)
            val componentEnabledSetting =
                appContext.packageManager.getComponentEnabledSetting(serviceComponent)
            componentEnabledSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } catch (_: Exception) {
            false
        }
    }

    private fun triggerServiceRestart() {
        try {
            startWarmUpService()
        } catch (_: Exception) {
            handler.postDelayed({ startWarmUpService() }, RESTART_RETRY_DELAY_MS)
        }
    }

    private fun startWarmUpService() {
        val intent = Intent(appContext, WarmUpService::class.java)
        ContextCompat.startForegroundService(appContext, intent)
    }

    companion object {
        private const val SERVICE_CHECK_INTERVAL_MS = 30_000L
        private const val RESTART_RETRY_DELAY_MS = 1_000L
    }
}
