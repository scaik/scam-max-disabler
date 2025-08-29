package ru.scaik.scammaxdisabler.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.scaik.scammaxdisabler.ui.theme.*

@Composable
fun CircularPowerIndicator(
    isActive: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "powerPulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isActive) 360f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "rotation"
    )

    val sweepAngle by animateFloatAsState(
        targetValue = if (isActive) 360f else 270f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 200f
        ),
        label = "sweepAngle"
    )

    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.85f,
        animationSpec = tween(300),
        label = "scale"
    )

    val primaryColor by animateColorAsState(
        targetValue = if (isActive) CrimsonBright else SkyBlueMedium,
        animationSpec = tween(600),
        label = "primaryColor"
    )

    val secondaryColor by animateColorAsState(
        targetValue = if (isActive) CrimsonDark else SkyBlueDark,
        animationSpec = tween(600),
        label = "secondaryColor"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isActive) {
            if (enabled) CrimsonAccent else CrimsonMedium.copy(alpha = 0.5f)
        } else {
            if (enabled) SkyBlueAccent else SkyBlueMedium.copy(alpha = 0.5f)
        },
        animationSpec = tween(400),
        label = "iconColor"
    )

    val backgroundAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.6f,
        animationSpec = tween(300),
        label = "backgroundAlpha"
    )

    val glassConfig = GlassmorphicConfig(
        blurRadius = 25.dp,
        surfaceColor = Color.White.copy(alpha = if (isActive) 0.15f else 0.25f),
        borderColor = Color.Transparent,
        borderWidth = 0.dp
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        if (isActive && enabled) {
            Box(
                modifier = Modifier
                    .size(size * 1.3f)
                    .scale(pulseScale)
                    .alpha(glowAlpha * 0.5f)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.4f),
                                primaryColor.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Box(
            modifier = Modifier
                .size(size)
                .shadow(
                    elevation = if (isActive) 16.dp else 8.dp,
                    shape = CircleShape,
                    ambientColor = primaryColor.copy(alpha = if (isActive) glowAlpha * 0.3f else 0.2f),
                    spotColor = primaryColor.copy(alpha = if (isActive) glowAlpha * 0.3f else 0.2f)
                )
                .clip(CircleShape)
                .alpha(backgroundAlpha)
                .glassmorphicSurface(glassConfig)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isActive) 0.1f else 0.2f),
                            Color.White.copy(alpha = if (isActive) 0.05f else 0.15f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.size(size)
            ) {
                val strokeWidth = 4.dp.toPx()
                val radius = (size.toPx() / 2) - strokeWidth

                rotate(if (isActive) rotation else -rotation * 0.5f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                primaryColor,
                                secondaryColor,
                                primaryColor
                            )
                        ),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round
                        ),
                        topLeft = Offset(
                            x = center.x - radius,
                            y = center.y - radius
                        ),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(size * 0.7f)
                    .align(Alignment.Center)
                    .shadow(
                        elevation = 4.dp,
                        shape = CircleShape,
                        ambientColor = primaryColor.copy(alpha = 0.2f),
                        spotColor = primaryColor.copy(alpha = 0.2f)
                    )
                    .clip(CircleShape)
                    .glassmorphicSurface(
                        GlassmorphicConfig(
                            blurRadius = 15.dp,
                            surfaceColor = Color.White.copy(alpha = if (isActive) 0.1f else 0.2f),
                            borderColor = Color.Transparent,
                            borderWidth = 0.dp
                        )
                    )
                    .background(
                        brush = Brush.radialGradient(
                            colors = if (isActive) {
                                listOf(
                                    primaryColor.copy(alpha = 0.08f),
                                    secondaryColor.copy(alpha = 0.04f),
                                    Color.Transparent
                                )
                            } else {
                                listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PowerSettingsNew,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier
                        .size(size * 0.4f)
                )
            }
        }
    }
}
