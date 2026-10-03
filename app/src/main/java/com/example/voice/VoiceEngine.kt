package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.AppConfig
import java.util.Locale

interface VoiceEngineCallback {
    fun onWakeWordDetected(fullSpokenText: String, extractedQuery: String)
    fun onSpeechVolumeChanged(normalizedLevel: Float) // 0.0f to 1.0f
    fun onListeningStateChanged(isListening: Boolean)
    fun onTtsSpeakingStateChanged(isSpeaking: Boolean)
    fun onError(errorMessage: String)
    fun onPartialTranscription(partialText: String)
}

class VoiceEngine(
    private val context: Context,
    private val callback: VoiceEngineCallback
) : TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false
    private var isListeningDesired = true
    private var isCurrentlyListening = false

    private val mainHandler = Handler(Looper.getMainLooper())
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        textToSpeech = TextToSpeech(context, this)
        initSpeechRecognizer()
    }

    private fun initSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            callback.onError("Speech recognition not available on this device.")
            return
        }

        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createRecognitionListener())
                }
            } catch (e: Exception) {
                callback.onError("Failed to init recognizer: ${e.message}")
            }
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isCurrentlyListening = true
                callback.onListeningStateChanged(true)
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {
                // rmsdB typically ranges from -2 to 10
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                callback.onSpeechVolumeChanged(normalized)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                callback.onSpeechVolumeChanged(0f)
            }

            override fun onError(error: Int) {
                isCurrentlyListening = false
                callback.onSpeechVolumeChanged(0f)
                callback.onListeningStateChanged(false)

                // If continuous listening is desired, restart after a brief backoff
                if (isListeningDesired) {
                    mainHandler.postDelayed({
                        startListening()
                    }, 800)
                }
            }

            override fun onResults(results: Bundle?) {
                isCurrentlyListening = false
                callback.onSpeechVolumeChanged(0f)
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull() ?: ""

                processTranscription(spokenText)

                // Continue listening cycle
                if (isListeningDesired) {
                    mainHandler.postDelayed({
                        startListening()
                    }, 500)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = matches?.firstOrNull() ?: ""
                if (partial.isNotBlank()) {
                    callback.onPartialTranscription(partial)
                    // Early check if wake word appears in partial results
                    if (containsWakeWord(partial)) {
                        triggerHapticFeedback()
                    }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    /**
     * Strict Wake-Word Detection Filter:
     * AI responds ONLY when user says "Hey Moto" or "Moto".
     * Ignores all other phrases or ambient sound.
     */
    private fun processTranscription(spokenText: String) {
        if (spokenText.isBlank()) return

        val lower = spokenText.lowercase().trim()
        val primaryLower = AppConfig.PRIMARY_WAKE_WORD.lowercase()
        val secondaryLower = AppConfig.SECONDARY_WAKE_WORD.lowercase()

        var matchedWakeWord: String? = null
        var queryStartIndex = -1

        if (lower.contains(primaryLower)) {
            matchedWakeWord = AppConfig.PRIMARY_WAKE_WORD
            queryStartIndex = lower.indexOf(primaryLower) + primaryLower.length
        } else if (lower.contains(secondaryLower)) {
            // Check word boundary so we don't match substrings of unrelated words
            val regex = Regex("\\b${secondaryLower}\\b", RegexOption.IGNORE_CASE)
            val match = regex.find(spokenText)
            if (match != null) {
                matchedWakeWord = AppConfig.SECONDARY_WAKE_WORD
                queryStartIndex = match.range.last + 1
            }
        }

        if (matchedWakeWord != null) {
            triggerHapticFeedback()

            // Extract the query following the wake word
            val rawQuery = if (queryStartIndex in 0..spokenText.length) {
                spokenText.substring(queryStartIndex).trim().trimStart(',', ':', '-', ' ')
            } else {
                ""
            }

            callback.onWakeWordDetected(
                fullSpokenText = spokenText,
                extractedQuery = rawQuery
            )
        }
    }

    fun containsWakeWord(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains(AppConfig.PRIMARY_WAKE_WORD.lowercase()) ||
                Regex("\\b${AppConfig.SECONDARY_WAKE_WORD.lowercase()}\\b").containsMatchIn(lower)
    }

    fun startListening() {
        isListeningDesired = true
        if (isCurrentlyListening) return

        mainHandler.post {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                // If recognizer is in bad state, reinit
                initSpeechRecognizer()
            }
        }
    }

    fun stopListening() {
        isListeningDesired = false
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
                isCurrentlyListening = false
                callback.onListeningStateChanged(false)
            } catch (_: Exception) {}
        }
    }

    fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Futuristic double-pulse haptic vibration
                val timings = longArrayOf(0, 40, 60, 80)
                val amplitudes = intArrayOf(0, 180, 0, 255)
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 40, 60, 80), -1)
            }
        } catch (_: Exception) {}
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.let { tts ->
                tts.language = Locale.US
                // Configure crisp robotic voice parameters
                tts.setPitch(0.85f)
                tts.setSpeechRate(1.0f)
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        callback.onTtsSpeakingStateChanged(true)
                    }

                    override fun onDone(utteranceId: String?) {
                        callback.onTtsSpeakingStateChanged(false)
                    }

                    override fun onError(utteranceId: String?) {
                        callback.onTtsSpeakingStateChanged(false)
                    }
                })
                isTtsReady = true
            }
        }
    }

    fun speak(text: String, utteranceId: String = "moto_ai_${System.currentTimeMillis()}") {
        if (!isTtsReady) return
        mainHandler.post {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
        callback.onTtsSpeakingStateChanged(false)
    }

    fun updateVoiceConfig(pitch: Float, speed: Float) {
        textToSpeech?.setPitch(pitch)
        textToSpeech?.setSpeechRate(speed)
    }

    fun destroy() {
        stopListening()
        speechRecognizer?.destroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}
