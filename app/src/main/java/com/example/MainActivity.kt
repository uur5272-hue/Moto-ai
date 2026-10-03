package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AssistantState
import com.example.model.ChatMessage
import com.example.model.MessageSender
import com.example.ui.components.CyberpunkBackground
import com.example.ui.components.CyberpunkOrb
import com.example.ui.components.GlassmorphicTopBar
import com.example.ui.components.MediaMessageCard
import com.example.ui.components.TelemetryBanner
import com.example.ui.theme.CyberNeonGreen
import com.example.ui.theme.CyberpunkPink
import com.example.ui.theme.DarkMetallicSurface
import com.example.ui.theme.DeepSpaceBackground
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.LaserBlue
import com.example.ui.theme.MetallicSurfaceVariant
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.MotoAiViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MotoAiMainScreen()
            }
        }
    }
}

@Composable
fun MotoAiMainScreen(viewModel: MotoAiViewModel = viewModel()) {
    val context = LocalContext.current
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            viewModel.initVoice(context)
        } else {
            Toast.makeText(context, "Microphone permission required for Moto AI voice engine.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (hasAudioPermission) {
            viewModel.initVoice(context)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val assistantState by viewModel.assistantState.collectAsState()
    val statusText by viewModel.statusText.collectAsState()
    val transcript by viewModel.transcript.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()
    val isContinuousListening by viewModel.isContinuousListening.collectAsState()
    val partialText by viewModel.partialTranscription.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Core HUD, 1: Media Studio, 2: Transcript

    val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    CyberpunkBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding, bottom = bottomPadding)
        ) {
            // Glassmorphic Top Bar
            GlassmorphicTopBar(
                state = assistantState,
                isListening = isContinuousListening,
                onToggleListening = {
                    if (hasAudioPermission) {
                        viewModel.toggleListening()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onOpenSettings = { showSettingsDialog = true }
            )

            // Telemetry Banner
            TelemetryBanner(
                audioRmsDb = audioLevel,
                isArmed = isContinuousListening && hasAudioPermission
            )

            // Tab Navigation
            CyberTabRow(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            // Permission Warning Banner if not granted
            if (!hasAudioPermission) {
                PermissionWarningBanner(
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                )
            }

            // Main Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> CoreHudScreen(
                        viewModel = viewModel,
                        assistantState = assistantState,
                        audioLevel = audioLevel,
                        statusText = statusText,
                        partialText = partialText,
                        hasPermission = hasAudioPermission,
                        onRequestPermission = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                    )
                    1 -> MediaStudioScreen(viewModel = viewModel)
                    2 -> TranscriptScreen(
                        transcript = transcript,
                        viewModel = viewModel
                    )
                }
            }

            // Bottom Quick Command Input Bar
            BottomCommandBar(
                viewModel = viewModel,
                isListening = isContinuousListening,
                onToggleMic = {
                    if (hasAudioPermission) {
                        viewModel.toggleListening()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }
}

@Composable
fun CyberTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(
        Pair("CORE HUD", Icons.Default.GraphicEq),
        Pair("MEDIA STUDIO", Icons.Default.AutoAwesome),
        Pair("LOGS", Icons.Default.History)
    )

    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color.Transparent,
        contentColor = NeonCyan,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                color = NeonCyan,
                height = 2.5.dp
            )
        },
        divider = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(NeonCyan.copy(alpha = 0.2f))
            )
        },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        tabs.forEachIndexed { index, pair ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = pair.first,
                        color = if (selectedTab == index) NeonCyan else TextTertiary,
                        fontSize = 11.sp,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                },
                icon = {
                    Icon(
                        imageVector = pair.second,
                        contentDescription = pair.first,
                        tint = if (selectedTab == index) NeonCyan else TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier.testTag("tab_${pair.first.lowercase().replace(" ", "_")}")
            )
        }
    }
}

