package com.example.ui.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.ui.components.FuturisticCard
import com.example.ui.components.JarvisVisualizer
import com.example.ui.theme.JarvisAmberGlow
import com.example.ui.theme.JarvisBlack
import com.example.ui.theme.JarvisBlueDark
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val conversations by viewModel.conversations.collectAsState()
    val currentConvId by viewModel.currentConversationId.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val attachmentDraft by viewModel.attachmentDraft.collectAsState()
    val streamingAnswer by viewModel.streamingAnswer.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val voiceState by viewModel.voiceManager.voiceState.collectAsState()
    val audioAmp by viewModel.voiceManager.audioAmplitude.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()

    var showModelSelectorMenu by remember { mutableStateOf(false) }
    var showRoleSelectorMenu by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Auto-scroll when new messages arrive
    LaunchedEffect(messages.size, streamingAnswer) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Pickers
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.attachImage(uri, "Image_${System.currentTimeMillis()}.jpg")
        }
    }

    val docPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.attachDocument(uri, "Doc_${System.currentTimeMillis()}.txt")
        }
    }

    var showRenameDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = JarvisPanelDark,
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    // Drawer Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "JARVIS",
                                color = JarvisOrange,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LOGS",
                                color = JarvisTextSecondary,
                                fontSize = 14.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.startNewConversation()
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.testTag("drawer_new_chat_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "New Mission", tint = JarvisCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Search missions...", fontSize = 13.sp, color = JarvisTextMuted) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = JarvisTextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisOrange,
                            unfocusedBorderColor = JarvisBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "RECENT PROTOCOLS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisGold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val filteredList = conversations.filter {
                        searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredList, key = { it.id }) { conv ->
                            val isSelected = conv.id == currentConvId
                            var menuExpanded by remember { mutableStateOf(false) }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) JarvisBlueDark else JarvisPanelSecondary)
                                    .border(
                                        1.dp,
                                        if (isSelected) JarvisCyan.copy(alpha = 0.6f) else JarvisBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        viewModel.selectConversation(conv.id)
                                        scope.launch { drawerState.close() }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) JarvisOrange else JarvisTextMuted)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = conv.title,
                                    color = if (isSelected) JarvisTextPrimary else JarvisTextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                Box {
                                    IconButton(
                                        onClick = { menuExpanded = true },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = "Options",
                                            tint = JarvisTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Rename") },
                                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                            onClick = {
                                                menuExpanded = false
                                                showRenameDialog = conv.id to conv.title
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete", color = JarvisDanger) },
                                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = JarvisDanger) },
                                            onClick = {
                                                menuExpanded = false
                                                viewModel.deleteConversation(conv.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = JarvisBorder, modifier = Modifier.padding(vertical = 12.dp))

                    Button(
                        onClick = {
                            viewModel.startNewConversation()
                            scope.launch { drawerState.close() }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("drawer_start_new_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("New Conversation", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = JarvisBlack,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(JarvisBlack.copy(alpha = 0.95f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("open_sidebar_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Missions", tint = JarvisTextPrimary)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "JARVIS",
                                color = JarvisOrange,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(JarvisPanelSecondary)
                                    .border(1.dp, JarvisBorder, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = when (voiceState) {
                                        JarvisVoiceState.IDLE -> "ONLINE"
                                        JarvisVoiceState.LISTENING -> "LISTENING"
                                        JarvisVoiceState.THINKING -> "THINKING"
                                        JarvisVoiceState.SPEAKING -> "SPEAKING"
                                        JarvisVoiceState.ERROR -> "ALERT"
                                    },
                                    color = when (voiceState) {
                                        JarvisVoiceState.IDLE -> JarvisGreen
                                        JarvisVoiceState.LISTENING -> JarvisGold
                                        JarvisVoiceState.THINKING -> JarvisCyan
                                        JarvisVoiceState.SPEAKING -> JarvisGreen
                                        JarvisVoiceState.ERROR -> JarvisDanger
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.startNewConversation() },
                            modifier = Modifier.testTag("top_bar_new_chat")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "New Chat", tint = JarvisCyan)
                        }
                    }

                    // Model & Chatbot Role Selection Bar (Feature: gemini-3.1-pro-preview / gemini-3.5-flash / gemini-3.1-flash-lite)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(JarvisPanelDark)
                            .border(1.dp, JarvisBorder)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        // Role Selector
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F1E2E))
                                    .border(1.dp, JarvisBorder, RoundedCornerShape(8.dp))
                                    .clickable { showRoleSelectorMenu = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("chatbot_role_selector")
                            ) {
                                Text(currentRole.icon, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(currentRole.displayName, color = JarvisGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            DropdownMenu(
                                expanded = showRoleSelectorMenu,
                                onDismissRequest = { showRoleSelectorMenu = false }
                            ) {
                                ChatbotRole.values().forEach { role ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(role.icon, fontSize = 13.sp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(role.displayName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                }
                                                Text("Role prompt & default: ${role.defaultModel}", fontSize = 10.sp, color = JarvisTextMuted)
                                            }
                                        },
                                        onClick = {
                                            viewModel.selectRole(role)
                                            showRoleSelectorMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Model Selector Chip
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF14202E))
                                    .border(1.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .clickable { showModelSelectorMenu = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("gemini_model_selector")
                            ) {
                                val modelTag = when (selectedModel) {
                                    "gemini-3.1-pro-preview" -> "🧠 3.1-Pro (Complex)"
                                    "gemini-3.1-flash-lite" -> "⚡ Flash-Lite (Fast)"
                                    else -> "✨ 3.5-Flash (General)"
                                }
                                Text(modelTag, color = JarvisCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            DropdownMenu(
                                expanded = showModelSelectorMenu,
                                onDismissRequest = { showModelSelectorMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("⚡ gemini-3.1-flash-lite", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("Fast tasks • Instant low-latency execution", fontSize = 10.sp, color = JarvisTextMuted)
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectModel("gemini-3.1-flash-lite")
                                        showModelSelectorMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("✨ gemini-3.5-flash", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("General tasks • Balanced multimodal intelligence", fontSize = 10.sp, color = JarvisTextMuted)
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectModel("gemini-3.5-flash")
                                        showModelSelectorMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("🧠 gemini-3.1-pro-preview", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("Complex tasks • Deep reasoning, STEM & coding", fontSize = 10.sp, color = JarvisTextMuted)
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectModel("gemini-3.1-pro-preview")
                                        showModelSelectorMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                // Bottom Composer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(JarvisPanelDark)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Attachment Draft Preview
                    AnimatedVisibility(visible = attachmentDraft != null) {
                        attachmentDraft?.let { draft ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(JarvisBlueDark)
                                    .border(1.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (draft.type == "image") Icons.Default.Image else Icons.Default.AttachFile,
                                    contentDescription = null,
                                    tint = JarvisCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = draft.name,
                                    color = JarvisTextPrimary,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.clearAttachment() },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = JarvisTextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    // Input Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Image attachment button
                        IconButton(
                            onClick = { imagePicker.launch("image/*") },
                            modifier = Modifier.testTag("attach_image_button")
                        ) {
                            Icon(Icons.Default.Image, contentDescription = "Attach Image", tint = JarvisCyan)
                        }

                        // Doc attachment button
                        IconButton(
                            onClick = { docPicker.launch("*/*") },
                            modifier = Modifier.testTag("attach_document_button")
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Attach Document", tint = JarvisTextSecondary)
                        }

                        // Text Field
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { viewModel.updateInputText(it) },
                            placeholder = {
                                Text(
                                    if (voiceState == JarvisVoiceState.LISTENING) "Listening..." else "Ask JARVIS anything...",
                                    fontSize = 14.sp,
                                    color = JarvisTextMuted
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("message_input_field"),
                            shape = RoundedCornerShape(14.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JarvisOrange,
                                unfocusedBorderColor = JarvisBorder,
                                focusedTextColor = JarvisTextPrimary,
                                unfocusedTextColor = JarvisTextPrimary,
                                cursorColor = JarvisOrange
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Mic Button
                        val micBgColor by animateColorAsState(
                            targetValue = if (voiceState == JarvisVoiceState.LISTENING) JarvisDanger else JarvisPanelSecondary,
                            label = "mic_bg"
                        )
                        IconButton(
                            onClick = { viewModel.toggleVoiceListening() },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(micBgColor)
                                .border(1.dp, if (voiceState == JarvisVoiceState.LISTENING) JarvisDanger else JarvisBorder, RoundedCornerShape(12.dp))
                                .testTag("mic_button")
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = if (voiceState == JarvisVoiceState.LISTENING) Color.White else JarvisOrange
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Send / Stop Button
                        if (isGenerating) {
                            IconButton(
                                onClick = { viewModel.stopGeneration() },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(JarvisDanger)
                                    .testTag("stop_generation_button")
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White)
                            }
                        } else {
                            IconButton(
                                onClick = { viewModel.sendMessage() },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(listOf(JarvisOrange, JarvisGold))
                                    )
                                    .testTag("send_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Send", tint = Color.Black)
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF0F2338).copy(alpha = 0.4f),
                                JarvisBlack
                            ),
                            radius = 1200f
                        )
                    )
            ) {
                if (messages.isEmpty() && !isGenerating) {
                    // Empty state: show JARVIS Visualizer Hero
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        JarvisVisualizer(
                            state = voiceState,
                            amplitude = audioAmp,
                            size = 180.dp,
                            onClick = { viewModel.toggleVoiceListening() }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "JARVIS AI ASSISTANT",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = JarvisOrange,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Tap the arc reactor or mic to talk. You can ask complex questions, upload images, or summarize documents in English & Hindi.",
                            fontSize = 13.5.sp,
                            color = JarvisTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.widthIn(max = 380.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Quick prompt suggestions
                        val suggestions = listOf(
                            "What is your diagnostic capability?",
                            "Help me summarize key points from my text",
                            "नमस्ते जार्विस, आप कैसे मदद कर सकते हैं?"
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.widthIn(max = 420.dp)
                        ) {
                            suggestions.forEach { prompt ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(JarvisPanelDark)
                                        .border(1.dp, JarvisBorder, RoundedCornerShape(12.dp))
                                        .clickable {
                                            viewModel.updateInputText(prompt)
                                            viewModel.sendMessage()
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("⚡", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = prompt,
                                        color = JarvisTextPrimary,
                                        fontSize = 12.5.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Chat messages list
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            MessageBubble(
                                message = msg,
                                onCopy = { viewModel.copyMessage(msg.content) },
                                onSpeak = { viewModel.speakMessage(msg.content) },
                                onEdit = { viewModel.editUserMessage(msg) },
                                onRetry = { viewModel.retryLastMessage() }
                            )
                        }

                        // Live streaming or thinking state
                        if (isGenerating) {
                            item {
                                JarvisThinkingBubble(streamingAnswer = streamingAnswer)
                            }
                        }
                    }
                }
            }
        }
    }

    // Rename Dialog
    if (showRenameDialog != null) {
        val (id, initialTitle) = showRenameDialog!!
        var renameText by remember { mutableStateOf(initialTitle) }

        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename Protocol", color = JarvisOrange) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisOrange,
                        unfocusedBorderColor = JarvisBorder,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.renameConversation(id, renameText)
                        showRenameDialog = null
                    }
                ) {
                    Text("Save", color = JarvisOrange, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) {
                    Text("Cancel", color = JarvisTextMuted)
                }
            },
            containerColor = JarvisPanelDark
        )
    }
}

@Composable
fun MessageBubble(
    message: MessageEntity,
    onCopy: () -> Unit,
    onSpeak: () -> Unit,
    onEdit: () -> Unit,
    onRetry: () -> Unit
) {
    val isUser = message.role == "user"
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // JARVIS Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(JarvisOrange, Color(0xFF0F1A28))
                        )
                    )
                    .border(1.dp, JarvisOrange.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🤖", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(
                        if (isUser) JarvisBlueDark
                        else if (message.isError) JarvisDanger.copy(alpha = 0.15f)
                        else JarvisPanelDark
                    )
                    .border(
                        1.dp,
                        if (isUser) JarvisCyan.copy(alpha = 0.4f)
                        else if (message.isError) JarvisDanger
                        else JarvisBorder,
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .padding(14.dp)
            ) {
                Column {
                    // Attachment if any
                    if (message.attachmentName != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.3f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (message.attachmentType == "image") Icons.Default.Image else Icons.Default.AttachFile,
                                contentDescription = null,
                                tint = JarvisCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = message.attachmentName,
                                fontSize = 11.sp,
                                color = JarvisCyan,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = message.content,
                        color = if (message.isError) JarvisDanger else JarvisTextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            // Message Footnote & Action row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
            ) {
                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    color = JarvisTextMuted
                )

                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = JarvisTextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                }

                if (!isUser) {
                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak",
                            tint = JarvisTextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit message",
                            tint = JarvisTextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                if (message.isError) {
                    IconButton(
                        onClick = onRetry,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Retry",
                            tint = JarvisOrange,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JarvisThinkingBubble(streamingAnswer: String?) {
    val infiniteTransition = rememberInfiniteTransition(label = "thinking_anim")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(JarvisPanelDark)
                .border(1.dp, JarvisCyan.copy(alpha = alphaAnim), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("⚡", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                .background(JarvisPanelDark)
                .border(1.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(14.dp)
                .widthIn(max = 320.dp)
        ) {
            if (!streamingAnswer.isNullOrEmpty()) {
                Text(
                    text = streamingAnswer,
                    color = JarvisTextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(JarvisCyan.copy(alpha = alphaAnim))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "JARVIS is computing response...",
                        color = JarvisCyan,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
