package ru.scaik.scammaxdisabler.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.scaik.scammaxdisabler.ui.theme.CrimsonAccent
import ru.scaik.scammaxdisabler.ui.theme.SkyBlueAccent
import ru.scaik.scammaxdisabler.ui.theme.TextPrimaryDark
import ru.scaik.scammaxdisabler.ui.theme.TextPrimaryLight

@Composable
fun GlassmorphicStatusCard(
    isActive: Boolean,
    statusText: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val iconRotation by animateFloatAsState(
        targetValue = if (isActive) 360f else 0f,
        animationSpec = tween(800),
        label = "iconRotation"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isActive) 1.1f else 1f,
        animationSpec = tween(400),
        label = "iconScale"
    )

    val glowColor by animateColorAsState(
        targetValue = if (isActive) CrimsonAccent else SkyBlueAccent,
        animationSpec = tween(600),
        label = "glowColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (isActive) TextPrimaryDark else TextPrimaryLight,
        animationSpec = tween(400),
        label = "textColor"
    )

    val surfaceColor =
        if (isActive) Color.Black.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.25f)

    val glassConfig = GlassmorphicConfig(
        blurRadius = 30.dp,
        surfaceColor = surfaceColor,
        borderColor = Color.Transparent,
        borderWidth = 0.dp
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isActive) 8.dp else 12.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = if (isActive) Color.Transparent else glowColor.copy(alpha = 0.2f),
                    spotColor = if (isActive) Color.Transparent else glowColor.copy(alpha = 0.2f)
                )
                .clip(RoundedCornerShape(20.dp))
                .glassmorphicSurface(glassConfig)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            surfaceColor,
                            surfaceColor.copy(alpha = surfaceColor.alpha * 0.6f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                AnimatedStatusIcon(
                    isActive = isActive,
                    rotation = iconRotation,
                    scale = iconScale,
                    glowColor = glowColor,
                    pulseAlpha = if (isActive) pulseAlpha else 0.4f
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = statusText,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )

                    subtitle?.let {
                        Text(
                            text = it,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = textColor.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedStatusIcon(
    isActive: Boolean,
    rotation: Float,
    scale: Float,
    glowColor: Color,
    pulseAlpha: Float
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(56.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .scale(scale * 1.2f)
                .alpha(pulseAlpha)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.3f),
                            glowColor.copy(alpha = 0f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    ambientColor = glowColor,
                    spotColor = glowColor
                )
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(
                    Color.White
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                Color.White.copy(alpha = 0.9f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Filled.Shield else Icons.Filled.Lock,
                    contentDescription = null,
                    tint = glowColor,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(rotation)
                        .scale(scale)
                )
            }
        }
    }
}
