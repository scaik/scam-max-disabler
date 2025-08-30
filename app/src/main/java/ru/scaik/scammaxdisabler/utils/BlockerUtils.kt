package ru.scaik.scammaxdisabler.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import ru.scaik.scammaxdisabler.service.WarmUpService

object BlockerUtils {

    fun checkAccessibilityPermission(context: Context): Boolean {
        val enabledServices =
                Settings.Secure.getString(
                        context.contentResolver,
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )
                        ?: ""

        val possibleServiceIds =
                listOf(
                        "${context.packageName}/ru.scaik.scammaxdisabler.service.AppMonitorService",
                        "${context.packageName}/.service.AppMonitorService",
                        "ru.scaik.scammaxdisabler/ru.scaik.scammaxdisabler.service.AppMonitorService",
                        "ru.scaik.scammaxdisabler/.service.AppMonitorService"
                )

        return possibleServiceIds.any { serviceId ->
            enabledServices.split(':').any { it == serviceId }
        }
    }

    fun checkOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun startWarmUpService(context: Context) {
        try {
            val serviceIntent = Intent(context, WarmUpService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openAccessibilitySettings(context: Context) {
        val intent =
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
        context.startActivity(intent)
    }

    fun openAppSettings(context: Context) {
        val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = ("package:" + context.packageName).toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
        context.startActivity(intent)
    }

    fun openOverlaySettings(context: Context) {
        val intent =
                Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                ("package:" + context.packageName).toUri()
                        )
                        .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(intent)
    }

    fun openBatteryOptimizationSettings(context: Context) {
        try {
            val intent =
                    Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val fallback =
                        Intent(Settings.ACTION_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                context.startActivity(fallback)
            } catch (_: Exception) {}
        }
    }

    fun openAutoloadSettings(context: Context) {
        val intents =
                listOf(
                        // MIUI
                        Intent().setClassName(
                                        "com.miui.securitycenter",
                                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                                ),
                        // EMUI
                        Intent().setClassName(
                                        "com.huawei.systemmanager",
                                        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                                ),
                        // ColorOS (Oppo)
                        Intent().setClassName(
                                        "com.coloros.safecenter",
                                        "com.coloros.safecenter.startupapp.StartupAppListActivity"
                                ),
                        Intent().setClassName(
                                        "com.oppo.safe",
                                        "com.oppo.safe.permission.startup.StartupAppListActivity"
                                ),
                        // Vivo
                        Intent().setClassName(
                                        "com.iqoo.secure",
                                        "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                                ),
                        Intent().setClassName(
                                        "com.vivo.permissionmanager",
                                        "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                                ),
                        // OnePlus
                        Intent().setClassName(
                                        "com.oneplus.security",
                                        "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
                                ),
                        // Fallback to app details
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = ("package:" + context.packageName).toUri()
                        },
                        // Final fallback: settings
                        Intent(Settings.ACTION_SETTINGS)
                )

        for (i in intents) {
            try {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (i.resolveActivity(context.packageManager) != null) {
                    context.startActivity(i)
                    return
                }
            } catch (_: Exception) {}
        }
    }

    fun openTecnoInfinixGuide(context: Context) {
        val intent =
                Intent(
                                Intent.ACTION_VIEW,
                                "https://github.com/scaik/scam-max-disabler/issues/15".toUri()
                        )
                        .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(intent)
    }

    fun buildAutoloadInstructions(): String {
        val manufacturer = Build.MANUFACTURER.lowercase()

        return when {
            manufacturer.contains("xiaomi") ->
                    "Найдите приложение «скаМ» в списке и включите автозапуск. На MIUI также проверьте настройки «Блокировка приложений»."
            manufacturer.contains("oppo") ->
                    "В разделе «Автозапуск» найдите приложение «скаМ» и разрешите автозапуск."
            manufacturer.contains("vivo") ->
                    "Найдите «скаМ» в списке приложений и включите опцию «Автозапуск»."
            manufacturer.contains("huawei") || manufacturer.contains("honor") ->
                    "В менеджере автозапуска найдите приложение «скаМ» и переключите его в положение «Включено»."
            manufacturer.contains("samsung") ->
                    "В разделе оптимизации батареи найдите «скаМ» и отключите оптимизацию или разрешите работу в фоне."
            manufacturer.contains("oneplus") ->
                    "Найдите «скаМ» в списке автозапуска и разрешите запуск приложения."
            else ->
                    "Найдите настройки автозапуска или автозагрузки в системных настройках и разрешите запуск приложения «скаМ» при включении устройства."
        }
    }

    fun buildAccessibilityInstructions(): String {
        return "В «Спец. возможности» откройте «Скачанные приложения», выберите «скаМ» и включите доступ.\n\n" +
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    "Если переключатель недоступен (Android 13+):\n" +
                            "1. Откройте настройки приложения\n" +
                            "2. Включите 'Разрешить ограниченные настройки'\n" +
                            "3. Вернитесь и включите службу в 'Спец. возможности'"
                } else "")
    }

    fun buildBatteryOptimizationInstructions(): String {
        return "Для надежной работы блокировки рекомендуется:\n" +
                "1. " +
                buildAutoloadInstructions() +
                "\n" +
                "2. Отключите оптимизацию батареи для приложения\n" +
                "3. Закрепите приложение в памяти"
    }

    // Package management functions for installation blocking
    private const val TARGET_PACKAGE_NAME = "ru.oneme.app"
    private const val DUMMY_APK_FILE_NAME = "dummy_max.apk"
    private const val DUMMY_APK_ASSET_PATH = "dummy_max.apk"
    private const val TAG = "BlockerUtils"

    fun canRequestPackageInstalls(context: Context): Boolean {
        return context.packageManager.canRequestPackageInstalls()
    }

    fun requestInstallPermission(context: Context) {
        val intent =
                Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = ("package:" + context.packageName).toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
        context.startActivity(intent)
    }

    fun isTargetPackageInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(TARGET_PACKAGE_NAME, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun extractDummyApkFromAssets(context: Context): File {

        val destinationFile = File(context.cacheDir, DUMMY_APK_FILE_NAME)

        try {
            // Check if asset exists first
            val assetList = context.assets.list("") ?: emptyArray()
            if (!assetList.contains(DUMMY_APK_FILE_NAME)) {
                Log.e(
                        TAG,
                        "Asset $DUMMY_APK_FILE_NAME not found in assets. Available assets: ${assetList.joinToString()}"
                )
                throw IllegalStateException("Dummy APK asset not found")
            }

            context.assets.open(DUMMY_APK_ASSET_PATH).use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    val bytesWritten = inputStream.copyTo(outputStream)

                    // Validate extracted file
                    if (bytesWritten == 0L) {
                        Log.e(TAG, "Extracted APK file is empty")
                        throw IllegalStateException("Extracted APK file is empty")
                    }

                    if (!destinationFile.exists() || destinationFile.length() == 0L) {
                        Log.e(TAG, "Extracted APK file validation failed")
                        throw IllegalStateException("Extracted APK file validation failed")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract APK from assets", e)
            // Clean up failed extraction
            if (destinationFile.exists()) {
                destinationFile.delete()
            }
            throw e
        }

        return destinationFile
    }

    fun installDummyApkWithCallback(context: Context, onComplete: (Boolean) -> Unit) {

        kotlinx.coroutines.GlobalScope.launch {
            try {
                // Check if the real package is installed and uninstall it first
                if (isTargetPackageInstalled(context)) {
                    Log.d(TAG, "Real package is installed, uninstalling it first")

                    // Uninstall the real package first
                    val uninstallIntent =
                            Intent(Intent.ACTION_DELETE).apply {
                                data = "package:$TARGET_PACKAGE_NAME".toUri()
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }

                    context.startActivity(uninstallIntent)

                    // Wait for uninstallation to complete
                    var uninstallAttempts = 0
                    val maxUninstallAttempts = 30

                    while (uninstallAttempts < maxUninstallAttempts) {
                        kotlinx.coroutines.delay(1000)
                        uninstallAttempts++

                        if (!isTargetPackageInstalled(context)) {
                            Log.d(TAG, "Real package uninstalled successfully")
                            break
                        }
                    }

                    // If still installed after timeout, fail
                    if (isTargetPackageInstalled(context)) {
                        Log.e(TAG, "Failed to uninstall real package - user may have cancelled")
                        onComplete(false)
                        return@launch
                    }
                }

                // Extract APK from assets
                val apkFile =
                        try {
                            extractDummyApkFromAssets(context)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to extract APK from assets", e)
                            onComplete(false)
                            return@launch
                        }

                // Use FileProvider to get a content URI for the APK
                val apkUri =
                        FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                apkFile
                        )

                // Create intent to launch system installation dialog
                val intent =
                        Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(apkUri, "application/vnd.android.package-archive")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }

                context.startActivity(intent)

                // Start background monitoring
                var attempts = 0
                val maxAttempts = 60 // 60 seconds maximum wait time

                while (attempts < maxAttempts) {
                    kotlinx.coroutines.delay(1000) // Wait 1 second between checks
                    attempts++

                    val isInstalled = isTargetPackageInstalled(context)

                    if (isInstalled) {
                        Log.d(TAG, "Package successfully installed after ${attempts} seconds")
                        onComplete(true)
                        return@launch
                    }
                }

                // Timeout - check final status
                Log.w(TAG, "Installation monitoring timeout")
                val finalCheck = isTargetPackageInstalled(context)
                onComplete(finalCheck)
            } catch (e: Exception) {
                Log.e(TAG, "Exception during APK installation", e)
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    suspend fun uninstallTargetPackage(context: Context): Boolean {

        try {
            // Check if package is installed
            if (!isTargetPackageInstalled(context)) {
                return true // Already uninstalled
            }

            // Use system uninstall dialog
            val intent =
                    Intent(Intent.ACTION_DELETE).apply {
                        data = "package:$TARGET_PACKAGE_NAME".toUri()
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

            context.startActivity(intent)

            // Return true immediately after launching dialog
            // The BlockerScreen will handle verification separately
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Exception during package uninstallation", e)
            e.printStackTrace()
            return false
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun uninstallTargetPackageWithCallback(context: Context, onComplete: (Boolean) -> Unit) {

        try {
            // Check if package is installed
            if (!isTargetPackageInstalled(context)) {
                onComplete(true)
                return
            }

            // Use system uninstall dialog
            val intent =
                    Intent(Intent.ACTION_DELETE).apply {
                        data = "package:$TARGET_PACKAGE_NAME".toUri()
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

            context.startActivity(intent)

            // Start background monitoring
            kotlinx.coroutines.GlobalScope.launch {
                var attempts = 0
                val maxAttempts = 30 // 30 seconds maximum wait time

                while (attempts < maxAttempts) {
                    kotlinx.coroutines.delay(1000) // Wait 1 second between checks
                    attempts++

                    val stillInstalled = isTargetPackageInstalled(context)

                    if (!stillInstalled) {
                        Log.d(TAG, "Package successfully uninstalled after $attempts seconds")
                        onComplete(true)
                        return@launch
                    }
                }

                // Timeout - check final status
                Log.w(TAG, "Uninstallation monitoring timeout")
                val finalCheck = isTargetPackageInstalled(context)
                onComplete(!finalCheck)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during package uninstallation", e)
            e.printStackTrace()
            onComplete(false)
        }
    }
}
