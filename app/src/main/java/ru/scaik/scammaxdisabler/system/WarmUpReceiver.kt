package ru.scaik.scammaxdisabler.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.scaik.scammaxdisabler.domain.PermissionValidator
import ru.scaik.scammaxdisabler.domain.ServiceRunner

class WarmUpReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_USER_UNLOCKED,
            Intent.ACTION_SCREEN_ON -> {
                startServiceIfNeeded(context)
            }
        }
    }

    private fun startServiceIfNeeded(context: Context) {
        val serviceRunner = ServiceRunner(context)
        val permissionValidator = PermissionValidator(context)

        if (serviceRunner.canStart() && permissionValidator.hasAccessibility()) {
            serviceRunner.startWarmUp()
        }
    }
}
