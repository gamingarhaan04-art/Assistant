package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        AppShortcutEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun jarvisDao(): JarvisDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): JarvisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisDatabase::class.java,
                    "jarvis_ai_database"
                )
                .addCallback(DatabaseCallback(scope))
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.jarvisDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: JarvisDao) {
                // Initial Default User Profile
                val defaultUser = UserProfileEntity(
                    email = "commander@jarvis.ai",
                    name = "Tony Stark",
                    planId = "PRO",
                    creditsUsed = 28,
                    creditsTotal = 500,
                    voiceSpeed = 1.0f,
                    voicePitch = 0.95f,
                    language = "en-US",
                    aiStyle = "Balanced",
                    isLoggedIn = true,
                    isAdmin = true
                )
                dao.saveUserProfile(defaultUser)

                // Initial Welcome Conversation
                val convId = "welcome_conv"
                val conv = ConversationEntity(
                    id = convId,
                    title = "JARVIS System Initialization",
                    isPinned = true
                )
                dao.insertConversation(conv)

                dao.insertMessage(
                    MessageEntity(
                        conversationId = convId,
                        role = "jarvis",
                        content = "Greetings, Commander. JARVIS AI online and standing by. All core diagnostic systems, neural models, voice protocols, and the Web Apps Hub are fully operational.\n\nHow may I assist you today?"
                    )
                )

                // Preset Web Apps from Prototype
                val presetApps = listOf(
                    AppShortcutEntity("youtube", "YouTube", "Watch videos & livestreams", "Media", "▶", "#FF5C5C", "https://youtube.com"),
                    AppShortcutEntity("whatsapp", "WhatsApp Web", "Encrypted messaging", "Social", "💬", "#25D366", "https://web.whatsapp.com"),
                    AppShortcutEntity("spotify", "Spotify", "Music & podcasts", "Media", "♪", "#2EE6A6", "https://open.spotify.com"),
                    AppShortcutEntity("chatgpt", "ChatGPT", "Conversational AI", "AI", "✦", "#EAF2F8", "https://chat.openai.com"),
                    AppShortcutEntity("gemini", "Gemini", "Google's flagship AI", "AI", "✨", "#5BC0FF", "https://gemini.google.com"),
                    AppShortcutEntity("claude", "Claude", "Anthropic assistant", "AI", "✺", "#FF8A1E", "https://claude.ai"),
                    AppShortcutEntity("google", "Google", "Search the web", "Google", "G", "#4285F4", "https://google.com"),
                    AppShortcutEntity("maps", "Google Maps", "Global GPS & traffic", "Google", "📍", "#34A853", "https://maps.google.com"),
                    AppShortcutEntity("earth", "Google Earth", "Satellite imagery", "Google", "🌍", "#4285F4", "https://earth.google.com"),
                    AppShortcutEntity("gmail", "Gmail", "Enterprise email", "Google", "✉", "#EA4335", "https://mail.google.com"),
                    AppShortcutEntity("drive", "Google Drive", "Cloud storage & sync", "Google", "▲", "#5BC0FF", "https://drive.google.com"),
                    AppShortcutEntity("calendar", "Google Calendar", "Schedule & tasks", "Google", "▦", "#5BC0FF", "https://calendar.google.com"),
                    AppShortcutEntity("docs", "Google Docs", "Word processing", "Productivity", "📄", "#5BC0FF", "https://docs.google.com"),
                    AppShortcutEntity("sheets", "Google Sheets", "Spreadsheets & data", "Productivity", "▤", "#2EE6A6", "https://sheets.google.com"),
                    AppShortcutEntity("slides", "Google Slides", "Presentations", "Productivity", "▥", "#FFA726", "https://slides.google.com"),
                    AppShortcutEntity("canva", "Canva", "Visual design suite", "Tools", "C", "#5BC0FF", "https://canva.com"),
                    AppShortcutEntity("figma", "Figma", "Collaborative UI/UX", "Dev", "◆", "#FF5C6A", "https://figma.com"),
                    AppShortcutEntity("github", "GitHub", "Code repositories & CI", "Dev", "⌥", "#EAF2F8", "https://github.com"),
                    AppShortcutEntity("telegram", "Telegram Web", "Speed & cloud chat", "Social", "✈", "#5BC0FF", "https://web.telegram.org"),
                    AppShortcutEntity("discord", "Discord", "Voice & community servers", "Social", "◎", "#8E7CFF", "https://discord.com")
                )
                dao.insertShortcuts(presetApps)
            }
        }
    }
}
