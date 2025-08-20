package ru.scaik.scammaxdisabler

import android.content.Context
import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.scaik.scammaxdisabler.ui.theme.ScamMaxDisablerTheme
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

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
    val gradientColors = if (isOn) {
        listOf(Color(0xFF000000), Color(0xFFF60000))
    } else {
        listOf(Color(0xFF00D4FF), Color(0xFF6A00FF))
    }

    // Update permission state when returning from Settings
    val lifecycleOwner = context as? LifecycleOwner
    DisposableEffect(lifecycleOwner) {
        if (lifecycleOwner == null) return@DisposableEffect onDispose { }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasAccessibility = checkAccessibilityPermission(context)
                if (!hasAccessibility) {
                    isOn = false
                    Prefs.setBlockerEnabled(context, false)
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App logo",
                modifier = Modifier.height(260.dp)
            )

            if (!hasAccessibility) {
                Spacer(modifier = Modifier.height(12.dp))
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
                            text = "Откройте настройки и включите службу спец. возможностей приложения.",
                            color = Color.Black,
                            fontSize = 14.sp
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
                            Text(text = "Открыть настройки")
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            val buttonContainerColor = if (isOn) Color(0xFFD32F2F) else Color(0xFF512DA8)
            Button(
                onClick = {
                    isOn = !isOn
                    Prefs.setBlockerEnabled(context, isOn)
                },
                enabled = hasAccessibility,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonContainerColor
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .width(240.dp)
                    .height(72.dp)
            ) {
                Text(
                    text = if (isOn) "ВЫКЛЮЧИТЬ" else "ВКЛЮЧИТЬ",
                    fontSize = 22.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

fun checkAccessibilityPermission(context: Context): Boolean {
    val accessibilityEnabled = try {
        Settings.Secure.getInt(
            context.contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED
        )
    } catch (e: Settings.SettingNotFoundException) {
        0
    }
    return accessibilityEnabled == 1
}
