package ru.scaik.scammaxdisabler.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.scaik.scammaxdisabler.R
import ru.scaik.scammaxdisabler.ScamMaxDisablerApplication
import ru.scaik.scammaxdisabler.state.PermissionStateManager
import ru.scaik.scammaxdisabler.state.ServiceStateManager

class WarmUpService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var serviceStateManager: ServiceStateManager
    private lateinit var permissionStateManager: PermissionStateManager

    override fun onCreate() {
        super.onCreate()
        initializeManagers()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createForegroundNotification())
    }

    private fun initializeManagers() {
        val application = ScamMaxDisablerApplication.getInstance(this)
        if (application != null) {
            serviceStateManager = application.serviceStateManager
            permissionStateManager = application.permissionStateManager
        } else {
            serviceStateManager = ServiceStateManager.getInstance(this)
            permissionStateManager = PermissionStateManager.getInstance(this)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureAccessibilityServiceRunning()
        return START_NOT_STICKY
    }

    private fun ensureAccessibilityServiceRunning() {
        serviceScope.launch {
            delay(SERVICE_STARTUP_DELAY_MS)

            permissionStateManager.refreshPermissionStates()
            val permissionState = permissionStateManager.permissionState.value

            if (permissionState.hasAccessibilityPermission) {
                serviceStateManager.ensureServiceRunning()
            }

            stopSelfGracefully()
        }
    }

    private fun stopSelfGracefully() {
        serviceScope.launch {
            delay(GRACEFUL_STOP_DELAY_MS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(Service.STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            stopSelf()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_MIN
                ).apply {
                    setShowBadge(false)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    private fun createForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(NOTIFICATION_TEXT)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "warmup_service_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "Service Bootstrap"
        private const val NOTIFICATION_ID = 1001
        private const val NOTIFICATION_TEXT = "Инициализация службы"
        private const val SERVICE_STARTUP_DELAY_MS = 200L
        private const val GRACEFUL_STOP_DELAY_MS = 500L
        private const val STOP_FOREGROUND_REMOVE = true
    }
}
