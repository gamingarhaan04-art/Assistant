package com.example.ui.onboarding

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserProfileEntity
import com.example.ui.chat.ChatViewModel
import com.example.ui.components.FuturisticCard
import com.example.ui.components.JarvisVisualizer
import com.example.ui.theme.JarvisAmberGlow
import com.example.ui.theme.JarvisBlack
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisOrange
import com.example.ui.theme.JarvisPanelDark
import com.example.ui.theme.JarvisPanelSecondary
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.voice.JarvisVoiceState
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    viewModel: ChatViewModel,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var commanderName by remember { mutableStateOf("Commander") }
    var selectedLang by remember { mutableStateOf("en-US") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F2338),
                        JarvisBlack
                    ),
                    radius = 1400f
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Glowing Arc Reactor Visualizer
            JarvisVisualizer(
                state = JarvisVoiceState.IDLE,
                size = 140.dp
            )

            Text(
                text = "MEET JARVIS AI",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = JarvisOrange,
                letterSpacing = 2.sp
            )

            Text(
                text = "Your intelligent assistant for conversational AI, natural voice, multimodal vision, documents, and everyday web tools.",
                fontSize = 13.5.sp,
                color = JarvisTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            // Features Overview
            FuturisticCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureRow(
                        icon = Icons.Default.Psychology,
                        title = "Next-Gen Neural Core",
                        desc = "Powered by Google Gemini with fast context handling."
                    )
                    FeatureRow(
                        icon = Icons.Default.Mic,
                        title = "Real-Time Voice Assistant",
                        desc = "Speak naturally in English or Hindi with audio speech synthesis."
                    )
                    FeatureRow(
                        icon = Icons.Default.Image,
                        title = "Visual Intelligence",
                        desc = "Upload photos, diagrams, and code for in-depth OCR and analysis."
                    )
                    FeatureRow(
                        icon = Icons.Default.Description,
                        title = "Document Summarizer",
                        desc = "Extract summaries, action items, and insights from text & docs."
                    )
                    FeatureRow(
                        icon = Icons.Default.Widgets,
                        title = "Web Apps Hub",
                        desc = "Quick access to YouTube, Spotify, WhatsApp, Maps & custom links."
                    )
                }
            }

            // Quick Setup
            FuturisticCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "INITIALIZE YOUR PROFILE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisGold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = commanderName,
                        onValueChange = { commanderName = it },
                        label = { Text("What should JARVIS call you?") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_name_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisOrange,
                            unfocusedBorderColor = JarvisBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Preferred Voice Language", fontSize = 12.sp, color = JarvisTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("en-US" to "English (US)", "hi-IN" to "हिंदी (Hindi)").forEach { (code, label) ->
                            val isSelected = selectedLang == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) JarvisCyan.copy(alpha = 0.2f) else JarvisPanelSecondary)
                                    .border(1.dp, if (isSelected) JarvisCyan else JarvisBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedLang = code }
                                    .padding(vertical = 10.dp),
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
                }
            }

            // Enter Button
            Button(
                onClick = {
                    scope.launch {
                        val profile = UserProfileEntity(
                            email = "commander@jarvis.ai",
                            name = commanderName.ifBlank { "Commander" },
                            language = selectedLang,
                            planId = "PRO"
                        )
                        viewModel.repository.saveProfile(profile)
                        viewModel.voiceManager.currentLanguage = selectedLang
                        onFinish()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = JarvisOrange
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_start_button")
            ) {
                Text(
                    text = "Launch JARVIS Assistant",
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Black)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun FeatureRow(
    icon: ImageVector,
    title: String,
    desc: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF14202E)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = JarvisOrange, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = JarvisTextPrimary)
            Text(text = desc, fontSize = 11.5.sp, color = JarvisTextMuted, lineHeight = 16.sp)
        }
    }
}
