package ru.scaik.scammaxdisabler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class WarmUpReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context, intent: Intent) {
		when (intent.action) {
			Intent.ACTION_BOOT_COMPLETED,
			Intent.ACTION_MY_PACKAGE_REPLACED -> {
				val i = Intent(context, WarmUpService::class.java)
				ContextCompat.startForegroundService(context, i)
			}
		}
	}
}


