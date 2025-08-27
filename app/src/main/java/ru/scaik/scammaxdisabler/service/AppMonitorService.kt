package ru.scaik.scammaxdisabler.service

import android.accessibilityservice.AccessibilityService
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.core.net.toUri
import ru.scaik.scammaxdisabler.data.Prefs

class AppMonitorService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!shouldBlock(event)) return

        val packageName = event?.packageName?.toString() ?: return
        if (packageName != TARGET_PACKAGE) return

        blockApp()
    }

    override fun onInterrupt() {}

    private fun shouldBlock(event: AccessibilityEvent?): Boolean {
        return Prefs.isBlockerEnabled(this) &&
               event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
    }

    private fun blockApp() {
        performGlobalAction(GLOBAL_ACTION_HOME)
        handler.postDelayed({ showCrashDialog() }, DIALOG_DELAY_MS)
    }

    private fun showCrashDialog() {
        val dialog = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle("В приложении \"MAX\" произошла ошибка")
            .setPositiveButton("Закрыть приложение") { _, _ ->
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
            .setNegativeButton("Сведения о приложении") { _, _ ->
                openAppSettings()
            }
            .create()

        dialog.window?.setType(getOverlayType())
        dialog.show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = "package:$TARGET_PACKAGE".toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    private fun getOverlayType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
    }

    companion object {
        private const val TARGET_PACKAGE = "ru.oneme.app"
        private const val DIALOG_DELAY_MS = 250L
    }
}
