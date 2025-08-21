package ru.scaik.scammaxdisabler

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

class WarmUpService : Service() {

	override fun onCreate() {
		super.onCreate()
		createChannel()
		startForeground(NOTIF_ID, buildNotification())
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		forceRebindIfEnabled()
		return START_NOT_STICKY
	}

	override fun onBind(intent: Intent?): IBinder? = null

	private fun forceRebindIfEnabled() {
		try {
			if (!isAccessibilityEnabled()) {
				stopSelfSafely()
				return
			}

			val packageManagerRef = packageManager
			val serviceComponent = ComponentName(this, AppMonitorService::class.java)

			packageManagerRef.setComponentEnabledSetting(
				serviceComponent,
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP
			)

			Handler(Looper.getMainLooper()).postDelayed({
				try {
					packageManagerRef.setComponentEnabledSetting(
						serviceComponent,
						PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
						PackageManager.DONT_KILL_APP
					)
				} finally {
					stopSelfSafely()
				}
			}, 400)
		} catch (_: Exception) {
			stopSelfSafely()
		}
	}

	private fun stopSelfSafely() {
		try {
			stopForeground(true)
		} catch (_: Exception) { }
		stopSelf()
	}

	private fun isAccessibilityEnabled(): Boolean {
		val enabled = android.provider.Settings.Secure.getString(
			contentResolver,
			android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
		) ?: ""
		val component = ComponentName(this, AppMonitorService::class.java)
		val fullId = component.flattenToString()
		val shortId = "$packageName/.AppMonitorService"
		return enabled.split(':').any { it == fullId || it == shortId }
	}

	private fun createChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val manager = getSystemService(NotificationManager::class.java)
			if (manager.getNotificationChannel(CHANNEL_ID) == null) {
				val channel = NotificationChannel(
					CHANNEL_ID,
					"Service bootstrap",
					NotificationManager.IMPORTANCE_MIN
				)
				channel.setShowBadge(false)
				manager.createNotificationChannel(channel)
			}
		}
	}

	private fun buildNotification(): Notification {
		return NotificationCompat.Builder(this, CHANNEL_ID)
			.setSmallIcon(R.mipmap.ic_launcher)
			.setContentTitle(getString(R.string.app_name))
			.setContentText("Инициализация службы")
			.setPriority(NotificationCompat.PRIORITY_MIN)
			.setOngoing(true)
			.build()
	}

	companion object {
		private const val CHANNEL_ID = "warmup"
		private const val NOTIF_ID = 1001
	}
}


