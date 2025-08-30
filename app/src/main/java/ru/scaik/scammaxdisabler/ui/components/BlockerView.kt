package ru.scaik.scammaxdisabler.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.scaik.scammaxdisabler.R
import ru.scaik.scammaxdisabler.ui.theme.CrimsonBright
import ru.scaik.scammaxdisabler.ui.theme.SkyBlueAccent
import ru.scaik.scammaxdisabler.ui.theme.TextSecondaryDark
import ru.scaik.scammaxdisabler.ui.theme.TextSecondaryLight

data class Permission(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isGranted: Boolean,
    val instructions: String,
    val actions: List<Pair<String, () -> Unit>>
)

data class BlockerViewState(
    val isBlockerEnabled: Boolean,
    val permissions: List<Permission>,
    val canToggleBlocker: Boolean,
    val onToggleBlocker: () -> Unit,
    val isInstallationBlocking: Boolean = false,
    val onSwitchView: () -> Unit = {},
    val powerButtonIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val isOperationInProgress: Boolean = false
)

@Composable
fun BlockerView(state: BlockerViewState, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(modifier = modifier.fillMaxSize()) {
            val gradientColors = if (state.isBlockerEnabled) {
                // Both modes use same active colors (red)
                listOf(
                    Color(0xFF2A1A1A), // Dark reddish charcoal
                    Color(0xFF3D2222), // Medium reddish charcoal
                    Color(0xFF660000) // Darker red
                )
            } else {
                if (state.isInstallationBlocking) {
                    listOf(
                        Color(0xFFB8F5E0), // Lighter mint green
                        Color(0xFF87EBCE), // Light mint green
                        Color(0xFFB8DCDC) // Lighter teal
                    )
                } else {
                    listOf(
                        Color(0xFFB8E0F5), // Lighter sky blue
                        Color(0xFF87CEEB), // Light sky blue
                        Color(0xFFB8A9DC) // Lighter slate blue
                    )
                }
            }

            // Gradient background fills entire screen including safe zones
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .gradientBackground(colors = gradientColors, angle = 135f)
                    .clickable(
                        enabled = state.canToggleBlocker,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { state.onToggleBlocker() })

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .systemBarsPadding()
                    .padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mode switch button (Active ↔ Installation Blocking)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    androidx.compose.material3.IconButton(
                        onClick = state.onSwitchView, enabled = !state.isBlockerEnabled
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = if (state.isBlockerEnabled) {
                                TextSecondaryDark.copy(alpha = 0.3f)
                            } else {
                                SkyBlueAccent
                            }
                        )
                    }
                }

                AnimatedAppLogo(
                    isActive = state.isBlockerEnabled, modifier = Modifier.heightIn(max = 300.dp)
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
                        isActive = state.isBlockerEnabled,
                        enabled = state.canToggleBlocker,
                        size = 140.dp,
                        customIcon = state.powerButtonIcon
                    )
                }
            }

            val allPermissionsGranted = state.permissions.all { it.isGranted }
            val showPermissions = state.permissions.isNotEmpty() && !state.isInstallationBlocking

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .systemBarsPadding()
                    .padding(bottom = 60.dp)
            ) {
                if (allPermissionsGranted || state.isInstallationBlocking) {
                    GlassmorphicStatusCard(
                        isActive = state.isBlockerEnabled,
                        statusText = if (state.isOperationInProgress && state.isInstallationBlocking) {
                            if (state.isBlockerEnabled) "Удаление MAX..."
                            else "Установка MAX..."
                        } else if (state.isBlockerEnabled) {
                            if (state.isInstallationBlocking) "MAX установлен"
                            else "Активная блокировка включена"
                        } else {
                            if (state.isInstallationBlocking) "MAX не установлен"
                            else "Активная блокировка отключена"
                        },
                        subtitle = if (state.isOperationInProgress && state.isInstallationBlocking) {
                            "Пожалуйста, подождите..."
                        } else if (state.isBlockerEnabled) {
                            if (state.isInstallationBlocking) "Нажмите для удаления"
                            else "MAX заблокирован"
                        } else {
                            if (state.isInstallationBlocking) "Нажмите для установки"
                            else "Нажмите для активации блокировки"
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(100.dp)
                    )
                } else if (showPermissions) {
                    PermissionCards(state = state, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }

            // Bottom hint text
            Text(
                text = when {
                    state.isOperationInProgress && state.isInstallationBlocking -> {
                        "Выполняется операция с пакетом..."
                    }

                    state.isBlockerEnabled -> {
                        if (state.isInstallationBlocking) {
                            "MAX установлен • Переключение режимов недоступно"
                        } else {
                            "Активная блокировка включена • Переключение режимов недоступно"
                        }
                    }

                    !state.canToggleBlocker && !state.isInstallationBlocking -> {
                        "Предоставьте необходимые разрешения"
                    }

                    state.isInstallationBlocking -> {
                        "Нажмите кнопку ↔ для переключения в режим активной блокировки"
                    }

                    else -> {
                        "Нажмите кнопку ↔ для переключения в режим блокировки установки"
                    }
                },
                fontSize = 12.sp,
                color = if (state.isBlockerEnabled) {
                    TextSecondaryDark.copy(alpha = 0.6f)
                } else {
                    TextSecondaryLight.copy(alpha = 0.6f)
                },
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .systemBarsPadding()
            )
        }
    }
}

@Composable
private fun AnimatedAppLogo(isActive: Boolean, modifier: Modifier = Modifier) {
    val alpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.9f, animationSpec = tween(600), label = "logoAlpha"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f, animationSpec = tween(800), label = "glowAlpha"
    )

    val shadowAlpha by animateFloatAsState(
        targetValue = if (!isActive) 1f else 0f, animationSpec = tween(600), label = "shadowAlpha"
    )

    val gradientAlpha by animateFloatAsState(
        targetValue = if (!isActive) 1f else 0f, animationSpec = tween(600), label = "gradientAlpha"
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
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        if (gradientAlpha > 0f) {
                            drawRect(
                                brush = Brush.linearGradient(
                                    0.0f to Color(0xFF6B00CC).copy(
                                        alpha = gradientAlpha
                                    ), // Brighter Violet
                                    0.4f to Color(0xFF6B00CC).copy(
                                        alpha = gradientAlpha
                                    ), // Brighter Violet
                                    // extended
                                    0.6f to Color(0xFF00BBFF).copy(
                                        alpha = gradientAlpha
                                    ), // Brighter Blue
                                    1.0f to Color(0xFF00BBFF).copy(
                                        alpha = gradientAlpha
                                    ), // Brighter Blue
                                    // extended
                                    start = Offset(size.width, 0f), end = Offset(0f, size.height)
                                ), blendMode = BlendMode.SrcAtop
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
private fun PermissionCards(state: BlockerViewState, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.permissions.forEach { permission ->
            AnimatedVisibility(
                visible = !permission.isGranted,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()) {
                CompactPermissionCard(
                    title = permission.title,
                    description = permission.description,
                    icon = permission.icon,
                    isGranted = permission.isGranted,
                    instructions = permission.instructions,
                    actions = permission.actions
                )
            }
        }
    }
}
