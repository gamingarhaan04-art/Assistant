package com.example.ui.voice

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.chat.ChatViewModel
import com.example.ui.components.FuturisticCard
import com.example.ui.components.JarvisVisualizer
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
import com.example.voice.JarvisVoiceState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveVoiceScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val isLiveActive by viewModel.isLiveActive.collectAsState()
    val liveDesc by viewModel.liveStateDescription.collectAsState()
    val liveMessages by viewModel.liveMessages.collectAsState()
    val voiceState by viewModel.voiceManager.voiceState.collectAsState()
    val audioAmp by viewModel.voiceManager.audioAmplitude.collectAsState()

    val transcriptState = rememberLazyListState()

    LaunchedEffect(liveMessages.size) {
        if (liveMessages.isNotEmpty()) {
            transcriptState.animateScrollToItem(liveMessages.size - 1)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_halo")
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo"
    )

    Scaffold(
        containerColor = JarvisBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎙", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "LIVE VOICE ASSISTANT",
                                color = JarvisOrange,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "MODEL: gemini-3.8-live (Real-Time Live API)",
                                color = JarvisCyan,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
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
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Live Status Banner
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isLiveActive) JarvisPanelDark else JarvisPanelSecondary)
                    .border(
                        1.dp,
                        if (isLiveActive) JarvisCyan.copy(alpha = 0.8f) else JarvisBorder,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isLiveActive) JarvisGreen else Color.Gray)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = liveDesc,
                    color = if (isLiveActive) JarvisGreen else JarvisTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Central Animated Visualizer
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLiveActive) {
                    Box(
                        modifier = Modifier
                            .size(200.dp * haloPulse)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        JarvisCyan.copy(alpha = 0.22f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                JarvisVisualizer(
                    state = if (isLiveActive) voiceState else JarvisVoiceState.IDLE,
                    amplitude = if (isLiveActive) audioAmp else 0f,
                    size = 180.dp,
                    onClick = { viewModel.toggleLiveSession() }
                )
            }

            // Real-Time Transcript View
            FuturisticCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp),
                glowBorder = isLiveActive
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "LIVE TRANSCRIPT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisGold,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "Bilingual: English & हिंदी",
                            fontSize = 10.sp,
                            color = JarvisTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (liveMessages.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isLiveActive) "Listening for your voice...\nSpeak in English or Hindi" else "Tap 'Start Live Link' to begin real-time speech conversation with gemini-3.8-live.",
                                color = JarvisTextMuted,
                                fontSize = 12.5.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            state = transcriptState,
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(liveMessages) { (speaker, text) ->
                                val isUser = speaker == "user"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isUser) Color(0xFF173257) else Color(0xFF0F1E2E))
                                            .border(1.dp, if (isUser) JarvisCyan.copy(alpha = 0.5f) else JarvisOrange.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = if (isUser) "COMMANDER" else "JARVIS (gemini-3.8-live)",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isUser) JarvisCyan else JarvisOrange
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = text,
                                                fontSize = 13.sp,
                                                color = JarvisTextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Live Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isLiveActive) {
                    Button(
                        onClick = { viewModel.startLiveSession() },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisOrange),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("start_live_voice_button")
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start Live Voice Link (gemini-3.8-live)",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.voiceManager.stopSpeaking() },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisPanelDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Interrupt", tint = JarvisGold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Interrupt", color = JarvisGold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.endLiveSession() },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisDanger),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("end_live_voice_button")
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = "End Live Link", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("End Live Link", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
