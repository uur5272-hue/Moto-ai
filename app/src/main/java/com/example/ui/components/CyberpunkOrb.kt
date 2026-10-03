package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.model.AssistantState
import com.example.ui.theme.CyberNeonGreen
import com.example.ui.theme.DarkMetallicSurface
import com.example.ui.theme.DeepSpaceBackground
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.LaserBlue
import com.example.ui.theme.NeonCyan
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CyberpunkOrb(
    state: AssistantState,
    audioLevel: Float, // 0.0 to 1.0 from microphone
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    // Infinite animations for continuous rotations and pulses
    val infiniteTransition = rememberInfiniteTransition(label = "OrbInfiniteTransition")

    // Slow rotation for idle ambient rings
    val idleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "IdleRotation"
    )

    // Fast rotation for thinking state
    val fastRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "FastRotation"
    )

    // Breathing pulse
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathingScale"
    )

    // Speaking equalizer phase
    val speakingPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SpeakingPhase"
    )

    // Smooth animated audio level response
    val animatedAudioLevel = remember { Animatable(0f) }
    LaunchedEffect(audioLevel) {
        animatedAudioLevel.animateTo(
            targetValue = audioLevel,
            animationSpec = tween(durationMillis = 80, easing = LinearEasing)
        )
    }

    Box(
        modifier = modifier
            .size(280.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension * 0.38f

            when (state) {
                AssistantState.IDLE -> {
                    drawIdleState(
                        center = center,
                        baseRadius = baseRadius,
                        rotation = idleRotation,
                        scale = breathingScale
                    )
                }
                AssistantState.LISTENING -> {
                    drawListeningState(
                        center = center,
                        baseRadius = baseRadius,
                        rotation = idleRotation * 2f,
                        audioLevel = animatedAudioLevel.value
                    )
                }
                AssistantState.THINKING -> {
                    drawThinkingState(
                        center = center,
                        baseRadius = baseRadius,
                        fastRotation = fastRotation,
                        scale = breathingScale
                    )
                }
                AssistantState.SPEAKING -> {
                    drawSpeakingState(
                        center = center,
                        baseRadius = baseRadius,
                        phase = speakingPhase,
                        rotation = idleRotation
                    )
                }
                AssistantState.GENERATING_MEDIA -> {
                    drawGeneratingMediaState(
                        center = center,
                        baseRadius = baseRadius,
                        fastRotation = fastRotation,
                        phase = speakingPhase
                    )
                }
            }
        }
    }
}

/**
 * IDLE STATE: Soft cyan pulse, subtle breathing effect, slow orbiting tech rings
 */
private fun DrawScope.drawIdleState(
    center: Offset,
    baseRadius: Float,
    rotation: Float,
    scale: Float
) {
    val coreRadius = baseRadius * 0.55f * scale

    // Ambient outer glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(NeonCyan.copy(alpha = 0.25f), Color.Transparent),
            center = center,
            radius = baseRadius * 1.3f
        ),
        radius = baseRadius * 1.3f,
        center = center
    )

    // Outer HUD segmented ring
    rotate(degrees = rotation, pivot = center) {
        drawCircle(
            color = NeonCyan.copy(alpha = 0.35f),
            radius = baseRadius * 1.1f,
            center = center,
            style = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 30f, 40f, 15f), 0f)
            )
        )
    }

    // Counter-rotating inner ring
    rotate(degrees = -rotation * 1.4f, pivot = center) {
        drawCircle(
            color = LaserBlue.copy(alpha = 0.45f),
            radius = baseRadius * 0.88f,
            center = center,
            style = Stroke(
                width = 2.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(50f, 25f, 15f, 25f), 0f)
            )
        )

        // Small tech notches
        for (i in 0 until 8) {
            val angle = (i * 45) * (PI / 180).toFloat()
            val notchStart = Offset(
                center.x + (baseRadius * 0.84f) * cos(angle),
                center.y + (baseRadius * 0.84f) * sin(angle)
            )
            val notchEnd = Offset(
                center.x + (baseRadius * 0.92f) * cos(angle),
                center.y + (baseRadius * 0.92f) * sin(angle)
            )
            drawLine(
                color = NeonCyan.copy(alpha = 0.6f),
                start = notchStart,
                end = notchEnd,
                strokeWidth = 2.dp.toPx()
            )
        }
    }

    // 3D Metallic Core sphere
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                NeonCyan.copy(alpha = 0.95f),
                LaserBlue,
                DarkMetallicSurface,
                DeepSpaceBackground
            ),
            center = center.copy(x = center.x - coreRadius * 0.2f, y = center.y - coreRadius * 0.25f),
            radius = coreRadius
        ),
        radius = coreRadius,
        center = center
    )

    // Glowing rim light
    drawCircle(
        color = NeonCyan,
        radius = coreRadius,
        center = center,
        style = Stroke(width = 3.dp.toPx())
    )
}

