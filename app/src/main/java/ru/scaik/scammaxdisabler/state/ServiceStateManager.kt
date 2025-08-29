package ru.scaik.scammaxdisabler.state

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.scaik.scammaxdisabler.service.AppMonitorService
import ru.scaik.scammaxdisabler.service.WarmUpService

class ServiceStateManager private constructor(private val context: Context) {

    private val applicationContext = context.applicationContext
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val handler = Handler(Looper.getMainLooper())

    private val _serviceRunningState = MutableStateFlow(false)
    val serviceRunningState: StateFlow<Boolean> = _serviceRunningState.asStateFlow()

    private val _accessibilityPermissionState = MutableStateFlow(false)
    val accessibilityPermissionState: StateFlow<Boolean> = _accessibilityPermissionState.asStateFlow()

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

    fun forceServiceRestart() {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                triggerServiceRestart()
            }
        }
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

    private fun isServiceProcessRunning(): Boolean {
        return try {
            val serviceComponent = ComponentName(applicationContext, AppMonitorService::class.java)
            val componentEnabledSetting = applicationContext.packageManager.getComponentEnabledSetting(serviceComponent)
            componentEnabledSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } catch (e: Exception) {
            false
        }
    }

    private fun triggerServiceRestart() {
        try {
            startWarmUpService()
        } catch (e: Exception) {
            handler.postDelayed({ startWarmUpService() }, RESTART_RETRY_DELAY_MS)
        }
    }

    private fun startWarmUpService() {
        val intent = Intent(applicationContext, WarmUpService::class.java)
        ContextCompat.startForegroundService(applicationContext, intent)
    }

    companion object {
        private const val SERVICE_CHECK_INTERVAL_MS = 30_000L
        private const val RESTART_RETRY_DELAY_MS = 1_000L

        @Volatile
        private var instance: ServiceStateManager? = null

        fun getInstance(context: Context): ServiceStateManager {
            return instance ?: synchronized(this) {
                instance ?: ServiceStateManager(context).also { instance = it }
            }
        }
    }
}
