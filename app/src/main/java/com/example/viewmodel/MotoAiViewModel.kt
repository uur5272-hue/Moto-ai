package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.AppConfig
import com.example.ai.MotoAiEngine
import com.example.ai.ParsedIntent
import com.example.model.AssistantState
import com.example.model.ChatMessage
import com.example.model.MessageSender
import com.example.model.MediaType
import com.example.voice.VoiceEngine
import com.example.voice.VoiceEngineCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class MotoAiViewModel(application: Application) : AndroidViewModel(application), VoiceEngineCallback {

    private val aiEngine = MotoAiEngine()
    private var voiceEngine: VoiceEngine? = null

    private val _assistantState = MutableStateFlow(AssistantState.IDLE)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    private val _statusText = MutableStateFlow("STANDBY // SAY \"HEY MOTO\"")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _transcript = MutableStateFlow<List<ChatMessage>>(emptyList())
    val transcript: StateFlow<List<ChatMessage>> = _transcript.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val _isContinuousListening = MutableStateFlow(true)
    val isContinuousListening: StateFlow<Boolean> = _isContinuousListening.asStateFlow()

    private val _partialTranscription = MutableStateFlow("")
    val partialTranscription: StateFlow<String> = _partialTranscription.asStateFlow()

    // Media studio preferences
    private val _selectedImageSize = MutableStateFlow("1K") // 1K, 2K, 4K
    val selectedImageSize: StateFlow<String> = _selectedImageSize.asStateFlow()

    private val _selectedImageAspect = MutableStateFlow("1:1") // 1:1, 16:9, 9:16
    val selectedImageAspect: StateFlow<String> = _selectedImageAspect.asStateFlow()

    private val _selectedVideoAspect = MutableStateFlow("16:9") // 16:9, 9:16
    val selectedVideoAspect: StateFlow<String> = _selectedVideoAspect.asStateFlow()

    // Voice pitch & speed
    private val _voicePitch = MutableStateFlow(0.85f)
    val voicePitch: StateFlow<Float> = _voicePitch.asStateFlow()

    private val _voiceSpeed = MutableStateFlow(1.0f)
    val voiceSpeed: StateFlow<Float> = _voiceSpeed.asStateFlow()

    // Photo uploaded for image-to-video Veo generation
    private val _uploadedPhotoBitmap = MutableStateFlow<Bitmap?>(null)
    val uploadedPhotoBitmap: StateFlow<Bitmap?> = _uploadedPhotoBitmap.asStateFlow()

    private val _uploadedPhotoBase64 = MutableStateFlow<String?>(null)
    val uploadedPhotoBase64: StateFlow<String?> = _uploadedPhotoBase64.asStateFlow()

    init {
        // Welcome message in transcript
        _transcript.value = listOf(
            ChatMessage(
                sender = MessageSender.MOTO_AI,
                text = "MOTO AI online. Neural matrix ready. Voice command armed with \"Hey Moto\". Say \"Hey Moto, create an image of a neon city\" or ask me any query."
            )
        )
    }

    fun initVoice(context: android.content.Context) {
        if (voiceEngine == null) {
            voiceEngine = VoiceEngine(context.applicationContext, this)
            if (_isContinuousListening.value) {
                voiceEngine?.startListening()
            }
        }
    }

    fun setContinuousListening(enabled: Boolean) {
        _isContinuousListening.value = enabled
        if (enabled) {
            voiceEngine?.startListening()
            _statusText.value = "LISTENING FOR \"HEY MOTO\""
        } else {
            voiceEngine?.stopListening()
            _statusText.value = "VOICE ENGINE PAUSED"
            _assistantState.value = AssistantState.IDLE
        }
    }

    fun toggleListening() {
        setContinuousListening(!_isContinuousListening.value)
    }

    fun updateVoiceConfig(pitch: Float, speed: Float) {
        _voicePitch.value = pitch
        _voiceSpeed.value = speed
        voiceEngine?.updateVoiceConfig(pitch, speed)
    }

    fun setImagePreferences(size: String, aspect: String) {
        _selectedImageSize.value = size
        _selectedImageAspect.value = aspect
    }

    fun setVideoAspect(aspect: String) {
        _selectedVideoAspect.value = aspect
    }

    fun setUploadedPhoto(bitmap: Bitmap?) {
        _uploadedPhotoBitmap.value = bitmap
        if (bitmap != null) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            val bytes = stream.toByteArray()
            _uploadedPhotoBase64.value = Base64.encodeToString(bytes, Base64.NO_WRAP)
        } else {
            _uploadedPhotoBase64.value = null
        }
    }

    /**
     * Strict Wake-Word handler triggered by VoiceEngine
     */
    override fun onWakeWordDetected(fullSpokenText: String, extractedQuery: String) {
        viewModelScope.launch {
            _statusText.value = "WAKE WORD DETECTED // PROCESSING"
            val queryToProcess = if (extractedQuery.isNotBlank()) {
                extractedQuery
            } else {
                "Hello Moto"
            }

            // Add user's spoken command to transcript
            addMessage(ChatMessage(sender = MessageSender.USER, text = fullSpokenText))

            executeVoiceQuery(queryToProcess)
        }
    }

    override fun onSpeechVolumeChanged(normalizedLevel: Float) {
        _audioLevel.value = normalizedLevel
    }

    override fun onListeningStateChanged(isListening: Boolean) {
        if (isListening && _assistantState.value == AssistantState.IDLE) {
            _assistantState.value = AssistantState.LISTENING
            _statusText.value = "LISTENING FOR \"HEY MOTO\""
        } else if (!isListening && _assistantState.value == AssistantState.LISTENING) {
            _assistantState.value = AssistantState.IDLE
        }
    }

    override fun onTtsSpeakingStateChanged(isSpeaking: Boolean) {
        if (isSpeaking) {
            _assistantState.value = AssistantState.SPEAKING
            _statusText.value = "SPEAKING ROBOTIC SYNTHESIS..."
        } else {
            _assistantState.value = AssistantState.IDLE
            _statusText.value = "STANDBY // READY"
            if (_isContinuousListening.value) {
                voiceEngine?.startListening()
            }
        }
    }

    override fun onError(errorMessage: String) {
        _statusText.value = "SYS ALERT: $errorMessage"
    }

    override fun onPartialTranscription(partialText: String) {
        _partialTranscription.value = partialText
    }

    /**
     * Parse intent and route query to appropriate AI pipeline
     */
    fun executeVoiceQuery(query: String) {
        viewModelScope.launch {
            val intent = aiEngine.classifyIntent(query)
            when (intent) {
                is ParsedIntent.GenerateImage -> {
                    triggerImageGeneration(
                        prompt = intent.prompt,
                        size = _selectedImageSize.value,
                        aspect = _selectedImageAspect.value
                    )
                }
                is ParsedIntent.GenerateVideo -> {
                    triggerVideoGeneration(
                        prompt = intent.prompt,
                        aspect = _selectedVideoAspect.value,
                        photoBase64 = _uploadedPhotoBase64.value
                    )
                }
                is ParsedIntent.Chat -> {
                    executeChatQuery(intent.query)
                }
            }
        }
    }

    fun submitTextQuery(rawText: String) {
        if (rawText.isBlank()) return
        addMessage(ChatMessage(sender = MessageSender.USER, text = rawText))
        executeVoiceQuery(rawText)
    }

    private suspend fun executeChatQuery(query: String) {
        _assistantState.value = AssistantState.THINKING
        _statusText.value = "THINKING // COMMUNICATING WITH GEMINI"

        val response = aiEngine.sendVoiceQuery(query)

        // Add to transcript
        addMessage(ChatMessage(sender = MessageSender.MOTO_AI, text = response))

        // Speak response via robotic TTS
        voiceEngine?.speak(response)
    }

    fun triggerImageGeneration(prompt: String, size: String = "1K", aspect: String = "1:1") {
        viewModelScope.launch {
            _assistantState.value = AssistantState.GENERATING_MEDIA
            _statusText.value = "SYNTHESIZING IMAGE // GEMINI 3 PRO"

            // Temporary placeholder card in transcript
            val placeholderId = java.util.UUID.randomUUID().toString()
            addMessage(
                ChatMessage(
                    id = placeholderId,
                    sender = MessageSender.MOTO_AI,
                    text = "Generating image: \"$prompt\" ($size, $aspect)...",
                    mediaType = MediaType.IMAGE,
                    mediaResolution = size,
                    mediaAspectRatio = aspect,
                    isGenerating = true
                )
            )

            val speakIntro = "Initiating image generation for $prompt in $size resolution."
            voiceEngine?.speak(speakIntro)

            val result = aiEngine.generateImage(prompt, size, aspect)

            // Replace placeholder with completed card
            _transcript.value = _transcript.value.map { msg ->
                if (msg.id == placeholderId) {
                    ChatMessage(
                        id = placeholderId,
                        sender = MessageSender.MOTO_AI,
                        text = result.description,
                        mediaType = MediaType.IMAGE,
                        mediaBase64 = result.base64Data,
                        mediaUrl = result.mediaUrl,
                        mediaResolution = size,
                        mediaAspectRatio = aspect,
                        isGenerating = false
                    )
                } else msg
            }

            _assistantState.value = AssistantState.IDLE
            _statusText.value = "IMAGE RENDER COMPLETE"
            voiceEngine?.speak("Image render complete.")
        }
    }

    fun triggerVideoGeneration(prompt: String, aspect: String = "16:9", photoBase64: String? = null) {
        viewModelScope.launch {
            _assistantState.value = AssistantState.GENERATING_MEDIA
            _statusText.value = "GENERATING VIDEO // VEO 3.1 FAST"

            val isImageToVideo = photoBase64 != null
            val actionLabel = if (isImageToVideo) "Animating photo into video" else "Generating video"

            val placeholderId = java.util.UUID.randomUUID().toString()
            addMessage(
                ChatMessage(
                    id = placeholderId,
                    sender = MessageSender.MOTO_AI,
                    text = "$actionLabel: \"$prompt\" ($aspect)...",
                    mediaType = MediaType.VIDEO,
                    mediaAspectRatio = aspect,
                    mediaResolution = "1080p",
                    isGenerating = true
                )
            )

            voiceEngine?.speak("Triggering Veo video pipeline. Stand by.")

            val result = aiEngine.generateVeoVideo(prompt, photoBase64, aspect)

            // Replace placeholder
            _transcript.value = _transcript.value.map { msg ->
                if (msg.id == placeholderId) {
                    ChatMessage(
                        id = placeholderId,
                        sender = MessageSender.MOTO_AI,
                        text = result.description,
                        mediaType = MediaType.VIDEO,
                        mediaUrl = result.mediaUrl,
                        mediaAspectRatio = aspect,
                        mediaResolution = "1080p",
                        isGenerating = false
                    )
                } else msg
            }

            _assistantState.value = AssistantState.IDLE
            _statusText.value = "VEO VIDEO PIPELINE READY"
            voiceEngine?.speak("Video generation complete and ready for playback.")
        }
    }

    fun animateImageToVideo(imageMessage: ChatMessage) {
        val prompt = "Animate with dynamic cinematic motion: " + imageMessage.text
        triggerVideoGeneration(
            prompt = prompt,
            aspect = if (imageMessage.mediaAspectRatio == "9:16") "9:16" else "16:9",
            photoBase64 = imageMessage.mediaBase64
        )
    }

    fun regenerateMedia(message: ChatMessage) {
        if (message.mediaType == MediaType.IMAGE) {
            triggerImageGeneration(message.text, message.mediaResolution, message.mediaAspectRatio)
        } else if (message.mediaType == MediaType.VIDEO) {
            triggerVideoGeneration(message.text, message.mediaAspectRatio)
        } else {
            executeVoiceQuery(message.text)
        }
    }

    fun speakText(text: String) {
        voiceEngine?.speak(text)
    }

    fun stopSpeaking() {
        voiceEngine?.stopSpeaking()
    }

    private fun addMessage(message: ChatMessage) {
        _transcript.value = _transcript.value + message
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine?.destroy()
    }
}
