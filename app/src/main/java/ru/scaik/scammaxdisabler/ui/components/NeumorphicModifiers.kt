package ru.scaik.scammaxdisabler.ui.components

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
data class NeumorphicShadowConfig(
    val lightShadowColor: Color,
    val darkShadowColor: Color,
    val shadowRadius: Dp = 12.dp,
    val shadowOffset: Dp = 6.dp,
    val cornerRadius: Dp = 16.dp
)

@Stable
data class GlassmorphicConfig(
    val blurRadius: Dp = 20.dp,
    val surfaceColor: Color,
    val borderColor: Color = Color.White.copy(alpha = 0.2f),
    val borderWidth: Dp = 1.dp
)

fun Modifier.neumorphicShadow(
    config: NeumorphicShadowConfig
) = this.drawBehind {
    drawNeumorphicShadow(
        lightColor = config.lightShadowColor,
        darkColor = config.darkShadowColor,
        shadowRadius = config.shadowRadius.toPx(),
        shadowOffset = config.shadowOffset.toPx(),
        cornerRadius = config.cornerRadius.toPx()
    )
}

fun Modifier.glassmorphicSurface(
    config: GlassmorphicConfig
) = this.drawBehind {
    drawGlassmorphicBackground(
        surfaceColor = config.surfaceColor,
        blurRadius = config.blurRadius.toPx()
    )
}

fun Modifier.gradientBackground(
    colors: List<Color>,
    angle: Float = 45f
) = this.drawBehind {
    val angleRad = Math.toRadians(angle.toDouble())
    val x = kotlin.math.cos(angleRad).toFloat()
    val y = kotlin.math.sin(angleRad).toFloat()

    val startOffset = Offset(
        x = if (x > 0) 0f else size.width,
        y = if (y > 0) 0f else size.height
    )

    val endOffset = Offset(
        x = if (x > 0) size.width else 0f,
        y = if (y > 0) size.height else 0f
    )

    drawRect(
        brush = Brush.linearGradient(
            colors = colors,
            start = startOffset,
            end = endOffset
        )
    )
}

private fun DrawScope.drawNeumorphicShadow(
    lightColor: Color,
    darkColor: Color,
    shadowRadius: Float,
    shadowOffset: Float,
    cornerRadius: Float
) {
    drawIntoCanvas { canvas ->
        val lightPaint = Paint().apply {
            color = lightColor
            this.asFrameworkPaint().apply {
                setShadowLayer(
                    shadowRadius,
                    -shadowOffset,
                    -shadowOffset,
                    lightColor.toArgb()
                )
            }
        }

        val darkPaint = Paint().apply {
            color = darkColor
            this.asFrameworkPaint().apply {
                setShadowLayer(
                    shadowRadius,
                    shadowOffset,
                    shadowOffset,
                    darkColor.toArgb()
                )
            }
        }

        canvas.drawRoundRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            radiusX = cornerRadius,
            radiusY = cornerRadius,
            paint = lightPaint
        )

        canvas.drawRoundRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            radiusX = cornerRadius,
            radiusY = cornerRadius,
            paint = darkPaint
        )
    }
}

private fun DrawScope.drawGlassmorphicBackground(
    surfaceColor: Color,
    blurRadius: Float
) {
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            color = surfaceColor
            this.asFrameworkPaint().apply {
                setMaskFilter(
                    android.graphics.BlurMaskFilter(
                        blurRadius,
                        android.graphics.BlurMaskFilter.Blur.NORMAL
                    )
                )
            }
        }

        canvas.drawRoundRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            radiusX = 16.dp.toPx(),
            radiusY = 16.dp.toPx(),
            paint = paint
        )
    }
}
