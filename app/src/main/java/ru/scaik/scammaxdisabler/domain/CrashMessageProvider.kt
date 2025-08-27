package ru.scaik.scammaxdisabler.domain

import android.content.Context
import ru.scaik.scammaxdisabler.R

class CrashMessageProvider(private val context: Context) {

    fun getCrashDialogTitle(): String =
        context.getString(R.string.crash_dialog_title)

    fun getCloseButtonText(): String =
        context.getString(R.string.crash_dialog_close)

    fun getInfoButtonText(): String =
        context.getString(R.string.crash_dialog_info)
}
