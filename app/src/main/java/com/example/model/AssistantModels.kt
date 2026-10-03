package com.example.model

enum class AssistantState(val label: String) {
    IDLE("ONLINE"),
    LISTENING("LISTENING..."),
    THINKING("THINKING..."),
    SPEAKING("SPEAKING..."),
    GENERATING_MEDIA("GENERATING MEDIA...")
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mediaType: MediaType = MediaType.NONE,
    val mediaUrl: String? = null,
    val mediaBase64: String? = null,
    val mediaAspectRatio: String = "1:1",
    val mediaResolution: String = "1K",
    val isGenerating: Boolean = false,
    val progress: Float = 1.0f
)

enum class MessageSender {
    USER,
    MOTO_AI,
    SYSTEM
}

enum class MediaType {
    NONE,
    IMAGE,
    VIDEO
}
