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
import ru.scaik.scammaxdisabler.ScamMaxDisablerApplication
import ru.scaik.scammaxdisabler.state.BlockerStateManager
import ru.scaik.scammaxdisabler.state.ServiceStateManager
import ru.scaik.scammaxdisabler.R

class AppMonitorService : AccessibilityService() {

    private lateinit var blockerStateManager: BlockerStateManager
    private lateinit var serviceStateManager: ServiceStateManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var lastBlockedTimestamp = 0L
    private var lastHandledPackage: String? = null

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

        if (isTargetAppBecomingActive(event, packageName)) {
            blockTargetApplication()
        }
    }

    private fun shouldProcessEvent(event: AccessibilityEvent?): Boolean {
        return blockerStateManager.isBlockerEnabled() &&
               event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
    }

    private fun isTargetApplication(packageName: String): Boolean {
        return packageName == TARGET_PACKAGE_NAME
    }

    private fun isTargetAppBecomingActive(event: AccessibilityEvent, packageName: String): Boolean {
        val currentTime = System.currentTimeMillis()
        val isDuplicateEvent = (currentTime - lastBlockedTimestamp) < DUPLICATE_EVENT_THRESHOLD_MS

        if (isDuplicateEvent && lastHandledPackage == packageName) {
            return false
        }

        val isOpeningEvent = event.className != null

        if (isOpeningEvent) {
            lastBlockedTimestamp = currentTime
            lastHandledPackage = packageName
            return true
        }

        return false
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
            .setTitle(getString(R.string.blocking_dialog_error_message, TARGET_APP_DISPLAY_NAME))
            .setPositiveButton(getString(R.string.blocking_dialog_close_button)) { _, _ ->
                navigateToHomeScreen()
            }
            .setNegativeButton(getString(R.string.blocking_dialog_info_button)) { _, _ ->
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
        private const val TARGET_APP_DISPLAY_NAME = "MAX"
        private const val DIALOG_DISPLAY_DELAY_MS = 250L
        private const val DUPLICATE_EVENT_THRESHOLD_MS = 1000L
    }
}
