package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val modelUsed: String = "gemini-3.5-flash",
    val isPinned: Boolean = false
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String,
    val role: String, // "user" or "jarvis" or "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachmentPath: String? = null,
    val attachmentType: String? = null, // "image", "document", null
    val attachmentName: String? = null,
    val modelUsed: String = "gemini-3.5-flash",
    val isError: Boolean = false
)

@Entity(tableName = "app_shortcuts")
data class AppShortcutEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String, // "AI", "Google", "Social", "Productivity", "Media", "Dev", "Tools"
    val iconSymbol: String,
    val colorHex: String,
    val url: String,
    val isCustom: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val email: String,
    val name: String,
    val planId: String = "PRO", // "FREE", "PRO", "ULTRA"
    val creditsUsed: Int = 14,
    val creditsTotal: Int = 500,
    val voiceSpeed: Float = 1.0f,
    val voicePitch: Float = 1.0f,
    val language: String = "en-US", // "en-US", "hi-IN", "auto"
    val aiStyle: String = "Balanced", // "Concise", "Balanced", "Detailed", "Technical"
    val isLoggedIn: Boolean = true,
    val isAdmin: Boolean = false
)