/**
 * LISTENING STATE: Rapid cyan wave expansion matching microphone audio volume
 */
private fun DrawScope.drawListeningState(
    center: Offset,
    baseRadius: Float,
    rotation: Float,
    audioLevel: Float
) {
    val expansion = audioLevel * 70f
    val coreRadius = baseRadius * 0.58f + (audioLevel * 20f)

    // Audio shockwaves
    for (i in 1..4) {
        val waveRadius = coreRadius + (i * 24f) + (expansion * (i * 0.6f))
        val alpha = ((1f - (i * 0.22f)) * (0.3f + audioLevel * 0.7f)).coerceIn(0f, 1f)
        drawCircle(
            color = NeonCyan.copy(alpha = alpha),
            radius = waveRadius,
            center = center,
            style = Stroke(
                width = (3.dp.toPx() + (audioLevel * 3f)),
                cap = StrokeCap.Round
            )
        )
    }

    // Rapid rotating waveform reticle
    rotate(degrees = rotation * 3f, pivot = center) {
        drawCircle(
            color = NeonCyan,
            radius = baseRadius * 1.15f + expansion,
            center = center,
            style = Stroke(
                width = 2.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
            )
        )
    }

    // Core pulsing violently with microphone intensity
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White,
                NeonCyan,
                LaserBlue,
                DeepSpaceBackground
            ),
            center = center,
            radius = coreRadius * 1.1f
        ),
        radius = coreRadius,
        center = center
    )

    // Inner bright spark
    drawCircle(
        color = Color.White,
        radius = 8.dp.toPx() + (audioLevel * 14f),
        center = center
    )
}

/**
 * THINKING STATE: Rotating dual-ring energy field with electric blue particles and violet energy arcs
 */
private fun DrawScope.drawThinkingState(
    center: Offset,
    baseRadius: Float,
    fastRotation: Float,
    scale: Float
) {
    val coreRadius = baseRadius * 0.5f

    // Intense Violet/Blue background aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(ElectricViolet.copy(alpha = 0.4f), Color.Transparent),
            center = center,
            radius = baseRadius * 1.35f
        ),
        radius = baseRadius * 1.35f,
        center = center
    )

    // Primary clockwise energy ring (Laser Blue)
    rotate(degrees = fastRotation, pivot = center) {
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(LaserBlue, NeonCyan, Color.Transparent, LaserBlue),
                center = center
            ),
            startAngle = 0f,
            sweepAngle = 260f,
            useCenter = false,
            topLeft = Offset(center.x - baseRadius * 1.1f, center.y - baseRadius * 1.1f),
            size = Size(baseRadius * 2.2f, baseRadius * 2.2f),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )

        // Trailing particles
        for (i in 0 until 6) {
            val angle = (i * 45) * (PI / 180).toFloat()
            val px = center.x + (baseRadius * 1.1f) * cos(angle)
            val py = center.y + (baseRadius * 1.1f) * sin(angle)
            drawCircle(
                color = NeonCyan,
                radius = 3.5.dp.toPx(),
                center = Offset(px, py)
            )
        }
    }

    // Secondary counter-clockwise ring (Electric Violet)
    rotate(degrees = -fastRotation * 1.6f, pivot = center) {
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(ElectricViolet, Color.White, Color.Transparent, ElectricViolet),
                center = center
            ),
            startAngle = 90f,
            sweepAngle = 220f,
            useCenter = false,
            topLeft = Offset(center.x - baseRadius * 0.85f, center.y - baseRadius * 0.85f),
            size = Size(baseRadius * 1.7f, baseRadius * 1.7f),
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Violet particles
        for (i in 0 until 4) {
            val angle = (i * 90 + 30) * (PI / 180).toFloat()
            val px = center.x + (baseRadius * 0.85f) * cos(angle)
            val py = center.y + (baseRadius * 0.85f) * sin(angle)
            drawCircle(
                color = ElectricViolet,
                radius = 4.dp.toPx(),
                center = Offset(px, py)
            )
        }
    }

    // Pulsing cyber core with electric arcs
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White,
                ElectricViolet,
                LaserBlue,
                DarkMetallicSurface
            ),
            center = center,
            radius = coreRadius * scale
        ),
        radius = coreRadius * scale,
        center = center
    )
}

