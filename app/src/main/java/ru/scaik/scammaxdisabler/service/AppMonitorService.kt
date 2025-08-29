package ru.scaik.scammaxdisabler.service

import android.accessibilityservice.AccessibilityService
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.core.net.toUri
import ru.scaik.scammaxdisabler.ScamMaxDisablerApplication
import ru.scaik.scammaxdisabler.state.BlockerStateManager
import ru.scaik.scammaxdisabler.state.ServiceStateManager
import java.util.logging.Logger

class AppMonitorService : AccessibilityService() {

    private lateinit var blockerStateManager: BlockerStateManager
    private lateinit var serviceStateManager: ServiceStateManager
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        initializeManagers()
        notifyServiceStarted()
    }

    private fun initializeManagers() {
        val application = ScamMaxDisablerApplication.getInstance(this)
        if (application != null) {
            blockerStateManager = application.blockerStateManager
            serviceStateManager = application.serviceStateManager
        } else {
            blockerStateManager = BlockerStateManager.getInstance(this)
            serviceStateManager = ServiceStateManager.getInstance(this)
        }
    }

    private fun notifyServiceStarted() {
        serviceStateManager.ensureServiceRunning()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!shouldProcessEvent(event)) return

        val packageName = event?.packageName?.toString() ?: return
        if (!isTargetApplication(packageName)) return

        blockTargetApplication()
    }

    private fun shouldProcessEvent(event: AccessibilityEvent?): Boolean {
        return blockerStateManager.isBlockerEnabled() &&
               event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
    }

    private fun isTargetApplication(packageName: String): Boolean {
        return packageName == TARGET_PACKAGE_NAME
    }

    private fun blockTargetApplication() {
        navigateToHomeScreen()
        scheduleBlockingDialogDisplay()
    }

    private fun navigateToHomeScreen() {
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    private fun scheduleBlockingDialogDisplay() {
        mainHandler.postDelayed(
            { displayBlockingDialog() },
            DIALOG_DISPLAY_DELAY_MS
        )
    }

    private fun displayBlockingDialog() {
        val dialog = createBlockingDialog()
        configureDialogWindowType(dialog)
        dialog.show()
    }

    private fun createBlockingDialog(): AlertDialog {
        return AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle(BLOCKING_DIALOG_TITLE)
            .setPositiveButton(BLOCKING_DIALOG_CLOSE_BUTTON) { _, _ ->
                navigateToHomeScreen()
            }
            .setNegativeButton(BLOCKING_DIALOG_INFO_BUTTON) { _, _ ->
                openApplicationSettings()
            }
            .create()
    }

    private fun configureDialogWindowType(dialog: AlertDialog) {
        dialog.window?.setType(getOverlayWindowType())
    }

    private fun getOverlayWindowType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
    }

    private fun openApplicationSettings() {
        val intent = createApplicationSettingsIntent()
        startActivity(intent)
    }

    private fun createApplicationSettingsIntent(): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = "package:$TARGET_PACKAGE_NAME".toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    override fun onInterrupt() {
        // Required by AccessibilityService but no cleanup needed
    }

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val TARGET_PACKAGE_NAME = "ru.oneme.app"
        private const val DIALOG_DISPLAY_DELAY_MS = 250L
        private const val BLOCKING_DIALOG_TITLE = "В приложении \"MAX\" произошла ошибка"
        private const val BLOCKING_DIALOG_CLOSE_BUTTON = "Закрыть приложение"
        private const val BLOCKING_DIALOG_INFO_BUTTON = "Сведения о приложении"
    }
}
