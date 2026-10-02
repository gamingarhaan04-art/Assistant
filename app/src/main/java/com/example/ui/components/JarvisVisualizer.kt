package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisAmberGlow
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDanger
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisOrange
import com.example.voice.JarvisVoiceState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JarvisVisualizer(
    state: JarvisVoiceState,
    amplitude: Float = 0f,
    size: Dp = 140.dp,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_visualizer")

    val rotationOuter by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rot"
    )

    val rotationInner by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rot"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val activeColor = when (state) {
        JarvisVoiceState.IDLE -> JarvisOrange
        JarvisVoiceState.LISTENING -> JarvisGold
        JarvisVoiceState.THINKING -> JarvisCyan
        JarvisVoiceState.SPEAKING -> JarvisGreen
        JarvisVoiceState.ERROR -> JarvisDanger
    }

    val stateText = when (state) {
        JarvisVoiceState.IDLE -> "JARVIS ONLINE"
        JarvisVoiceState.LISTENING -> "LISTENING..."
        JarvisVoiceState.THINKING -> "COMPUTING..."
        JarvisVoiceState.SPEAKING -> "TRANSMITTING..."
        JarvisVoiceState.ERROR -> "SYSTEM ALERT"
    }

    Column(
        modifier = modifier.semantics {
            contentDescription = "JARVIS Visualizer in $stateText state"
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val canvasWidth = this.size.width
                val canvasHeight = this.size.height
                val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
                val baseRadius = (canvasWidth / 2f) * 0.85f

                // Outer rotating segmented ring
                rotate(rotationOuter, pivot = center) {
                    drawCircle(
                        color = activeColor.copy(alpha = 0.28f),
                        radius = baseRadius,
                        center = center,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 16f, 8f, 16f))
                        )
                    )
                }

                // Inner reverse-rotating technical ring
                rotate(rotationInner, pivot = center) {
                    drawCircle(
                        color = JarvisAmberGlow.copy(alpha = 0.45f),
                        radius = baseRadius * 0.76f,
                        center = center,
                        style = Stroke(
                            width = 1.8.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f))
                        )
                    )
                    // Orbital nodes
                    for (i in 0 until 4) {
                        val angle = (i * 90.0) * (Math.PI / 180.0)
                        val r = baseRadius * 0.76f
                        val nodeOffset = Offset(
                            x = center.x + (r * cos(angle)).toFloat(),
                            y = center.y + (r * sin(angle)).toFloat()
                        )
                        drawCircle(
                            color = activeColor,
                            radius = 3.5.dp.toPx(),
                            center = nodeOffset
                        )
                    }
                }

                // Center glowing reactor core
                val dynamicRadius = baseRadius * 0.52f * (if (state == JarvisVoiceState.LISTENING || state == JarvisVoiceState.SPEAKING) (pulse + amplitude * 0.25f) else pulse)

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            activeColor.copy(alpha = 0.85f),
                            activeColor.copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = dynamicRadius * 1.5f
                    ),
                    radius = dynamicRadius * 1.5f,
                    center = center
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            activeColor,
                            Color(0xFF0C1622)
                        ),
                        center = center,
                        radius = dynamicRadius
                    ),
                    radius = dynamicRadius,
                    center = center
                )

                // High-tech center emblem line
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = dynamicRadius * 0.25f,
                    center = center
                )

                drawCircle(
                    color = activeColor,
                    radius = dynamicRadius * 0.38f,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // State Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0C1622).copy(alpha = 0.9f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(activeColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stateText,
                color = activeColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }
    }
}
