package com.example.ui.chat

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ConversationEntity
import com.example.data.local.JarvisDatabase
import com.example.data.local.MessageEntity
import com.example.data.local.UserProfileEntity
import com.example.data.remote.GeminiClient
import com.example.data.remote.VeoVideoResponse
import com.example.data.repository.JarvisRepository
import com.example.voice.JarvisVoiceManager
import com.example.voice.JarvisVoiceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID

data class AttachmentDraft(
    val uri: Uri,
    val type: String, // "image" or "document"
    val name: String,
    val base64Data: String? = null,
    val extractedText: String? = null
)

enum class ChatbotRole(
    val displayName: String,
    val icon: String,
    val defaultModel: String,
    val systemInstruction: String
) {
    JARVIS_TACTICAL(
        displayName = "JARVIS Tactical",
        icon = "🛡️",
        defaultModel = "gemini-3.5-flash",
        systemInstruction = "You are JARVIS AI (Just A Rather Very Intelligent System). You are a calm, highly capable, polite, and extraordinarily intelligent personal AI assistant. Seamlessly support English and Hindi. Balance brevity with thoroughness."
    ),
    COMPLEX_REASONER(
        displayName = "Pro Analyst",
        icon = "🧠",
        defaultModel = "gemini-3.1-pro-preview",
        systemInstruction = "You are JARVIS Pro Analytical Core powered by gemini-3.1-pro-preview. You specialize in complex logic, STEM problems, deep reasoning, multi-step algorithmic planning, and meticulous technical breakdowns."
    ),
    FAST_DISPATCH(
        displayName = "Flash Lite",
        icon = "⚡",
        defaultModel = "gemini-3.1-flash-lite",
        systemInstruction = "You are JARVIS High-Velocity Core powered by gemini-3.1-flash-lite. You provide ultra-fast, snappy, concise answers and instant actions without unnecessary elaboration."
    ),
    CODE_ARCHITECT(
        displayName = "Code Architect",
        icon = "💻",
        defaultModel = "gemini-3.1-pro-preview",
        systemInstruction = "You are JARVIS Principal Software Architect. You write clean, modern, production-grade Kotlin, Android, algorithms, and system design with explanations of edge cases and performance."
    )
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = JarvisDatabase.getDatabase(application, viewModelScope)
    private var customApiKey: String? = null

    // Model and Role States
    private val _currentRole = MutableStateFlow(ChatbotRole.JARVIS_TACTICAL)
    val currentRole: StateFlow<ChatbotRole> = _currentRole.asStateFlow()

    private val _selectedModel = MutableStateFlow("gemini-3.5-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    var systemPromptOverride: String? = null

    private val geminiClient = GeminiClient(
        customKeyProvider = { customApiKey },
        modelProvider = { _selectedModel.value }
    )

    val repository = JarvisRepository(db.jarvisDao(), geminiClient)
    val voiceManager = JarvisVoiceManager(application)

    val conversations: StateFlow<List<ConversationEntity>> = repository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _attachmentDraft = MutableStateFlow<AttachmentDraft?>(null)
    val attachmentDraft: StateFlow<AttachmentDraft?> = _attachmentDraft.asStateFlow()

    private val _streamingAnswer = MutableStateFlow<String?>(null)
    val streamingAnswer: StateFlow<String?> = _streamingAnswer.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // --- Live API (gemini-3.8-live) State ---
    private val _isLiveActive = MutableStateFlow(false)
    val isLiveActive: StateFlow<Boolean> = _isLiveActive.asStateFlow()

    private val _liveStateDescription = MutableStateFlow("Tap to start live voice link")
    val liveStateDescription: StateFlow<String> = _liveStateDescription.asStateFlow()

    private val _liveMessages = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val liveMessages: StateFlow<List<Pair<String, String>>> = _liveMessages.asStateFlow()

    // --- Veo 3 Video Generation State ---
    private val _isGeneratingVeo = MutableStateFlow(false)
    val isGeneratingVeo: StateFlow<Boolean> = _isGeneratingVeo.asStateFlow()

    private val _veoProgress = MutableStateFlow("")
    val veoProgress: StateFlow<String> = _veoProgress.asStateFlow()

    private val _veoHistory = MutableStateFlow<List<VeoVideoResponse>>(emptyList())
    val veoHistory: StateFlow<List<VeoVideoResponse>> = _veoHistory.asStateFlow()

    private var activeGenerationJob: Job? = null
    private var messagesObserverJob: Job? = null

    init {
        viewModelScope.launch {
            conversations.collect { list ->
                if (_currentConversationId.value == null && list.isNotEmpty()) {
                    selectConversation(list.first().id)
                }
            }
        }

        voiceManager.onSpeechRecognized = { spoken ->
            if (_isLiveActive.value) {
                sendLiveUtterance(spoken)
            } else {
                _inputText.value = spoken
                sendMessage(autoSpeakReply = true)
            }
        }
    }

    fun selectRole(role: ChatbotRole) {
        _currentRole.value = role
        _selectedModel.value = role.defaultModel
        systemPromptOverride = role.systemInstruction
    }

    fun selectModel(model: String) {
        _selectedModel.value = model
    }

    fun setCustomApiKey(key: String) {
        customApiKey = key.ifBlank { null }
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectConversation(id: String) {
        _currentConversationId.value = id
        messagesObserverJob?.cancel()
        messagesObserverJob = viewModelScope.launch {
            repository.getMessagesForConversation(id).collect {
                _messages.value = it
            }
        }
    }

    fun startNewConversation() {
        viewModelScope.launch {
            val newId = repository.createNewConversation("Mission " + System.currentTimeMillis().toString().takeLast(4))
            selectConversation(newId)
        }
    }

    fun renameConversation(id: String, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            repository.updateConversationTitle(id, newTitle)
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_currentConversationId.value == id) {
                _currentConversationId.value = null
                val remaining = conversations.value.filter { it.id != id }
                if (remaining.isNotEmpty()) {
                    selectConversation(remaining.first().id)
                } else {
                    startNewConversation()
                }
            }
        }
    }

    fun attachImage(uri: Uri, fileName: String = "image.jpg") {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                _attachmentDraft.value = AttachmentDraft(
                    uri = uri,
                    type = "image",
                    name = fileName,
                    base64Data = base64
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Failed to read image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun attachDocument(uri: Uri, fileName: String = "document.txt") {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val textContent = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                _attachmentDraft.value = AttachmentDraft(
                    uri = uri,
                    type = "document",
                    name = fileName,
                    extractedText = textContent.take(15000) // 15k characters limit
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Failed to read document", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun clearAttachment() {
        _attachmentDraft.value = null
    }

    fun sendMessage(autoSpeakReply: Boolean = false) {
        val text = _inputText.value.trim()
        val attachment = _attachmentDraft.value
        if (text.isEmpty() && attachment == null) return

        val promptToSend = if (text.isEmpty() && attachment != null) {
            if (attachment.type == "image") "Analyze this visual data in detail, JARVIS." else "Summarize and extract key insights from this document, JARVIS."
        } else {
            text
        }

        _inputText.value = ""
        _attachmentDraft.value = null
        _isGenerating.value = true
        _streamingAnswer.value = ""
        voiceManager.setThinkingState()

        val roleInstruction = systemPromptOverride ?: _currentRole.value.systemInstruction
        val activeModel = _selectedModel.value

        activeGenerationJob?.cancel()
        activeGenerationJob = viewModelScope.launch {
            var convId = _currentConversationId.value
            if (convId == null) {
                convId = repository.createNewConversation(promptToSend.take(30))
                selectConversation(convId)
            }

            val result = repository.sendMessage(
                conversationId = convId,
                prompt = promptToSend,
                imageBase64 = attachment?.base64Data,
                documentName = attachment?.name,
                documentText = attachment?.extractedText,
                systemPromptOverride = roleInstruction,
                modelOverride = activeModel,
                onChunk = { chunk ->
                    _streamingAnswer.value = (_streamingAnswer.value ?: "") + chunk
                }
            )

            _isGenerating.value = false
            _streamingAnswer.value = null

            if (result.isSuccess) {
                val reply = result.getOrNull()?.content ?: ""
                if (autoSpeakReply && reply.isNotEmpty()) {
                    voiceManager.speak(reply)
                } else {
                    voiceManager.setIdleState()
                }
            } else {
                voiceManager.setIdleState()
                val err = result.exceptionOrNull()?.message ?: "Communication failed"
                Toast.makeText(getApplication(), err, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun stopGeneration() {
        activeGenerationJob?.cancel()
        _isGenerating.value = false
        _streamingAnswer.value = null
        voiceManager.setIdleState()
    }

    fun copyMessage(text: String) {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("JARVIS Message", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(getApplication(), "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun retryLastMessage() {
        val lastUserMsg = _messages.value.lastOrNull { it.role == "user" }
        if (lastUserMsg != null) {
            _inputText.value = lastUserMsg.content
            sendMessage()
        }
    }

    fun editUserMessage(message: MessageEntity) {
        _inputText.value = message.content
    }

    fun toggleVoiceListening() {
        if (voiceManager.voiceState.value == JarvisVoiceState.LISTENING) {
            voiceManager.stopListening()
        } else {
            voiceManager.startListening()
        }
    }

    fun speakMessage(text: String) {
        if (voiceManager.voiceState.value == JarvisVoiceState.SPEAKING) {
            voiceManager.stopSpeaking()
        } else {
            voiceManager.speak(text)
        }
    }

    // --- Live API (gemini-3.8-live) Real-Time Voice Functions ---
    fun toggleLiveSession() {
        if (_isLiveActive.value) {
            endLiveSession()
        } else {
            startLiveSession()
        }
    }

    fun startLiveSession() {
        _isLiveActive.value = true
        _liveStateDescription.value = "CONNECTING (gemini-3.8-live)..."
        viewModelScope.launch {
            kotlinx.coroutines.delay(600)
            _liveStateDescription.value = "LIVE LINK READY • Speak to JARVIS"
            voiceManager.speak("Live audio link established with model gemini-3.8-live. I am listening.")
            kotlinx.coroutines.delay(2000)
            voiceManager.startListening()
        }
    }

    fun endLiveSession() {
        _isLiveActive.value = false
        _liveStateDescription.value = "Live link terminated."
        voiceManager.stopListening()
        voiceManager.stopSpeaking()
        voiceManager.setIdleState()
    }

    fun sendLiveUtterance(text: String) {
        if (text.isBlank()) return
        val current = _liveMessages.value.toMutableList()
        current.add("user" to text)
        _liveMessages.value = current
        _liveStateDescription.value = "STREAMING (gemini-3.8-live)..."
        voiceManager.setThinkingState()

        viewModelScope.launch {
            val response = repository.converseLiveApi(
                prompt = text,
                history = current
            )

            if (response.isSuccess) {
                val reply = response.getOrNull() ?: ""
                current.add("jarvis" to reply)
                _liveMessages.value = current
                _liveStateDescription.value = "SPEAKING..."
                voiceManager.speak(reply)
                // Resume listening after speaking
                kotlinx.coroutines.delay(3000)
                if (_isLiveActive.value) {
                    _liveStateDescription.value = "LISTENING (gemini-3.8-live)..."
                    voiceManager.startListening()
                }
            } else {
                _liveStateDescription.value = "Error: " + (response.exceptionOrNull()?.message ?: "Live error")
                voiceManager.setIdleState()
            }
        }
    }

    // --- Veo 3 Video Generation Function ---
    fun generateVeoVideo(
        prompt: String,
        aspectRatio: String = "16:9",
        resolution: String = "1080p"
    ) {
        if (prompt.isBlank() || _isGeneratingVeo.value) return

        _isGeneratingVeo.value = true
        _veoProgress.value = "Connecting to veo-3.1-fast-generate-preview..."

        viewModelScope.launch {
            val result = repository.generateVeoVideo(
                prompt = prompt,
                aspectRatio = aspectRatio,
                resolution = resolution,
                onProgress = { status ->
                    _veoProgress.value = status
                }
            )

            _isGeneratingVeo.value = false
            _veoProgress.value = ""

            if (result.isSuccess) {
                val video = result.getOrNull()
                if (video != null) {
                    val updated = _veoHistory.value.toMutableList()
                    updated.add(0, video)
                    _veoHistory.value = updated
                    Toast.makeText(getApplication(), "Veo 3: Video job initialized successfully!", Toast.LENGTH_LONG).show()
                }
            } else {
                val err = result.exceptionOrNull()?.message ?: "Veo 3 generation failed"
                Toast.makeText(getApplication(), err, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
