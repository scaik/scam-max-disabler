package ru.scaik.scammaxdisabler.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.scaik.scammaxdisabler.ui.theme.*

@Composable
fun NeumorphicToggle(
    isOn: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    width: Dp = 120.dp,
    height: Dp = 60.dp
) {
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    val thumbOffset by animateDpAsState(
        targetValue = if (isOn) width - height + 8.dp else 8.dp,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 300f
        ),
        label = "thumbOffset"
    )

    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.95f,
        animationSpec = tween(200),
        label = "scale"
    )

    val backgroundGradientStart by animateColorAsState(
        targetValue = if (isOn) CrimsonBright else SkyBlueLight,
        animationSpec = tween(400),
        label = "backgroundGradientStart"
    )

    val backgroundGradientEnd by animateColorAsState(
        targetValue = if (isOn) CrimsonDark else SkyBlueMedium,
        animationSpec = tween(400),
        label = "backgroundGradientEnd"
    )

    val thumbGlowColor by animateColorAsState(
        targetValue = if (isOn) CrimsonAccent.copy(alpha = 0.3f) else SkyBlueAccent.copy(alpha = 0.2f),
        animationSpec = tween(400),
        label = "thumbGlowColor"
    )

    val shadowConfig = NeumorphicShadowConfig(
        lightShadowColor = if (isOn) NeumorphicDarkGlow else NeumorphicLightGlow,
        darkShadowColor = if (isOn) NeumorphicDarkShadow else NeumorphicLightShadow,
        shadowRadius = 16.dp,
        shadowOffset = 8.dp,
        cornerRadius = height / 2
    )

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .scale(scale)
            .neumorphicShadow(shadowConfig)
            .clip(RoundedCornerShape(height / 2))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(backgroundGradientStart, backgroundGradientEnd)
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = if (isOn) RippleDark else RippleLight
                ),
                enabled = enabled,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggle()
                }
            )
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .align(Alignment.CenterStart)
                .size(height - 16.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    ambientColor = thumbGlowColor,
                    spotColor = thumbGlowColor
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            Color.White.copy(alpha = 0.95f)
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = if (isOn) {
                                listOf(
                                    CrimsonAccent.copy(alpha = 0.2f),
                                    CrimsonAccent.copy(alpha = 0.05f)
                                )
                            } else {
                                listOf(
                                    SkyBlueAccent.copy(alpha = 0.15f),
                                    SkyBlueAccent.copy(alpha = 0.03f)
                                )
                            }
                        )
                    )
            )
        }
    }
}
