package ru.scaik.scammaxdisabler.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
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
import ru.scaik.scammaxdisabler.ui.theme.DividerLight
import ru.scaik.scammaxdisabler.ui.theme.ErrorRed
import ru.scaik.scammaxdisabler.ui.theme.NeumorphicLightBackground
import ru.scaik.scammaxdisabler.ui.theme.NeumorphicLightGlow
import ru.scaik.scammaxdisabler.ui.theme.NeumorphicLightShadow
import ru.scaik.scammaxdisabler.ui.theme.SkyBlueMedium
import ru.scaik.scammaxdisabler.ui.theme.SuccessGreen
import ru.scaik.scammaxdisabler.ui.theme.TextPrimaryLight
import ru.scaik.scammaxdisabler.ui.theme.TextSecondaryLight

@Composable
fun NeumorphicPermissionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    actionButtonText: String = "Открыть настройки",
    detailedInstructions: String? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    val cardScale by animateFloatAsState(
        targetValue = if (isExpanded) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 400f
        ),
        label = "cardScale"
    )

    val expandIconRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "expandIconRotation"
    )

    val statusColor by animateColorAsState(
        targetValue = if (isGranted) SuccessGreen else ErrorRed,
        animationSpec = tween(400),
        label = "statusColor"
    )

    val backgroundColor = if (isGranted) {
        NeumorphicLightBackground.copy(alpha = 0.95f)
    } else {
        Color.White.copy(alpha = 0.98f)
    }

    val shadowConfig = NeumorphicShadowConfig(
        lightShadowColor = if (isGranted) {
            NeumorphicLightGlow.copy(alpha = 0.7f)
        } else {
            NeumorphicLightGlow.copy(alpha = 0.9f)
        },
        darkShadowColor = if (isGranted) {
            NeumorphicLightShadow.copy(alpha = 0.3f)
        } else {
            NeumorphicLightShadow.copy(alpha = 0.4f)
        },
        shadowRadius = if (isExpanded) 20.dp else 14.dp,
        shadowOffset = if (isExpanded) 10.dp else 7.dp,
        cornerRadius = 16.dp
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(cardScale)
            .neumorphicShadow(shadowConfig)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = detailedInstructions != null,
                onClick = {
                    if (detailedInstructions != null) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        isExpanded = !isExpanded
                    }
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top
                ) {
                    PermissionIconBox(
                        icon = icon,
                        isGranted = isGranted,
                        statusColor = statusColor
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimaryLight,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            StatusIndicator(
                                isGranted = isGranted,
                                statusColor = statusColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = description,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextSecondaryLight,
                            lineHeight = 20.sp
                        )

                        if (!isGranted) {
                            Spacer(modifier = Modifier.height(12.dp))

                            NeumorphicActionButton(
                                text = actionButtonText,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onAction()
                                }
                            )
                        }
                    }
                }

                if (detailedInstructions != null) {
                    Icon(
                        imageVector = Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = TextSecondaryLight,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(24.dp)
                            .rotate(expandIconRotation)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded && detailedInstructions != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                detailedInstructions?.let {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(DividerLight)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = it,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextSecondaryLight,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionIconBox(
    icon: ImageVector,
    isGranted: Boolean,
    statusColor: Color
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(48.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(
                    elevation = if (isGranted) 4.dp else 6.dp,
                    shape = CircleShape,
                    ambientColor = statusColor.copy(alpha = 0.2f),
                    spotColor = statusColor.copy(alpha = 0.2f)
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            statusColor.copy(alpha = 0.1f),
                            statusColor.copy(alpha = 0.05f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun StatusIndicator(
    isGranted: Boolean,
    statusColor: Color
) {
    val pulseAlpha by animateFloatAsState(
        targetValue = if (isGranted) 1f else 0.8f,
        animationSpec = tween(600),
        label = "pulseAlpha"
    )

    Icon(
        imageVector = if (isGranted) Icons.Filled.CheckCircle else Icons.Filled.Error,
        contentDescription = null,
        tint = statusColor,
        modifier = Modifier
            .size(20.dp)
            .alpha(pulseAlpha)
    )
}

@Composable
private fun NeumorphicActionButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = SkyBlueMedium.copy(alpha = 0.3f),
                spotColor = SkyBlueMedium.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SkyBlueMedium,
            contentColor = Color.White
        )
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
