package com.standby.mode.ui.modules

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

private data class Particle(
    val initialX: Float,
    val initialY: Float,
    val speed: Float,
    val radius: Float,
    val alpha: Float
)

@Composable
fun AmbientVisualsModule(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")

    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "grad_anim"
    )

    val particles = remember {
        List(35) {
            Particle(
                initialX = Random.nextFloat(),
                initialY = Random.nextFloat(),
                speed = Random.nextFloat() * 0.15f + 0.05f,
                radius = Random.nextFloat() * 3.5f + 1.5f,
                alpha = Random.nextFloat() * 0.5f + 0.2f
            )
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Smooth animated gradient
            val color1 = primaryColor.copy(alpha = 0.25f + gradientOffset * 0.15f)
            val color2 = secondaryColor.copy(alpha = 0.2f + (1f - gradientOffset) * 0.15f)
            val color3 = tertiaryColor.copy(alpha = 0.15f)

            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(color1, color2, color3, surfaceColor),
                    center = Offset(width * (0.3f + gradientOffset * 0.4f), height * (0.4f + (1f - gradientOffset) * 0.3f)),
                    radius = width.coerceAtLeast(height) * 0.85f
                )
            )

            // Draw floating particles
            particles.forEach { p ->
                val currentY = (p.initialY - gradientOffset * p.speed).mod(1.0f) * height
                val currentX = (p.initialX + (gradientOffset * 0.05f)).mod(1.0f) * width

                drawCircle(
                    color = Color.White.copy(alpha = p.alpha),
                    radius = p.radius,
                    center = Offset(currentX, currentY)
                )
            }
        }
    }
}

