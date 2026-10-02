package com.example.data.repository

import com.example.data.local.AppShortcutEntity
import com.example.data.local.ConversationEntity
import com.example.data.local.JarvisDao
import com.example.data.local.MessageEntity
import com.example.data.local.UserProfileEntity
import com.example.data.remote.GeminiClient
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class JarvisRepository(
    private val dao: JarvisDao,
    private val geminiClient: GeminiClient
) {
    // Conversations
    val allConversations: Flow<List<ConversationEntity>> = dao.getAllConversations()

    suspend fun createNewConversation(title: String = "New Mission"): String {
        val id = UUID.randomUUID().toString()
        val conv = ConversationEntity(
            id = id,
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        dao.insertConversation(conv)
        return id
    }

    suspend fun updateConversationTitle(id: String, title: String) {
        dao.updateConversationTitle(id, title)
    }

    suspend fun deleteConversation(id: String) {
        dao.deleteMessagesByConversation(id)
        dao.deleteConversation(id)
    }

    // Messages
    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>> =
        dao.getMessagesForConversation(conversationId)

    suspend fun sendMessage(
        conversationId: String,
        prompt: String,
        imageBase64: String? = null,
        imageMimeType: String = "image/jpeg",
        documentName: String? = null,
        documentText: String? = null,
        systemPromptOverride: String? = null,
        modelOverride: String? = null,
        onChunk: ((String) -> Unit)? = null
    ): Result<MessageEntity> {
        // 1. Record user message
        val userMsg = MessageEntity(
            conversationId = conversationId,
            role = "user",
            content = prompt,
            attachmentPath = if (imageBase64 != null) "image_attachment" else null,
            attachmentType = if (imageBase64 != null) "image" else if (documentName != null) "document" else null,
            attachmentName = documentName,
            modelUsed = modelOverride ?: "gemini-3.5-flash"
        )
        dao.insertMessage(userMsg)

        // 2. Fetch recent turns for context
        val previous = dao.getMessagesSnapshot(conversationId)
        val historyTurns = previous.dropLast(1).map { it.role to it.content }

        // Construct full prompt if document is attached
        val fullPrompt = if (!documentText.isNullOrEmpty()) {
            """
            [ATTACHED DOCUMENT: $documentName]
            $documentText
            
            [USER INSTRUCTION]:
            $prompt
            """.trimIndent()
        } else {
            prompt
        }

        // 3. Call Gemini
        val result = geminiClient.generateResponse(
            prompt = fullPrompt,
            history = historyTurns,
            imageBase64 = imageBase64,
            imageMimeType = imageMimeType,
            systemPromptOverride = systemPromptOverride,
            modelOverride = modelOverride,
            onChunk = onChunk
        )

        // 4. Update conversation title if it was default
        val conv = dao.getConversationById(conversationId)
        if (conv != null && (conv.title == "New Mission" || conv.title.startsWith("New Conversation"))) {
            val autoTitle = prompt.take(30).replace("\n", " ").trim()
            if (autoTitle.isNotEmpty()) {
                dao.updateConversationTitle(conversationId, autoTitle)
            }
        }

        // 5. Store AI response
        return if (result.isSuccess) {
            val replyText = result.getOrNull() ?: "Protocol executed."
            val jarvisMsg = MessageEntity(
                conversationId = conversationId,
                role = "jarvis",
                content = replyText,
                isError = false
            )
            val msgId = dao.insertMessage(jarvisMsg)
            // Deduct usage credit
            val user = dao.getUserProfileSnapshot()
            if (user != null) {
                dao.incrementCredits(user.email, 1)
            }
            Result.success(jarvisMsg.copy(id = msgId))
        } else {
            val errText = result.exceptionOrNull()?.message ?: "Neural link disrupted."
            val errorMsg = MessageEntity(
                conversationId = conversationId,
                role = "jarvis",
                content = errText,
                isError = true
            )
            val msgId = dao.insertMessage(errorMsg)
            Result.failure(Exception(errText))
        }
    }

    suspend fun retryMessage(messageId: Long): Result<MessageEntity> {
        val msg = dao.getMessageById(messageId) ?: return Result.failure(Exception("Message not found"))
        if (msg.role != "user") return Result.failure(Exception("Can only retry from user queries"))
        return sendMessage(msg.conversationId, msg.content)
    }

    // App Shortcuts
    val allShortcuts: Flow<List<AppShortcutEntity>> = dao.getAllShortcuts()

    suspend fun addCustomShortcut(name: String, desc: String, category: String, url: String, icon: String) {
        val shortcut = AppShortcutEntity(
            id = "custom_" + System.currentTimeMillis(),
            name = name,
            description = desc,
            category = category,
            iconSymbol = icon,
            colorHex = "#FF8A1E",
            url = url,
            isCustom = true
        )
        dao.insertShortcut(shortcut)
    }

    suspend fun deleteShortcut(id: String) {
        dao.deleteShortcut(id)
    }

    // User Profile & Subscriptions
    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()

    suspend fun getUserProfile(): UserProfileEntity? = dao.getUserProfileSnapshot()

    suspend fun saveProfile(profile: UserProfileEntity) {
        dao.saveUserProfile(profile)
    }

    suspend fun upgradeSubscription(planId: String, totalCredits: Int) {
        val current = dao.getUserProfileSnapshot()
        if (current != null) {
            dao.updateSubscriptionPlan(current.email, planId, totalCredits)
        }
    }

    // Live API (gemini-3.8-live) Real-Time Voice Conversation
    suspend fun converseLiveApi(
        prompt: String,
        history: List<Pair<String, String>> = emptyList(),
        onChunk: ((String) -> Unit)? = null
    ): Result<String> {
        val result = geminiClient.converseLiveApi(prompt, history, onChunk)
        val user = dao.getUserProfileSnapshot()
        if (user != null && result.isSuccess) {
            dao.incrementCredits(user.email, 1)
        }
        return result
    }

    // Veo 3 Video Generation (veo-3.1-fast-generate-preview)
    suspend fun generateVeoVideo(
        prompt: String,
        aspectRatio: String = "16:9",
        resolution: String = "1080p",
        onProgress: ((String) -> Unit)? = null
    ): Result<com.example.data.remote.VeoVideoResponse> {
        val result = geminiClient.generateVeoVideo(prompt, aspectRatio, resolution, onProgress)
        val user = dao.getUserProfileSnapshot()
        if (user != null && result.isSuccess) {
            dao.incrementCredits(user.email, 5) // Veo 3 video costs 5 credits
        }
        return result
    }
}
