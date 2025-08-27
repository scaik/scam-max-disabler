package ru.scaik.scammaxdisabler.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import ru.scaik.scammaxdisabler.R
import ru.scaik.scammaxdisabler.data.Prefs
import ru.scaik.scammaxdisabler.domain.PermissionValidator

class WarmUpService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val permissionValidator by lazy { PermissionValidator(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        restartAccessibilityService()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun restartAccessibilityService() {
        if (!shouldRestart()) {
            stopSelf()
            return
        }

        try {
            val component = ComponentName(this, AppMonitorService::class.java)

            disableService(component)
            handler.postDelayed({
                enableService(component)
                stopSelf()
            }, RESTART_DELAY_MS)

        } catch (e: Exception) {
            stopSelf()
        }
    }

    private fun shouldRestart(): Boolean {
        return Prefs.isBlockerEnabled(this) && permissionValidator.hasAccessibility()
    }

    private fun disableService(component: ComponentName) {
        packageManager.setComponentEnabledSetting(
            component,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun enableService(component: ComponentName) {
        packageManager.setComponentEnabledSetting(
            component,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Service initialization",
                    NotificationManager.IMPORTANCE_MIN
                ).apply {
                    setShowBadge(false)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Initializing service")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "warmup_channel"
        private const val NOTIFICATION_ID = 1001
        private const val RESTART_DELAY_MS = 500L
    }
}
