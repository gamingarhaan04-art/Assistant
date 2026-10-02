package com.example.ui.video

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.VeoVideoResponse
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VeoVideoScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var prompt by remember { mutableStateOf("") }
    var selectedAspectRatio by remember { mutableStateOf("16:9") } // "16:9" or "9:16"
    var selectedResolution by remember { mutableStateOf("1080p") }

    val isGenerating by viewModel.isGeneratingVeo.collectAsState()
    val progressStatus by viewModel.veoProgress.collectAsState()
    val history by viewModel.veoHistory.collectAsState()

    val presetPrompts = listOf(
        "Cinematic drone shot soaring over glowing neon skyscrapers at dusk with amber volumetric reflections",
        "Close-up of futuristic holographic arc reactor humming with glowing orange cyber circuits",
        "A hypercar speeding through a rainy futuristic canyon highway under dual moons, photorealistic 8k",
        "Macro focus on a high-tech robotic iris assembling itself with optical lens flare"
    )

    fun openVideoLink(uri: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open video URL", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = JarvisBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎬", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "VEO 3 VIDEO STUDIO",
                                color = JarvisOrange,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "MODEL: veo-3.1-fast-generate-preview",
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
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Prompt Card
            item {
                FuturisticCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "DESCRIBE YOUR SCENE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisGold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = prompt,
                            onValueChange = { prompt = it },
                            placeholder = {
                                Text(
                                    "Enter video scene description (lighting, motion, atmosphere)...",
                                    fontSize = 13.sp,
                                    color = JarvisTextMuted
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .testTag("veo_prompt_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JarvisOrange,
                                unfocusedBorderColor = JarvisBorder,
                                focusedTextColor = JarvisTextPrimary,
                                unfocusedTextColor = JarvisTextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Aspect Ratio Toggle: 16:9 vs 9:16 (Required by Feature Spec)
                        Text(
                            text = "ASPECT RATIO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisTextSecondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 16:9 Option
                            val is169 = selectedAspectRatio == "16:9"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (is169) JarvisOrange.copy(alpha = 0.2f) else JarvisPanelSecondary)
                                    .border(1.dp, if (is169) JarvisOrange else JarvisBorder, RoundedCornerShape(10.dp))
                                    .clickable { selectedAspectRatio = "16:9" }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🖥️", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "16:9 (Landscape)",
                                        fontSize = 12.sp,
                                        fontWeight = if (is169) FontWeight.Bold else FontWeight.Normal,
                                        color = if (is169) JarvisOrange else JarvisTextSecondary
                                    )
                                }
                            }

                            // 9:16 Option
                            val is916 = selectedAspectRatio == "9:16"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (is916) JarvisCyan.copy(alpha = 0.2f) else JarvisPanelSecondary)
                                    .border(1.dp, if (is916) JarvisCyan else JarvisBorder, RoundedCornerShape(10.dp))
                                    .clickable { selectedAspectRatio = "9:16" }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📱", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "9:16 (Portrait)",
                                        fontSize = 12.sp,
                                        fontWeight = if (is916) FontWeight.Bold else FontWeight.Normal,
                                        color = if (is916) JarvisCyan else JarvisTextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Generate Button
                        Button(
                            onClick = {
                                viewModel.generateVeoVideo(
                                    prompt = prompt.trim(),
                                    aspectRatio = selectedAspectRatio,
                                    resolution = selectedResolution
                                )
                            },
                            enabled = !isGenerating && prompt.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisOrange),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("generate_veo_button")
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Synthesizing Video with Veo 3...", color = Color.Black, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Video with Veo 3", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Progress notification
                        AnimatedVisibility(visible = isGenerating) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                Text(
                                    text = progressStatus,
                                    color = JarvisCyan,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // Quick Prompt Ideas
            item {
                Text(
                    text = "SAMPLE SCENARIOS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisGold,
                    letterSpacing = 1.sp
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(presetPrompts) { sample ->
                        Box(
                            modifier = Modifier
                                .width(240.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(JarvisPanelDark)
                                .border(1.dp, JarvisBorder, RoundedCornerShape(10.dp))
                                .clickable { prompt = sample }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = sample,
                                fontSize = 11.5.sp,
                                color = JarvisTextSecondary,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Output / Generated Videos
            item {
                Text(
                    text = "GENERATED VIDEOS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisOrange,
                    letterSpacing = 1.sp
                )
            }

            if (history.isEmpty() && !isGenerating) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No videos generated yet.\nDescribe a scene and choose 16:9 or 9:16 to create your first Veo 3 video.",
                            color = JarvisTextMuted,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                items(history) { video ->
                    VeoVideoCard(video = video, onOpen = { openVideoLink(it) })
                }
            }
        }
    }
}

@Composable
fun VeoVideoCard(
    video: VeoVideoResponse,
    onOpen: (String) -> Unit
) {
    FuturisticCard(
        modifier = Modifier.fillMaxWidth(),
        glowBorder = video.isDone
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with aspect ratio and state
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (video.isDone) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = if (video.isDone) JarvisGreen else JarvisGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (video.isDone) "RENDER COMPLETE" else "PIPELINE PROCESSING",
                        color = if (video.isDone) JarvisGreen else JarvisGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0F1E2E))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${video.aspectRatio} • ${video.resolution}",
                        color = JarvisCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Video Preview Placeholder Box matching Aspect Ratio
            val isPortrait = video.aspectRatio == "9:16"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isPortrait) 200.dp else 140.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF070E17))
                    .border(1.dp, JarvisBorder, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.PlayCircle,
                        contentDescription = "Play Video",
                        tint = JarvisOrange,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Veo 3 Neural Stream (${video.aspectRatio})",
                        color = JarvisGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = video.prompt,
                color = JarvisTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "ID: ${video.operationName.takeLast(24)}",
                color = JarvisTextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            if (video.downloadUrl != null || video.videoUri != null) {
                val targetUrl = video.downloadUrl ?: video.videoUri ?: ""
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onOpen(targetUrl) },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View / Download Video", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
