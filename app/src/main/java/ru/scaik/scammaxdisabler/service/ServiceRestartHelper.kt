package ru.scaik.scammaxdisabler.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.work.*
import java.util.concurrent.TimeUnit
import ru.scaik.scammaxdisabler.receiver.WarmUpReceiver

class ServiceRestartHelper(private val context: Context) {

    private val applicationContext = context.applicationContext

    fun schedulePeriodicServiceCheck() {
        scheduleWithWorkManager()
        scheduleWithJobScheduler()
        scheduleWithAlarmManager()
    }

    fun scheduleImmediateRestart() {
        triggerWarmUpService()
        scheduleDelayedRestartWithAlarmManager()
    }

    private fun scheduleWithWorkManager() {
        val workRequest = PeriodicWorkRequestBuilder<ServiceCheckWorker>(
            WORK_MANAGER_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        )
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                WORK_MANAGER_TAG,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
    }

    private fun scheduleWithJobScheduler() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return

        val jobScheduler = applicationContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler
            ?: return

        val jobInfo = JobInfo.Builder(JOB_ID_SERVICE_CHECK, ComponentName(applicationContext, ServiceCheckJob::class.java))
            .setPeriodic(JOB_SCHEDULER_INTERVAL_MILLIS)
            .setPersisted(true)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
            .build()

        jobScheduler.schedule(jobInfo)
    }

    private fun scheduleWithAlarmManager() {
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            ?: return

        val intent = Intent(applicationContext, WarmUpReceiver::class.java).apply {
            action = ACTION_SERVICE_CHECK
        }

        val pendingIntent = createPendingIntentForAlarm(intent)

        val triggerTime = SystemClock.elapsedRealtime() + ALARM_MANAGER_INTERVAL_MILLIS

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                alarmManager.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            else -> {
                alarmManager.set(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        }
    }

    private fun scheduleDelayedRestartWithAlarmManager() {
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            ?: return

        val intent = Intent(applicationContext, WarmUpReceiver::class.java).apply {
            action = ACTION_IMMEDIATE_RESTART
        }

        val pendingIntent = createPendingIntentForAlarm(intent)

        val triggerTime = SystemClock.elapsedRealtime() + IMMEDIATE_RESTART_DELAY_MILLIS

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    private fun createPendingIntentForAlarm(intent: Intent): PendingIntent {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return PendingIntent.getBroadcast(
            applicationContext,
            REQUEST_CODE_ALARM,
            intent,
            flags
        )
    }

    private fun triggerWarmUpService() {
        try {
            val intent = Intent(applicationContext, WarmUpService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationContext.startForegroundService(intent)
            } else {
                applicationContext.startService(intent)
            }
        } catch (e: Exception) {
            // Service start might fail in some conditions, other mechanisms will retry
        }
    }

    companion object {
        private const val WORK_MANAGER_TAG = "service_check_work"
        private const val WORK_MANAGER_INTERVAL_MINUTES = 15L

        private const val JOB_ID_SERVICE_CHECK = 1001
        private const val JOB_SCHEDULER_INTERVAL_MILLIS = 15 * 60 * 1000L

        private const val ALARM_MANAGER_INTERVAL_MILLIS = 30 * 60 * 1000L
        private const val IMMEDIATE_RESTART_DELAY_MILLIS = 2000L

        private const val REQUEST_CODE_ALARM = 2001

        const val ACTION_SERVICE_CHECK = "ru.scaik.scammaxdisabler.ACTION_SERVICE_CHECK"
        const val ACTION_IMMEDIATE_RESTART = "ru.scaik.scammaxdisabler.ACTION_IMMEDIATE_RESTART"
    }
}

class ServiceCheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val intent = Intent(applicationContext, WarmUpService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationContext.startForegroundService(intent)
            } else {
                applicationContext.startService(intent)
            }
        } catch (e: Exception) {
            return Result.retry()
        }
        return Result.success()
    }
}

class ServiceCheckJob : android.app.job.JobService() {

    override fun onStartJob(params: android.app.job.JobParameters?): Boolean {
        val intent = Intent(this, WarmUpService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            // Silently fail, will retry on next schedule
        }
        return false
    }

    override fun onStopJob(params: android.app.job.JobParameters?): Boolean {
        return true
    }
}
