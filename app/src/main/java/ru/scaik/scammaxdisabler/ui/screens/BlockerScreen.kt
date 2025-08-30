package ru.scaik.scammaxdisabler.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Layers
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import ru.scaik.scammaxdisabler.MainActivity
import ru.scaik.scammaxdisabler.R
import ru.scaik.scammaxdisabler.service.WarmUpService
import ru.scaik.scammaxdisabler.ui.components.CircularPowerIndicator
import ru.scaik.scammaxdisabler.ui.components.CompactPermissionCard
import ru.scaik.scammaxdisabler.ui.components.GlassmorphicStatusCard
import ru.scaik.scammaxdisabler.ui.components.gradientBackground
import ru.scaik.scammaxdisabler.ui.theme.CrimsonBright
import ru.scaik.scammaxdisabler.ui.theme.TextSecondaryDark
import ru.scaik.scammaxdisabler.ui.theme.TextSecondaryLight

@Composable
fun BlockerScreen(context: Context, modifier: Modifier = Modifier) {
    val activity = context as MainActivity
    val blockerStateManager = activity.blockerStateManager

    var isBlockerEnabled by remember { mutableStateOf(blockerStateManager.isBlockerEnabled()) }
    var hasAccessibilityPermission by remember {
        mutableStateOf(checkAccessibilityPermission(context))
    }
    var hasOverlayPermission by remember { mutableStateOf(checkOverlayPermission(context)) }
    var isBatteryOptimized by remember { mutableStateOf(isBatteryOptimizationIgnored(context)) }

    val canToggleBlocker = hasAccessibilityPermission && hasOverlayPermission

    ObserveLifecycleEvents(
        context = context,
        onResume = {
            hasAccessibilityPermission = checkAccessibilityPermission(context)
            hasOverlayPermission = checkOverlayPermission(context)
            isBatteryOptimized = isBatteryOptimizationIgnored(context)

            if (!hasAccessibilityPermission) {
                isBlockerEnabled = false
                blockerStateManager.setBlockerEnabled(false)
            }
            if (hasAccessibilityPermission && blockerStateManager.isBlockerEnabled()) {
                startWarmUpService(context)
            }
        }
    )

    LaunchedEffect(hasAccessibilityPermission) {
        if (!hasAccessibilityPermission && isBlockerEnabled) {
            isBlockerEnabled = false
            blockerStateManager.setBlockerEnabled(false)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(modifier = modifier.fillMaxSize()) {
            val gradientColors =
                if (isBlockerEnabled) {
                    listOf(
                        Color(0xFF2A1A1A), // Dark reddish charcoal
                        Color(0xFF3D2222), // Medium reddish charcoal
                        Color(0xFF660000) // Darker red
                    )
                } else {
                    listOf(
                        Color(0xFFB8E0F5), // Lighter sky blue
                        Color(0xFF87CEEB), // Light sky blue
                        Color(0xFFB8A9DC) // Lighter slate blue
                    )
                }

            // Gradient background fills entire screen including safe zones
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .gradientBackground(colors = gradientColors, angle = 135f)
                        .clickable(
                            enabled = canToggleBlocker,
                            interactionSource =
                                remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            isBlockerEnabled = !isBlockerEnabled
                            blockerStateManager.setBlockerEnabled(isBlockerEnabled)
                            if (isBlockerEnabled && hasAccessibilityPermission) {
                                startWarmUpService(context)
                            }
                        }
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .systemBarsPadding()
                        .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedAppLogo(
                    isActive = isBlockerEnabled,
                    modifier = Modifier.heightIn(max = 360.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularPowerIndicator(
                        isActive = isBlockerEnabled,
                        enabled = canToggleBlocker,
                        size = 140.dp
                    )
                }
            }

            val allPermissionsGranted =
                hasAccessibilityPermission && hasOverlayPermission && isBatteryOptimized

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .systemBarsPadding()
                        .padding(bottom = 60.dp)
            ) {
                if (allPermissionsGranted) {
                    GlassmorphicStatusCard(
                        isActive = isBlockerEnabled,
                        statusText =
                            if (isBlockerEnabled) "Блокировка активна"
                            else "Блокировка отключена",
                        subtitle =
                            if (isBlockerEnabled) {
                                "MAX заблокирован"
                            } else {
                                "Нажмите для активации блокировки"
                            },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .height(100.dp)
                    )
                } else {
                    PermissionCards(
                        context = context,
                        hasAccessibilityPermission = hasAccessibilityPermission,
                        hasOverlayPermission = hasOverlayPermission,
                        isBatteryOptimized = isBatteryOptimized,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Bottom hint text
            androidx.compose.material3.Text(
                text =
                    if (canToggleBlocker) {
                        "Нажмите в любом месте для переключения"
                    } else {
                        "Предоставьте необходимые разрешения"
                    },
                fontSize = 12.sp,
                color =
                    if (isBlockerEnabled) {
                        TextSecondaryDark.copy(alpha = 0.6f)
                    } else {
                        TextSecondaryLight.copy(alpha = 0.6f)
                    },
                fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .systemBarsPadding()
                //                    Modifier.align(Alignment.BottomCenter)
                //                            .padding(bottom = 16.dp)
                //                            .padding(horizontal = 32.dp)
            )
        }
    } // Close content Box with systemBarsPadding
} // Close main outer Box

@Composable
private fun AnimatedAppLogo(isActive: Boolean, modifier: Modifier = Modifier) {
    val alpha by
    androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isActive) 1f else 0.9f,
        animationSpec = androidx.compose.animation.core.tween(600),
        label = "logoAlpha"
    )

    val glowAlpha by
    androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(800),
        label = "glowAlpha"
    )

    val shadowAlpha by
    androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (!isActive) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(600),
        label = "shadowAlpha"
    )

    val gradientAlpha by
    androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (!isActive) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(600),
        label = "gradientAlpha"
    )

    Box(modifier = modifier.alpha(alpha), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center,
                colorFilter = ColorFilter.tint(CrimsonBright),
                modifier = Modifier
                    .blur(64.dp)
                    .alpha(glowAlpha)
            )

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center,
                colorFilter = ColorFilter.tint(Color(0xFF999999)),
                modifier = Modifier
                    .blur(20.dp)
                    .alpha(shadowAlpha * 0.3f)
            )

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            drawContent()
                            if (gradientAlpha > 0f) {
                                drawRect(
                                    brush =
                                        Brush.linearGradient(
                                            0.0f to
                                                    Color(0xFF6B00CC)
                                                        .copy(
                                                            alpha =
                                                                gradientAlpha
                                                        ), // Brighter Violet
                                            0.4f to
                                                    Color(0xFF6B00CC)
                                                        .copy(
                                                            alpha =
                                                                gradientAlpha
                                                        ), // Brighter Violet
                                            // extended
                                            0.6f to
                                                    Color(0xFF00BBFF)
                                                        .copy(
                                                            alpha =
                                                                gradientAlpha
                                                        ), // Brighter Blue
                                            1.0f to
                                                    Color(0xFF00BBFF)
                                                        .copy(
                                                            alpha =
                                                                gradientAlpha
                                                        ), // Brighter Blue
                                            // extended
                                            start = Offset(size.width, 0f),
                                            end = Offset(0f, size.height)
                                        ),
                                    blendMode =
                                        androidx.compose.ui.graphics.BlendMode.SrcAtop
                                )
                            }
                        },
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center
            )
        }
    }
}

