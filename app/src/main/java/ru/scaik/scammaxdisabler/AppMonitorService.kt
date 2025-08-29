package ru.scaik.scammaxdisabler

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

class AppMonitorService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!Prefs.isBlockerEnabled(this))
            return

        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
            return

        val packageName = event.packageName?.toString() ?: return

        if (packageName != "ru.oneme.app")
            return

        performGlobalAction(GLOBAL_ACTION_HOME)
        Handler(Looper.getMainLooper()).postDelayed({
            showFakeCrashDialog()
        }, 250)
    }

    override fun onInterrupt() {
    }

    private fun showFakeCrashDialog() {
        val dialogBuilder =
            AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("В приложении \"MAX\" произошла ошибка")
                .setPositiveButton("Закрыть приложение") { _, _ ->
                    performGlobalAction(GLOBAL_ACTION_HOME)
                }
                .setNegativeButton("Сведения о приложении") { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = "package:ru.oneme.app".toUri()
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }

        val dialog = dialogBuilder.create()
        dialog.window?.setType(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        )
        dialog.show()
    }
}
