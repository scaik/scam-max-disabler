package ru.scaik.scammaxdisabler

import android.content.Context
import androidx.core.content.edit

object Prefs {
    private const val PREFS_NAME = "scam_service_prefs"
    private const val KEY_ENABLE_BLOCKER = "scam_max_blocker_enabled"

    fun isBlockerEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLE_BLOCKER, true)
    }

    fun setBlockerEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit(commit = true) {
                putBoolean(KEY_ENABLE_BLOCKER, enabled)
            }
    }
}
