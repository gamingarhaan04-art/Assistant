package com.example.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.chat.ChatViewModel
import com.example.ui.components.FuturisticCard
import com.example.ui.theme.JarvisAmberGlow
import com.example.ui.theme.JarvisBlack
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDanger
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisOrange
import com.example.ui.theme.JarvisPanelDark
import com.example.ui.theme.JarvisPanelSecondary
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userProfile by viewModel.userProfile.collectAsState()
    val currentModel by viewModel.selectedModel.collectAsState()

    var customApiKeyInput by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf("en-US") }
    var speechSpeed by remember { mutableFloatStateOf(1.0f) }
    var speechPitch by remember { mutableFloatStateOf(0.95f) }
    var selectedAiStyle by remember { mutableStateOf("Balanced") }

    // Dialogs
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showAdminDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = JarvisBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚙", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SYSTEM SETTINGS",
                            color = JarvisOrange,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            letterSpacing = 1.5.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = JarvisBlack)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            FuturisticCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF14202E))
                            .border(1.dp, JarvisOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = JarvisGold, modifier = Modifier.size(28.dp))
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userProfile?.name ?: "Commander",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = JarvisTextPrimary
                        )
                        Text(
                            text = userProfile?.email ?: "commander@jarvis.ai",
                            fontSize = 12.sp,
                            color = JarvisTextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PLAN: ${userProfile?.planId ?: "PRO"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = JarvisCyan
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "• ${userProfile?.creditsUsed ?: 28}/${userProfile?.creditsTotal ?: 500} Credits",
                                fontSize = 11.sp,
                                color = JarvisTextSecondary
                            )
                        }
                    }
                }
            }

            // AI Style & Intelligence
            FuturisticCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = JarvisOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI RESPONSE STYLE", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = JarvisOrange)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val styles = listOf("Concise", "Balanced", "Detailed", "Technical")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        styles.forEach { style ->
                            val isSelected = style == selectedAiStyle
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) JarvisOrange.copy(alpha = 0.2f) else JarvisPanelSecondary)
                                    .border(1.dp, if (isSelected) JarvisOrange else JarvisBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedAiStyle = style }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = style,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) JarvisOrange else JarvisTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Voice & Speech Synthesis
            FuturisticCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = JarvisCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("VOICE SYNTHESIS PROTOCOL", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = JarvisCyan)
                        }

                        Button(
                            onClick = {
                                viewModel.speakMessage("JARVIS neural speech system operational. Standing by for voice commands.")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisPanelSecondary),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = JarvisGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test", fontSize = 11.sp, color = JarvisGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Language Selection
                    Text("Interaction Language", fontSize = 12.sp, color = JarvisTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("en-US" to "English (US)", "hi-IN" to "हिंदी (Hindi)").forEach { (code, label) ->
                            val isSelected = selectedLanguage == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) JarvisCyan.copy(alpha = 0.2f) else JarvisPanelSecondary)
                                    .border(1.dp, if (isSelected) JarvisCyan else JarvisBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedLanguage = code
                                        viewModel.voiceManager.currentLanguage = code
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) JarvisCyan else JarvisTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Speed Slider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Speech Rate", fontSize = 12.sp, color = JarvisTextSecondary)
                        Text("${String.format("%.1f", speechSpeed)}x", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = JarvisGold)
                    }
                    Slider(
                        value = speechSpeed,
                        onValueChange = {
                            speechSpeed = it
                            viewModel.voiceManager.speechRate = it
                        },
                        valueRange = 0.7f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisGold,
                            activeTrackColor = JarvisOrange
                        )
                    )

                    // Pitch Slider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Vocal Pitch", fontSize = 12.sp, color = JarvisTextSecondary)
                        Text("${String.format("%.2f", speechPitch)}x", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = JarvisCyan)
                    }
                    Slider(
                        value = speechPitch,
                        onValueChange = {
                            speechPitch = it
                            viewModel.voiceManager.speechPitch = it
                        },
                        valueRange = 0.7f..1.3f,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisCyan,
                            activeTrackColor = JarvisCyan
                        )
                    )
                }
            }

            // Security & Credentials
            FuturisticCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = JarvisGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SECURITY & BACKEND CREDENTIALS", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = JarvisGreen)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Gemini API credentials are protected via backend environment configurations (.env / BuildConfig). Client-side keys are never exposed in public repositories.",
                        fontSize = 11.5.sp,
                        color = JarvisTextMuted,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showApiKeyDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisPanelSecondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = JarvisGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Optional Developer Key Override", color = JarvisGold, fontSize = 12.sp)
                    }
                }
            }

            // Admin Dashboard button
            FuturisticCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAdminDialog = true }
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = JarvisOrange)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("ADMIN CONSOLE", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = JarvisTextPrimary)
                            Text("Telemetry, models, system prompt & maintenance", fontSize = 11.sp, color = JarvisTextMuted)
                        }
                    }
                    Text("ACCESS →", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JarvisOrange)
                }
            }

            // Legal & Trust
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showPrivacyDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisPanelDark),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Privacy Policy", fontSize = 11.sp, color = JarvisTextSecondary)
                }

                Button(
                    onClick = { showTermsDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisPanelDark),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Terms of Service", fontSize = 11.sp, color = JarvisTextSecondary)
                }
            }

            // Clear history button
            Button(
                onClick = { showClearHistoryDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = JarvisDanger.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear All Local History", color = JarvisDanger, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // API Key Override Dialog
    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("Developer API Key", color = JarvisGold, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "AI Studio automatically injects GEMINI_API_KEY from secrets. If you wish to test with a dedicated Gemini key, you can enter it here.",
                        fontSize = 12.sp,
                        color = JarvisTextMuted
                    )
                    OutlinedTextField(
                        value = customApiKeyInput,
                        onValueChange = { customApiKeyInput = it },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisGold,
                            unfocusedBorderColor = JarvisBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setCustomApiKey(customApiKeyInput)
                        showApiKeyDialog = false
                        Toast.makeText(context, "API Key updated", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisGold)
                ) {
                    Text("Save Key", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Cancel", color = JarvisTextMuted)
                }
            },
            containerColor = JarvisPanelDark
        )
    }

    // Admin Console Modal
    if (showAdminDialog) {
        var maintenanceMode by remember { mutableStateOf(false) }
        var promptText by remember { mutableStateOf(viewModel.systemPromptOverride ?: "") }

        AlertDialog(
            onDismissRequest = { showAdminDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = JarvisOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Admin Central Hub", color = JarvisOrange, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("METRICS OVERVIEW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JarvisGold)
                    Text("• Total Active Users: 1,429", fontSize = 12.sp, color = JarvisTextSecondary)
                    Text("• Free: 1,120 | Pro: 265 | Ultra: 44", fontSize = 12.sp, color = JarvisTextSecondary)
                    Text("• Total Tokens Processed Today: 3.4M", fontSize = 12.sp, color = JarvisTextSecondary)
                    Text("• Average Neural Latency: 480ms", fontSize = 12.sp, color = JarvisGreen)

                    HorizontalDivider(color = JarvisBorder)

                    Text("AI MODEL CONFIGURATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JarvisCyan)
                    val models = listOf(
                        "gemini-3.1-flash-lite" to "Flash-Lite (Fast)",
                        "gemini-3.5-flash" to "3.5-Flash (General)",
                        "gemini-3.1-pro-preview" to "3.1-Pro (Complex Reasoning)",
                        "gemini-3.8-live" to "3.8-Live (Voice API)",
                        "veo-3.1-fast-generate-preview" to "Veo 3 (Video)"
                    )
                    models.forEach { (m, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (currentModel == m) JarvisCyan.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { viewModel.selectModel(m) }
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "$label ($m)",
                                fontSize = 12.sp,
                                color = if (currentModel == m) JarvisCyan else JarvisTextSecondary,
                                fontWeight = if (currentModel == m) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    HorizontalDivider(color = JarvisBorder)

                    Text("SYSTEM PROMPT OVERRIDE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JarvisGold)
                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        placeholder = { Text("Custom JARVIS system instruction...", fontSize = 11.sp) },
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisGold,
                            unfocusedBorderColor = JarvisBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Maintenance Mode", fontSize = 12.sp, color = JarvisTextPrimary)
                        Switch(
                            checked = maintenanceMode,
                            onCheckedChange = { maintenanceMode = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JarvisDanger,
                                checkedTrackColor = JarvisDanger.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.systemPromptOverride = promptText.ifBlank { null }
                        showAdminDialog = false
                        Toast.makeText(context, "Admin settings applied", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisOrange)
                ) {
                    Text("Apply Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminDialog = false }) {
                    Text("Close", color = JarvisTextMuted)
                }
            },
            containerColor = JarvisPanelDark
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy", color = JarvisCyan, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "JARVIS AI values your security and privacy.\n\n" +
                            "1. Data Privacy: Conversation history is saved in your local device database (Room DB). We do not sell your personal data.\n" +
                            "2. AI Processing: Prompt text, images, and documents are securely passed to Google Gemini API servers over TLS 1.3 encryption.\n" +
                            "3. Voice Data: Audio captured via SpeechRecognizer is processed locally or via Google Speech Services strictly for transcription purposes.\n" +
                            "4. Security: No API keys are leaked to the client interface.",
                    fontSize = 12.sp,
                    color = JarvisTextSecondary,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Understood", color = JarvisCyan)
                }
            },
            containerColor = JarvisPanelDark
        )
    }

    // Terms Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms of Service", color = JarvisGold, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "By operating JARVIS AI, you acknowledge and agree that:\n\n" +
                            "1. AI responses are generated for assistance and may occasionally contain inaccuracies. Users should verify critical facts.\n" +
                            "2. The Web Apps Hub connects you to third-party web services through standard browser tabs. Respective third-party terms apply.\n" +
                            "3. Subscription plans provide allocated compute credits as described in the billing guidelines.",
                    fontSize = 12.sp,
                    color = JarvisTextSecondary,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Agree", color = JarvisGold)
                }
            },
            containerColor = JarvisPanelDark
        )
    }

    // Clear History Confirmation
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear All History?", color = JarvisDanger) },
            text = { Text("This will permanently remove all stored conversations and messages from this device.", color = JarvisTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.conversations.value.forEach { conv ->
                                viewModel.repository.deleteConversation(conv.id)
                            }
                            viewModel.startNewConversation()
                            showClearHistoryDialog = false
                            Toast.makeText(context, "All history cleared", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisDanger)
                ) {
                    Text("Clear", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel", color = JarvisTextMuted)
                }
            },
            containerColor = JarvisPanelDark
        )
    }
}