@Composable
fun CoreHudScreen(
    viewModel: MotoAiViewModel,
    assistantState: AssistantState,
    audioLevel: Float,
    statusText: String,
    partialText: String,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Cyberpunk dynamic central metallic orb
        CyberpunkOrb(
            state = assistantState,
            audioLevel = audioLevel,
            modifier = Modifier.testTag("cyberpunk_orb"),
            onClick = {
                if (!hasPermission) {
                    onRequestPermission()
                } else {
                    viewModel.executeVoiceQuery("Hey Moto, give me a status report")
                }
            }
        )

        // Status Readout Box
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = statusText,
                color = when (assistantState) {
                    AssistantState.IDLE -> CyberNeonGreen
                    AssistantState.LISTENING -> NeonCyan
                    AssistantState.THINKING -> LaserBlue
                    AssistantState.SPEAKING -> NeonCyan
                    AssistantState.GENERATING_MEDIA -> ElectricViolet
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            AnimatedVisibility(visible = partialText.isNotBlank()) {
                Text(
                    text = "\"$partialText\"",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Quick Suggestion / Trigger Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = "SYNAPSE TRIGGERS // VOICE COMMANDS",
                color = TextTertiary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            val commands = listOf(
                "Hey Moto, status report",
                "Hey Moto, generate image of a cybernetic tiger",
                "Hey Moto, create video of flying cars in rain",
                "Hey Moto, who created you?",
                "Hey Moto, explain quantum computing"
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(commands) { cmd ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.executeVoiceQuery(cmd)
                            }
                            .testTag("chip_${cmd.take(15)}"),
                        color = DarkMetallicSurface.copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = cmd,
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MediaStudioScreen(viewModel: MotoAiViewModel) {
    val context = LocalContext.current
    val selectedImageSize by viewModel.selectedImageSize.collectAsState()
    val selectedImageAspect by viewModel.selectedImageAspect.collectAsState()
    val selectedVideoAspect by viewModel.selectedVideoAspect.collectAsState()
    val uploadedPhotoBitmap by viewModel.uploadedPhotoBitmap.collectAsState()

    var imagePrompt by remember { mutableStateOf("") }
    var videoPrompt by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    viewModel.setUploadedPhoto(bitmap)
                    Toast.makeText(context, "Photo loaded for Veo animation.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Gemini 3 Pro High-Quality Image Generation
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkMetallicSurface.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "IMAGE GENERATION",
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "gemini-3-pro-image-preview",
                            color = TextTertiary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Resolution affordance: 1K, 2K, 4K
                    Text("RESOLUTION SPECIFICATION:", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("1K", "2K", "4K").forEach { size ->
                            FilterChip(
                                selected = selectedImageSize == size,
                                onClick = { viewModel.setImagePreferences(size, selectedImageAspect) },
                                label = { Text(size, fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan,
                                    selectedLabelColor = DeepSpaceBackground
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Aspect ratio affordance: 1:1, 16:9, 9:16
                    Text("ASPECT RATIO:", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("1:1", "16:9", "9:16").forEach { ratio ->
                            FilterChip(
                                selected = selectedImageAspect == ratio,
                                onClick = { viewModel.setImagePreferences(selectedImageSize, ratio) },
                                label = { Text(ratio, fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LaserBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = imagePrompt,
                        onValueChange = { imagePrompt = it },
                        modifier = Modifier.fillMaxWidth().testTag("image_prompt_input"),
                        placeholder = { Text("Cyberpunk samurai in neon rain, metallic armor...", color = TextTertiary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = MetallicSurfaceVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (imagePrompt.isNotBlank()) {
                                viewModel.triggerImageGeneration(imagePrompt, selectedImageSize, selectedImageAspect)
                                imagePrompt = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("generate_image_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = DeepSpaceBackground)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Synthesize Image ($selectedImageSize)", color = DeepSpaceBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 2: Veo 3 Video Generation & Image-To-Video Animation
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectricViolet.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkMetallicSurface.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "VEO 3 VIDEO STUDIO",
                            color = ElectricViolet,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "veo-3.1-fast-generate-preview",
                            color = TextTertiary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Aspect ratio affordance: 16:9 or 9:16
                    Text("VIDEO ORIENTATION (MANDATORY):", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(Pair("16:9", "16:9 Landscape"), Pair("9:16", "9:16 Portrait")).forEach { pair ->
                            FilterChip(
                                selected = selectedVideoAspect == pair.first,
                                onClick = { viewModel.setVideoAspect(pair.first) },
                                label = { Text(pair.second, fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricViolet,
                                    selectedLabelColor = DeepSpaceBackground
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Animate photo into video affordance
                    Text("IMAGE-TO-VIDEO ANIMATION (OPTIONAL):", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.6f)),
                            modifier = Modifier.testTag("upload_photo_button")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Select Photo", tint = ElectricViolet)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Photo", color = TextPrimary, fontSize = 12.sp)
                        }

                        if (uploadedPhotoBitmap != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    bitmap = uploadedPhotoBitmap!!.asImageBitmap(),
                                    contentDescription = "Uploaded photo preview",
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(onClick = { viewModel.setUploadedPhoto(null) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove photo", tint = CyberpunkPink)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = videoPrompt,
                        onValueChange = { videoPrompt = it },
                        modifier = Modifier.fillMaxWidth().testTag("video_prompt_input"),
                        placeholder = { Text(if (uploadedPhotoBitmap != null) "Animate photo with neon particles and camera pan..." else "Cinematic cyberpunk speeder flying over neon megacity...", color = TextTertiary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricViolet,
                            unfocusedBorderColor = MetallicSurfaceVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (videoPrompt.isNotBlank()) {
                                viewModel.triggerVideoGeneration(
                                    prompt = videoPrompt,
                                    aspect = selectedVideoAspect,
                                    photoBase64 = viewModel.uploadedPhotoBase64.value
                                )
                                videoPrompt = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("generate_video_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
                    ) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = DeepSpaceBackground)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uploadedPhotoBitmap != null) "Animate Photo into Veo Video ($selectedVideoAspect)" else "Generate Veo Video ($selectedVideoAspect)",
                            color = DeepSpaceBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TranscriptScreen(
    transcript: List<ChatMessage>,
    viewModel: MotoAiViewModel
) {
    val listState = rememberLazyListState()

    LaunchedEffect(transcript.size) {
        if (transcript.isNotEmpty()) {
            listState.animateScrollToItem(transcript.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(transcript, key = { it.id }) { message ->
            if (message.mediaType != com.example.model.MediaType.NONE) {
                MediaMessageCard(
                    message = message,
                    onPlayAudio = { viewModel.speakText(it) },
                    onRegenerate = { viewModel.regenerateMedia(it) },
                    onAnimateImageToVideo = { viewModel.animateImageToVideo(it) }
                )
            } else {
                ChatBubble(
                    message = message,
                    onSpeak = { viewModel.speakText(message.text) }
                )
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    onSpeak: () -> Unit
) {
    val isUser = message.sender == MessageSender.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (isUser) LaserBlue.copy(alpha = 0.6f) else NeonCyan.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                ),
            color = if (isUser) LaserBlue.copy(alpha = 0.25f) else DarkMetallicSurface.copy(alpha = 0.85f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "COMMANDER" else "MOTO AI",
                        color = if (isUser) LaserBlue else NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    if (!isUser) {
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Speak Text",
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.text,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun BottomCommandBar(
    viewModel: MotoAiViewModel,
    isListening: Boolean,
    onToggleMic: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp)),
        color = DarkMetallicSurface.copy(alpha = 0.95f),
        tonalElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleMic,
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (isListening) NeonCyan.copy(alpha = 0.2f) else Color.Transparent,
                        CircleShape
                    )
                    .border(
                        1.dp,
                        if (isListening) NeonCyan else TextTertiary,
                        CircleShape
                    )
                    .testTag("bottom_mic_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Listen",
                    tint = if (isListening) NeonCyan else TextTertiary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("command_input_field"),
                placeholder = {
                    Text(
                        text = "Say \"Hey Moto...\" or type command",
                        color = TextTertiary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (textInput.isNotBlank()) {
                        viewModel.submitTextQuery(textInput)
                        textInput = ""
                        keyboardController?.hide()
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.submitTextQuery(textInput)
                        textInput = ""
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .background(NeonCyan, CircleShape)
                    .testTag("send_command_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Command",
                    tint = DeepSpaceBackground,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun PermissionWarningBanner(onRequestPermission: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, CyberpunkPink, RoundedCornerShape(12.dp)),
        color = CyberpunkPink.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AUDIO SENSOR AUTHORIZATION REQUIRED",
                    color = CyberpunkPink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Continuous \"Hey Moto\" wake-word requires mic permission.",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }

            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = CyberpunkPink),
                modifier = Modifier.testTag("grant_permission_button")
            ) {
                Text("Authorize", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingsDialog(
    viewModel: MotoAiViewModel,
    onDismiss: () -> Unit
) {
    val pitch by viewModel.voicePitch.collectAsState()
    val speed by viewModel.voiceSpeed.collectAsState()
    val continuous by viewModel.isContinuousListening.collectAsState()

    var tempPitch by remember { mutableStateOf(pitch) }
    var tempSpeed by remember { mutableStateOf(speed) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "MOTO AI SYSTEM CONFIGURATION",
                color = NeonCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Wake-word info
                Column {
                    Text("ACTIVE WAKE WORDS:", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("Primary: \"Hey Moto\" • Secondary: \"Moto\"", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Continuous Listening
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Continuous Wake Listening", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Auto-restarts microphone scan loop", color = TextTertiary, fontSize = 10.sp)
                    }
                    Switch(
                        checked = continuous,
                        onCheckedChange = { viewModel.setContinuousListening(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                    )
                }

                // Voice Pitch (Robotic futuristic = 0.85f)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TTS Robotic Pitch", color = TextPrimary, fontSize = 12.sp)
                        Text(String.format("%.2f", tempPitch), color = NeonCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = tempPitch,
                        onValueChange = {
                            tempPitch = it
                            viewModel.updateVoiceConfig(tempPitch, tempSpeed)
                        },
                        valueRange = 0.5f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = LaserBlue)
                    )
                }

                // Voice Speed
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Speech Rate", color = TextPrimary, fontSize = 12.sp)
                        Text(String.format("%.2f", tempSpeed), color = NeonCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = tempSpeed,
                        onValueChange = {
                            tempSpeed = it
                            viewModel.updateVoiceConfig(tempPitch, tempSpeed)
                        },
                        valueRange = 0.5f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = LaserBlue)
                    )
                }

                // Test voice button
                OutlinedButton(
                    onClick = {
                        viewModel.speakText("Moto AI robotic speech matrix configured. All systems nominal.")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Robotic Voice", color = NeonCyan, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = NeonCyan, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = DarkMetallicSurface,
        shape = RoundedCornerShape(20.dp)
    )
}
