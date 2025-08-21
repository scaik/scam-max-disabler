package ru.scaik.scammaxdisabler

import android.content.Context
import android.os.Bundle
import android.content.Intent
import android.content.ComponentName
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.scaik.scammaxdisabler.ui.theme.ScamMaxDisablerTheme
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.core.net.toUri
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScamMaxDisablerTheme {
                MainScreen(this)
            }
        }
    }
}

@Composable
fun MainScreen(context: Context) {
    var isOn by remember { mutableStateOf(Prefs.isBlockerEnabled(context)) }
    var hasAccessibility by remember { mutableStateOf(checkAccessibilityPermission(context)) }
    var hasOverlay by remember { mutableStateOf(checkOverlayPermission(context)) }
    var batteryUnrestricted by remember { mutableStateOf(isBatteryOptimizationIgnored(context)) }
    val gradientColors = if (isOn) {
        listOf(Color(0xFF000000), Color(0xFFF60000))
    } else {
        listOf(Color(0xFF00D4FF), Color(0xFF6A00FF))
    }
    val uriHandler = LocalUriHandler.current
    val canToggle = hasAccessibility && hasOverlay
    val toggleIfAllowed: () -> Unit = {
        if (canToggle) {
            isOn = !isOn
            Prefs.setBlockerEnabled(context, isOn)
            if (isOn && hasAccessibility) {
                startWarmUpService(context)
            }
        }
    }

    // Update permission state when returning from Settings
    val lifecycleOwner = context as? LifecycleOwner
    DisposableEffect(lifecycleOwner) {
        if (lifecycleOwner == null) return@DisposableEffect onDispose { }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasAccessibility = checkAccessibilityPermission(context)
                hasOverlay = checkOverlayPermission(context)
                batteryUnrestricted = isBatteryOptimizationIgnored(context)
                if (!hasAccessibility) {
                    isOn = false
                    Prefs.setBlockerEnabled(context, false)
                }
                if (hasAccessibility && Prefs.isBlockerEnabled(context)) {
                    startWarmUpService(context)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(hasAccessibility) {
        if (!hasAccessibility && isOn) {
            isOn = false
            Prefs.setBlockerEnabled(context, false)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(size.width, 0f), // top-right
                        end = Offset(0f, size.height)   // bottom-left
                    )
                )
            }
            .then(if (canToggle) Modifier.clickable { toggleIfAllowed() } else Modifier)
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 24.dp, start = 16.dp, end = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App logo",
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .heightIn(max = 140.dp),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            )

            if (!hasAccessibility) {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠ Требуется разрешение: спец. возможности",
                            color = Color(0xFFB00020),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "В «Спец. возможности» откройте «Скачанные приложения», выберите «скаМ» и включите доступ.",
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Если переключатель недоступен (Android 13+):\n1) Откройте настройки приложения\n2) Включите ‘Разрешить ограниченные настройки’\n3) Вернитесь и включите службу в ‘Спец. возможности’.",
                            color = Color.Black,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Открыть «Спец. возможности»")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = ("package:" + context.packageName).toUri()
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Открыть настройки приложения")
                        }
                    }
                }
            }

            if (!hasOverlay) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠ Разрешение: показывать поверх других окон",
                            color = Color(0xFFB00020),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Откройте «Показ поверх других окон», выберите «скаМ» и разрешите показ.",
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    ("package:" + context.packageName).toUri()
                                ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Открыть настройки показа поверх")
                        }
                    }
                }
            }

            if (hasAccessibility && hasOverlay && !batteryUnrestricted) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.instr_title_reliability),
                            color = Color(0xFF1A1A1A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.instr_text_reliability),
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    try {
                                        val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(fallback)
                                    } catch (_: Exception) { }
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = stringResource(id = R.string.open_battery_settings))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = {
                                openAutoStartSettings(context)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = stringResource(id = R.string.open_autostart_settings))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp)
                .padding(start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (canToggle) {
                val statusTextColor = Color.White
                Text(
                    text = if (isOn) "Включено" else "Выключено",
                    fontSize = 26.sp,
                    color = statusTextColor,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                val toggleLabel = if (isOn) "Нажмите, чтобы выключить" else "Нажмите, чтобы включить"
                Text(
                    text = toggleLabel,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Как скрыть приложение?",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f),
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    uriHandler.openUri("https://hi-tech.mail.ru/review/55973-kak-skryt-lyuboe-prilozhenie-na-android/")
                }
            )
        }
    }
}

fun checkAccessibilityPermission(context: Context): Boolean {
    val resolver = context.contentResolver
    val enabledServices = Settings.Secure.getString(
        resolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: ""

    val component = ComponentName(context, AppMonitorService::class.java)
    val fullId = component.flattenToString() // ru.scaik.scammaxdisabler/ru.scaik.scammaxdisabler.AppMonitorService
    val shortId = "${context.packageName}/.AppMonitorService" // ru.scaik.scammaxdisabler/.AppMonitorService (на некоторых прошивках)

    return enabledServices.split(':').any { it == fullId || it == shortId }
}

fun checkOverlayPermission(context: Context): Boolean {
    return Settings.canDrawOverlays(context)
}

fun isBatteryOptimizationIgnored(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        pm.isIgnoringBatteryOptimizations(context.packageName)
    } else true
}

private fun startWarmUpService(context: Context) {
    try {
        val intent = Intent(context, WarmUpService::class.java)
        ContextCompat.startForegroundService(context, intent)
    } catch (_: Exception) { }
}

private fun openAutoStartSettings(context: Context) {
    val intents = listOf(
        // MIUI
        Intent().setClassName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
        // EMUI
        Intent().setClassName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
        // ColorOS (Oppo)
        Intent().setClassName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"),
        Intent().setClassName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"),
        // Vivo
        Intent().setClassName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"),
        Intent().setClassName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"),
        // OnePlus
        Intent().setClassName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"),
        // Fallback to app details
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = ("package:" + context.packageName).toUri() },
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
        } catch (_: Exception) { }
    }
}