@Composable
private fun PermissionCards(
    context: Context,
    hasAccessibilityPermission: Boolean,
    hasOverlayPermission: Boolean,
    isBatteryOptimized: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AnimatedVisibility(
            visible = !hasAccessibilityPermission,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            CompactPermissionCard(
                title = "Специальные возможности",
                description = "Требуется для обнаружения запуска MAX",
                icon = Icons.Filled.Accessibility,
                isGranted = hasAccessibilityPermission,
                instructions =
                    buildAccessibilityInstructions(),
                actions =
                    listOf(
                        "Открыть «Спец. возможности»" to
                                {
                                    openAccessibilitySettings(context)
                                },
                        "Открыть настройки приложения" to { openAppSettings(context) }
                    )
            )
        }

        AnimatedVisibility(
            visible = !hasOverlayPermission,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            CompactPermissionCard(
                title = "Показ поверх других окон",
                description = "Требуется для блокировки экрана MAX",
                icon = Icons.Filled.Layers,
                isGranted = hasOverlayPermission,
                instructions =
                    "Откройте «Показ поверх других окон», найдите приложение «скаМ» в списке и разрешите показ.",
                actions = listOf("Открыть настройки" to { openOverlaySettings(context) })
            )
        }

        AnimatedVisibility(
            visible = !isBatteryOptimized,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            CompactPermissionCard(
                title = "Оптимизация батареи",
                description = "Отключите для надежной работы блокировки",
                icon = Icons.Filled.BatteryChargingFull,
                isGranted = isBatteryOptimized,
                instructions = buildBatteryOptimizationInstructions(),
                actions =
                    listOf(
                        "Открыть настройки автозапуска" to
                                {
                                    openAutoloadSettings(context)
                                },
                        "Открыть настройки батареи" to
                                {
                                    openBatteryOptimizationSettings(context)
                                },
                        "Инструкции для Tecno/Infinix" to
                                {
                                    openTecnoInfinixGuide(context)
                                }
                    )
            )
        }
    }
}

