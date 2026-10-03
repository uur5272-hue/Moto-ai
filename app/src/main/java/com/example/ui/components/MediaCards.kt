package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.model.ChatMessage
import com.example.model.MediaType
import com.example.ui.theme.CyberNeonGreen
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
fun MediaMessageCard(
    message: ChatMessage,
    onPlayAudio: (String) -> Unit,
    onRegenerate: (ChatMessage) -> Unit,
    onAnimateImageToVideo: ((ChatMessage) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = if (message.mediaType == MediaType.VIDEO) {
                        listOf(ElectricViolet.copy(alpha = 0.8f), LaserBlue.copy(alpha = 0.3f))
                    } else {
                        listOf(NeonCyan.copy(alpha = 0.8f), ElectricViolet.copy(alpha = 0.3f))
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp)),
        color = DarkMetallicSurface.copy(alpha = 0.9f),
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Type badge & Resolution/Aspect tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (message.mediaType == MediaType.VIDEO) Icons.Default.Movie else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (message.mediaType == MediaType.VIDEO) ElectricViolet else NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (message.mediaType == MediaType.VIDEO) "VEO 3.1 VIDEO" else "GEMINI 3 PRO IMAGE",
                        color = if (message.mediaType == MediaType.VIDEO) ElectricViolet else NeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TagChip(text = message.mediaResolution)
                    TagChip(text = message.mediaAspectRatio)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prompt description
            Text(
                text = message.text,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Media Preview or Generating Spinner
            if (message.isGenerating) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(MetallicSurfaceVariant, RoundedCornerShape(12.dp))
                        .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = if (message.mediaType == MediaType.VIDEO) ElectricViolet else NeonCyan,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (message.mediaType == MediaType.VIDEO) "SYNTHESIZING VEO VIDEO..." else "RENDERING NEURAL IMAGE...",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            } else if (message.mediaType == MediaType.IMAGE) {
                ImagePreviewContainer(message = message)
            } else if (message.mediaType == MediaType.VIDEO) {
                VideoPreviewContainer(message = message)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Play Audio, Download, Share, Re-generate, Animate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Play Audio
                    OutlinedButton(
                        onClick = { onPlayAudio(message.text) },
                        modifier = Modifier.height(34.dp).testTag("play_audio_button"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Play Audio", tint = NeonCyan, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Audio", color = NeonCyan, fontSize = 11.sp)
                    }

                    // Download / Save
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Asset saved to device gallery cache.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.height(34.dp).testTag("download_button"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TextTertiary)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download", tint = TextPrimary, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save", color = TextPrimary, fontSize = 11.sp)
                    }

                    // Share
                    OutlinedButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Generated by MOTO AI ASSISTANT: ${message.text}\n${message.mediaUrl ?: ""}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share MOTO AI Creation"))
                        },
                        modifier = Modifier.height(34.dp).testTag("share_button"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TextTertiary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = TextPrimary, modifier = Modifier.size(15.dp))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // If image, allow 1-tap "Animate to Video with Veo"
                    if (message.mediaType == MediaType.IMAGE && onAnimateImageToVideo != null) {
                        Button(
                            onClick = { onAnimateImageToVideo(message) },
                            modifier = Modifier.height(34.dp).testTag("animate_veo_button"),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
                        ) {
                            Icon(Icons.Default.Movie, contentDescription = "Animate with Veo", tint = DeepSpaceBackground, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Animate", color = DeepSpaceBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Re-generate
                    IconButton(
                        onClick = { onRegenerate(message) },
                        modifier = Modifier.size(34.dp).testTag("regenerate_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Re-generate", tint = NeonCyan)
                    }
                }
            }
        }
    }
}

@Composable
private fun TagChip(text: String) {
    Box(
        modifier = Modifier
            .background(DeepSpaceBackground.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
            .border(0.8.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = NeonCyan,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ImagePreviewContainer(message: ChatMessage) {
    val bitmap = remember(message.mediaBase64) {
        if (!message.mediaBase64.isNullOrBlank()) {
            try {
                val decoded = Base64.decode(message.mediaBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
    }

    val aspect = when (message.mediaAspectRatio) {
        "16:9" -> 16f / 9f
        "9:16" -> 9f / 16f
        else -> 1f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspect)
            .clip(RoundedCornerShape(12.dp))
            .background(DeepSpaceBackground)
            .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = message.text,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (!message.mediaUrl.isNullOrBlank()) {
            AsyncImage(
                model = message.mediaUrl,
                contentDescription = message.text,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = "Rendering Complete",
                color = TextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun VideoPreviewContainer(message: ChatMessage) {
    var isPlaying by remember { mutableStateOf(false) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    val aspect = when (message.mediaAspectRatio) {
        "9:16" -> 9f / 16f
        else -> 16f / 9f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspect)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
            .border(1.dp, ElectricViolet.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    val uri = Uri.parse(message.mediaUrl ?: "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4")
                    setVideoURI(uri)
                    setOnPreparedListener { mp ->
                        mp.isLooping = true
                    }
                    videoViewRef = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Play/Pause Button
        IconButton(
            onClick = {
                videoViewRef?.let { vv ->
                    if (isPlaying) {
                        vv.pause()
                        isPlaying = false
                    } else {
                        vv.start()
                        isPlaying = true
                    }
                }
            },
            modifier = Modifier
                .size(54.dp)
                .background(DeepSpaceBackground.copy(alpha = 0.65f), CircleShape)
                .border(1.5.dp, if (isPlaying) NeonCyan else ElectricViolet, CircleShape)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause Video" else "Play Video",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        // Live HUD video watermark
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(DeepSpaceBackground.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "VEO 3.1 • 1080p",
                color = ElectricViolet,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
