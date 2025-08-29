package ru.scaik.scammaxdisabler.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import ru.scaik.scammaxdisabler.service.WarmUpService

class WarmUpReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (!isRelevantAction(intent.action)) return

        startWarmUpService(context)
    }

    private fun isRelevantAction(action: String?): Boolean {
        return action in RELEVANT_ACTIONS
    }

    private fun startWarmUpService(context: Context) {
        val serviceIntent = Intent(context, WarmUpService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    companion object {
        private val RELEVANT_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "ru.scaik.scammaxdisabler.ACTION_SERVICE_CHECK",
            "ru.scaik.scammaxdisabler.ACTION_IMMEDIATE_RESTART"
        )
    }
}