@Composable
private fun ObserveLifecycleEvents(context: Context, onResume: () -> Unit) {
    val lifecycleOwner = context as? LifecycleOwner

    DisposableEffect(lifecycleOwner) {
        if (lifecycleOwner == null) return@DisposableEffect onDispose {}

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onResume()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

private fun checkAccessibilityPermission(context: Context): Boolean {
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

private fun checkOverlayPermission(context: Context): Boolean {
    return Settings.canDrawOverlays(context)
}

private fun isBatteryOptimizationIgnored(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}

private fun startWarmUpService(context: Context) {
    try {
        val serviceIntent = Intent(context, WarmUpService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun openAccessibilitySettings(context: Context) {
    val intent =
        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    context.startActivity(intent)
}

private fun openAppSettings(context: Context) {
    val intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = ("package:" + context.packageName).toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    context.startActivity(intent)
}

private fun openOverlaySettings(context: Context) {
    val intent =
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            ("package:" + context.packageName).toUri()
        )
            .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    context.startActivity(intent)
}

private fun openBatteryOptimizationSettings(context: Context) {
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
        } catch (_: Exception) {
        }
    }
}

private fun openAutoloadSettings(context: Context) {
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
        } catch (_: Exception) {
        }
    }
}

private fun buildAutoloadInstructions(): String {
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

private fun buildAccessibilityInstructions(): String {
    return "В «Спец. возможности» откройте «Скачанные приложения», выберите «скаМ» и включите доступ.\n\n" +
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                "Если переключатель недоступен (Android 13+):\n" +
                        "1) Откройте настройки приложения\n" +
                        "2) Включите 'Разрешить ограниченные настройки'\n" +
                        "3) Вернитесь и включите службу в 'Спец. возможности'"
            } else "")
}

private fun openTecnoInfinixGuide(context: Context) {
    val intent =
        Intent(
            Intent.ACTION_VIEW,
            "https://github.com/scaik/scam-max-disabler/issues/15".toUri()
        )
            .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    context.startActivity(intent)
}

private fun buildBatteryOptimizationInstructions(): String {
    return "Для надежной работы блокировки рекомендуется:\n" +
            "1. Отключить оптимизацию батареи для приложения\n" +
            "2. " + buildAutoloadInstructions() + "\n" +
            "3. Закрепить приложение в памяти"
}
