package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssistantState
import com.example.ui.theme.CyberGridLine
import com.example.ui.theme.CyberNeonGreen
import com.example.ui.theme.CyberpunkPink
import com.example.ui.theme.DarkMetallicSurface
import com.example.ui.theme.DeepSpaceBackground
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.LaserBlue
import com.example.ui.theme.MetallicSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun CyberpunkBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBackground)
    ) {
        // High-tech sci-fi grid overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 48.dp.toPx()
            val w = size.width
            val h = size.height

            // Vertical grid lines
            var x = 0f
            while (x <= w) {
                drawLine(
                    color = CyberGridLine,
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1f
                )
                x += step
            }

            // Horizontal grid lines
            var y = 0f
            while (y <= h) {
                drawLine(
                    color = CyberGridLine,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
                y += step
            }

            // Sci-fi corner brackets
            val bracketLen = 24.dp.toPx()
            val bracketStroke = 2.dp.toPx()
            val bracketColor = NeonCyan.copy(alpha = 0.4f)

            // Top-left
            drawLine(bracketColor, Offset(16f, 16f), Offset(16f + bracketLen, 16f), bracketStroke)
            drawLine(bracketColor, Offset(16f, 16f), Offset(16f, 16f + bracketLen), bracketStroke)

            // Top-right
            drawLine(bracketColor, Offset(w - 16f, 16f), Offset(w - 16f - bracketLen, 16f), bracketStroke)
            drawLine(bracketColor, Offset(w - 16f, 16f), Offset(w - 16f, 16f + bracketLen), bracketStroke)

            // Bottom-left
            drawLine(bracketColor, Offset(16f, h - 16f), Offset(16f + bracketLen, h - 16f), bracketStroke)
            drawLine(bracketColor, Offset(16f, h - 16f), Offset(16f, h - 16f - bracketLen), bracketStroke)

            // Bottom-right
            drawLine(bracketColor, Offset(w - 16f, h - 16f), Offset(w - 16f - bracketLen, h - 16f), bracketStroke)
            drawLine(bracketColor, Offset(w - 16f, h - 16f), Offset(w - 16f, h - 16f - bracketLen), bracketStroke)
        }

        content()
    }
}

@Composable
fun GlassmorphicTopBar(
    state: AssistantState,
    isListening: Boolean,
    onToggleListening: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "StatusTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(NeonCyan.copy(alpha = 0.6f), LaserBlue.copy(alpha = 0.2f), ElectricViolet.copy(alpha = 0.5f))
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .clip(RoundedCornerShape(18.dp)),
        color = DarkMetallicSurface.copy(alpha = 0.85f),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Logo and branding
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            brush = Brush.radialGradient(listOf(NeonCyan, LaserBlue)),
                            shape = CircleShape
                        )
                        .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "M",
                        color = DeepSpaceBackground,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "MOTO",
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI",
                            color = NeonCyan,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "WAKE: \"HEY MOTO\"",
                        color = TextTertiary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            // Middle: Animated Live Status Indicator
            val (statusColor, statusGlow) = when (state) {
                AssistantState.IDLE -> Pair(CyberNeonGreen, CyberNeonGreen.copy(alpha = 0.3f))
                AssistantState.LISTENING -> Pair(NeonCyan, NeonCyan.copy(alpha = pulseAlpha))
                AssistantState.THINKING -> Pair(LaserBlue, ElectricViolet.copy(alpha = pulseAlpha))
                AssistantState.SPEAKING -> Pair(NeonCyan, NeonCyan.copy(alpha = pulseAlpha))
                AssistantState.GENERATING_MEDIA -> Pair(ElectricViolet, ElectricViolet.copy(alpha = pulseAlpha))
            }

            Box(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = statusColor.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(statusGlow.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                color = statusColor.copy(alpha = if (state != AssistantState.IDLE) pulseAlpha else 1f),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = state.label,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Right: Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleListening,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("toggle_mic_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Toggle Microphone",
                        tint = if (isListening) NeonCyan else TextTertiary
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Settings",
                        tint = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun TelemetryBanner(
    audioRmsDb: Float,
    isArmed: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "SYS: GEMINI-1.5 // VEO 3.1",
            color = TextTertiary,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(if (isArmed) CyberNeonGreen else CyberpunkPink, CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isArmed) "TRIGGER: ARMED" else "STANDBY",
                color = if (isArmed) CyberNeonGreen else TextTertiary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Text(
            text = "VOL: ${(audioRmsDb * 100).toInt()}%",
            color = if (audioRmsDb > 0.1f) NeonCyan else TextTertiary,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
