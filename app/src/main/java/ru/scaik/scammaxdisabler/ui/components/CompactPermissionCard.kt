package ru.scaik.scammaxdisabler.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.scaik.scammaxdisabler.ui.theme.ErrorRed
import ru.scaik.scammaxdisabler.ui.theme.SuccessGreen
import ru.scaik.scammaxdisabler.ui.theme.TextPrimaryLight
import ru.scaik.scammaxdisabler.ui.theme.TextSecondaryLight

@Composable
fun CompactPermissionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    modifier: Modifier = Modifier,
    instructions: String? = null,
    actions: List<Pair<String, () -> Unit>> = emptyList()
) {
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    var isExpanded by remember { mutableStateOf(false) }

    val statusColor by
    animateColorAsState(
        targetValue = if (isGranted) SuccessGreen else ErrorRed,
        animationSpec = tween(400),
        label = "statusColor"
    )

    val surfaceColor =
        if (isGranted) {
            Color.White.copy(alpha = 0.4f)
        } else {
            Color.White.copy(alpha = 0.45f)
        }

    val glassConfig =
        GlassmorphicConfig(
            blurRadius = 20.dp,
            surfaceColor = surfaceColor,
            borderColor = Color.Transparent,
            borderWidth = 0.dp
        )

    androidx.compose.foundation.layout.Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape =
                            if (isExpanded && instructions != null && !isGranted
                            ) {
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = 0.dp,
                                    bottomEnd = 0.dp
                                )
                            } else {
                                RoundedCornerShape(12.dp)
                            },
                        ambientColor = statusColor.copy(alpha = 0.1f),
                        spotColor = statusColor.copy(alpha = 0.1f)
                    )
                    .clip(
                        if (isExpanded && instructions != null && !isGranted) {
                            RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp,
                                bottomStart = 0.dp,
                                bottomEnd = 0.dp
                            )
                        } else {
                            RoundedCornerShape(12.dp)
                        }
                    )
                    .glassmorphicSurface(glassConfig)
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        surfaceColor,
                                        surfaceColor.copy(
                                            alpha =
                                                surfaceColor
                                                    .alpha *
                                                        0.9f
                                        )
                                    )
                            )
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            if (!isGranted &&
                                (instructions != null ||
                                        actions.isNotEmpty())
                            ) {
                                haptics.performHapticFeedback(
                                    HapticFeedbackType.LongPress
                                )
                                isExpanded = !isExpanded
                            }
                        }
                    )
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                CompactIconBox(icon = icon, statusColor = statusColor)

                Spacer(modifier = Modifier.width(12.dp))

                androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimaryLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = description,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondaryLight.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (!isGranted && (instructions != null || actions.isNotEmpty())) {
                    Icon(
                        imageVector =
                            if (isExpanded) Icons.Filled.ExpandLess
                            else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = TextSecondaryLight,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector =
                            if (isGranted) Icons.Filled.CheckCircle else Icons.Filled.Error,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier
                            .size(20.dp)
                            .alpha(if (isGranted) 1f else 0.8f)
                    )
                }
            }
        }

        androidx.compose.animation.AnimatedVisibility(
            visible =
                isExpanded && (instructions != null || actions.isNotEmpty()) && !isGranted,
            enter =
                androidx.compose.animation.expandVertically() +
                        androidx.compose.animation.fadeIn(),
            exit =
                androidx.compose.animation.shrinkVertically() +
                        androidx.compose.animation.fadeOut()
        ) {
            instructions?.let {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(
                                    topStart = 0.dp,
                                    topEnd = 0.dp,
                                    bottomStart = 12.dp,
                                    bottomEnd = 12.dp
                                )
                            )
                            .background(
                                brush =
                                    Brush.verticalGradient(
                                        colors =
                                            listOf(
                                                surfaceColor,
                                                surfaceColor.copy(
                                                    alpha =
                                                        surfaceColor
                                                            .alpha *
                                                                0.7f
                                                )
                                            )
                                    )
                            )
                            .padding(12.dp)
                ) {
                    androidx.compose.foundation.layout.Column {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextSecondaryLight,
                            lineHeight = 16.sp
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))

                        actions.forEach { (text, action) ->
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    action()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                            ) { Text(text = text, fontSize = 13.sp) }

                            androidx.compose.foundation.layout.Spacer(
                                modifier = Modifier.height(2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactIconBox(icon: ImageVector, statusColor: Color) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .shadow(
                        elevation = 2.dp,
                        shape = CircleShape,
                        ambientColor = statusColor.copy(alpha = 0.1f),
                        spotColor = statusColor.copy(alpha = 0.1f)
                    )
                    .clip(CircleShape)
                    .background(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        statusColor.copy(
                                            alpha = 0.08f
                                        ),
                                        statusColor.copy(
                                            alpha = 0.03f
                                        )
                                    )
                            )
                    ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