/**
 * SPEAKING STATE: Rhythmic equalizer voice wave synced to TTS audio output
 */
private fun DrawScope.drawSpeakingState(
    center: Offset,
    baseRadius: Float,
    phase: Float,
    rotation: Float
) {
    val coreRadius = baseRadius * 0.52f

    // Outer equalizer bars arranged radially
    val barCount = 36
    val barAngleStep = 360f / barCount

    for (i in 0 until barCount) {
        val angleDeg = i * barAngleStep
        val angleRad = angleDeg * (PI / 180).toFloat()
        // Wave equation simulating synthetic voice harmonics
        val waveVal = sin(i * 0.8f + phase) * 0.5f + sin(i * 1.6f - phase * 1.5f) * 0.5f
        val barHeight = (12.dp.toPx() + waveVal.coerceAtLeast(0f) * 45f)

        val startRadius = baseRadius * 0.88f
        val endRadius = startRadius + barHeight

        val startX = center.x + startRadius * cos(angleRad)
        val startY = center.y + startRadius * sin(angleRad)
        val endX = center.x + endRadius * cos(angleRad)
        val endY = center.y + endRadius * sin(angleRad)

        val barColor = if (i % 2 == 0) NeonCyan else LaserBlue

        drawLine(
            color = barColor,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    // Inner voice equalizer sine path
    val path = Path()
    val waveWidth = coreRadius * 1.4f
    val waveStart = center.x - waveWidth / 2f
    val points = 30

    for (j in 0..points) {
        val progress = j.toFloat() / points
        val x = waveStart + (progress * waveWidth)
        val envelope = sin(progress * PI.toFloat()) // tapered at edges
        val y = center.y + sin(progress * 6 * PI.toFloat() + phase * 2f) * (20f * envelope)
        if (j == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    // Core sphere
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                NeonCyan,
                LaserBlue,
                DarkMetallicSurface,
                DeepSpaceBackground
            ),
            center = center,
            radius = coreRadius
        ),
        radius = coreRadius,
        center = center
    )

    // Draw the active sine waveform across the core
    drawPath(
        path = path,
        color = Color.White,
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )
}

/**
 * MEDIA GENERATING STATE: High-speed dual vortex with neon green and purple pulses
 */
private fun DrawScope.drawGeneratingMediaState(
    center: Offset,
    baseRadius: Float,
    fastRotation: Float,
    phase: Float
) {
    val coreRadius = baseRadius * 0.52f

    // Rotating dashed rings in tertiary CyberNeonGreen
    rotate(degrees = fastRotation * 2f, pivot = center) {
        drawCircle(
            color = CyberNeonGreen,
            radius = baseRadius * 1.1f,
            center = center,
            style = Stroke(
                width = 3.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 15f), 0f)
            )
        )
    }

    rotate(degrees = -fastRotation * 1.5f, pivot = center) {
        drawCircle(
            color = ElectricViolet,
            radius = baseRadius * 0.9f,
            center = center,
            style = Stroke(
                width = 3.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
            )
        )
    }

    // Dual vortex core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White,
                CyberNeonGreen,
                ElectricViolet,
                DarkMetallicSurface
            ),
            center = center,
            radius = coreRadius
        ),
        radius = coreRadius,
        center = center
    )
}
