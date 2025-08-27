package ru.scaik.scammaxdisabler

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import ru.scaik.scammaxdisabler.R
import ru.scaik.scammaxdisabler.domain.AppState
import ru.scaik.scammaxdisabler.domain.PermissionValidator
import ru.scaik.scammaxdisabler.domain.ServiceRunner
import ru.scaik.scammaxdisabler.domain.StateCoordinator
import ru.scaik.scammaxdisabler.ui.theme.ScamMaxDisablerTheme

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
    val coordinator = remember { StateCoordinator(context) }
    val state by coordinator.state.collectAsState()

    LaunchedEffect(Unit) {
        coordinator.initialize()
    }

    val lifecycleOwner = context as? LifecycleOwner
    DisposableEffect(lifecycleOwner) {
        if (lifecycleOwner == null) return@DisposableEffect onDispose { }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                coordinator.refreshState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val gradientColors = if (state.blockerEnabled) {
        listOf(Color(0xFF000000), Color(0xFFF60000))
    } else {
        listOf(Color(0xFF00D4FF), Color(0xFF6A00FF))
    }

    val uriHandler = LocalUriHandler.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(size.width, 0f),
                        end = Offset(0f, size.height)
                    )
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (!state.accessibilityGranted) {
                PermissionCard(
                    title = stringResource(id = R.string.instr_title_accessibility),
                    text = stringResource(id = R.string.instr_text_accessibility),
                    buttonText = stringResource(id = R.string.open_accessibility_settings),
                    onClick = { openAccessibilitySettings(context) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!state.overlayGranted) {
                PermissionCard(
                    title = stringResource(id = R.string.instr_title_overlay),
                    text = stringResource(id = R.string.instr_text_overlay),
                    buttonText = stringResource(id = R.string.open_overlay_settings),
                    onClick = { openOverlaySettings(context) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (state.allPermissionsGranted && !state.batteryExempted) {
                PermissionCard(
                    title = stringResource(id = R.string.instr_title_reliability),
                    text = stringResource(id = R.string.instr_text_reliability),
                    buttonText = stringResource(id = R.string.open_battery_settings),
                    onClick = { openBatterySettings(context) }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            ToggleSection(
                state = state,
                onToggle = { coordinator.toggleBlocker() }
            )

            state.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(12.dp))
                ErrorCard(
                    message = error,
                    onDismiss = { coordinator.clearError() }
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

@Composable
private fun PermissionCard(
    title: String,
    text: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                color = Color.Black,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onClick,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = buttonText)
            }
        }
    }
}

@Composable
private fun ToggleSection(
    state: AppState,
    onToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.canToggle) {
            Text(
                text = if (state.blockerEnabled) "Включено" else "Выключено",
                fontSize = 26.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            val toggleLabel = if (state.blockerEnabled) "Нажмите, чтобы выключить" else "Нажмите, чтобы включить"
            Text(
                text = toggleLabel,
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    Box(
        modifier = Modifier
            .size(200.dp)
            .background(
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(100.dp)
            )
            .border(
                width = 4.dp,
                color = Color.White,
                shape = RoundedCornerShape(100.dp)
            )
            .clickable(enabled = state.canToggle) { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (state.canToggle) {
                if (state.blockerEnabled) "ВЫКЛ" else "ВКЛ"
            } else {
                "—"
            },
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = Color.Red,
            modifier = Modifier
                .padding(12.dp)
                .clickable { onDismiss() }
        )
    }
}

private fun openAccessibilitySettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        openSettings(context)
    }
}

private fun openOverlaySettings(context: Context) {
    try {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
        } else {
            Intent(Settings.ACTION_SETTINGS)
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        openSettings(context)
    }
}

private fun openBatterySettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        openSettings(context)
    }
}

private fun openSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}

private fun openAutoStartSettings(context: Context) {
    val intents = listOf(
        Intent().setClassName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
        Intent().setClassName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"),
        Intent().setClassName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
        Intent().setClassName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"),
        Intent().setClassName("com.android.settings", "com.android.settings.Settings\$AdvancedAppSettingsActivity")
    )

    for (intent in intents) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return
        } catch (_: Exception) {
            continue
        }
    }
    openSettings(context)
}
