package ru.scaik.scammaxdisabler.domain

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import ru.scaik.scammaxdisabler.data.Prefs
import ru.scaik.scammaxdisabler.service.WarmUpService

class ServiceRunner(private val context: Context) {

    fun startWarmUp(): Result<Unit> {
        return try {
            val intent = Intent(context, WarmUpService::class.java)
            ContextCompat.startForegroundService(context, intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun canStart(): Boolean {
        return Prefs.isBlockerEnabled(context)
    }
}
