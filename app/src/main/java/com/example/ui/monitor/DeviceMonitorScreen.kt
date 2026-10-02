package com.example.ui.monitor

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.example.ui.plans.SubscriptionPlansScreen
import com.example.ui.settings.SettingsScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.device.DeviceDiagnostics
import com.example.device.DeviceMonitorHelper
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
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceMonitorScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var diagnostics by remember { mutableStateOf(DeviceMonitorHelper.getDiagnostics(context)) }

    // Live Clock
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTime = timeFormat.format(now)
            currentDate = dateFormat.format(now)
            delay(1000)
        }
    }

    fun refreshDiagnostics() {
        diagnostics = DeviceMonitorHelper.getDiagnostics(context)
    }

    fun speakReport() {
        val speech = "Diagnostics report. This device is ${diagnostics.deviceName}. Battery is at ${diagnostics.batteryPct} percent ${if (diagnostics.isCharging) "and charging." else "."} Network status is ${diagnostics.networkType}."
        viewModel.speakMessage(speech)
    }

    fun openExternal(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    var selectedSubTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = JarvisBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🖥", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SYSTEM & DIAGNOSTICS",
                            color = JarvisOrange,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            letterSpacing = 1.5.sp
                        )
                    }
                },
                actions = {
                    if (selectedSubTab == 0) {
                        IconButton(
                            onClick = { refreshDiagnostics() },
                            modifier = Modifier.testTag("refresh_diagnostics_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = JarvisCyan)
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
        ) {
            // Sub-Tab Switcher
            TabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = JarvisPanelDark,
                contentColor = JarvisOrange,
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, JarvisBorder, RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = { Text("Telemetry", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = { Text("Plans & Credits", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedSubTab == 2,
                    onClick = { selectedSubTab = 2 },
                    text = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }

            when (selectedSubTab) {
                1 -> SubscriptionPlansScreen(viewModel = viewModel)
                2 -> SettingsScreen(viewModel = viewModel)
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Hero Time Card from Prototype
                        FuturisticCard(
                            modifier = Modifier.fillMaxWidth(),
                            glowBorder = true
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = if (currentTime.isNotEmpty()) currentTime else "--:--:--",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = JarvisTextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 2.sp
                                )
                                Text(
                                    text = if (currentDate.isNotEmpty()) currentDate else "Initializing...",
                                    fontSize = 13.sp,
                                    color = JarvisGold
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "\"The future is not something we predict, it's something we build.\"",
                                    fontSize = 12.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = JarvisTextSecondary
                                )
                                Text(
                                    text = "— JARVIS Neural Core",
                                    fontSize = 10.5.sp,
                                    color = JarvisAmberGlow,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Diagnostics Card
                        FuturisticCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "HARDWARE TELEMETRY",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = JarvisOrange,
                                        letterSpacing = 1.2.sp
                                    )
                                    Button(
                                        onClick = { speakReport() },
                                        colors = ButtonDefaults.buttonColors(containerColor = JarvisPanelSecondary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Audio Brief", fontSize = 11.sp, color = JarvisCyan)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Device Name
                                TelemetryRow(
                                    icon = Icons.Default.Smartphone,
                                    label = "Device",
                                    value = diagnostics.deviceName
                                )

                                // OS Version
                                TelemetryRow(
                                    icon = Icons.Default.Speed,
                                    label = "Operating System",
                                    value = diagnostics.osVersion
                                )

                                // Battery
                                val batColor = when {
                                    diagnostics.batteryPct <= 20 && !diagnostics.isCharging -> JarvisDanger
                                    diagnostics.batteryPct <= 50 -> JarvisGold
                                    else -> JarvisGreen
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF0E1C2C)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Battery", fontSize = 12.5.sp, color = JarvisTextSecondary, modifier = Modifier.weight(1f))

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${diagnostics.batteryPct}%" + (if (diagnostics.isCharging) " ⚡ (Charging)" else ""),
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = batColor
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { (diagnostics.batteryPct / 100f).coerceIn(0f, 1f) },
                                            color = batColor,
                                            trackColor = Color(0xFF152232),
                                            modifier = Modifier
                                                .width(90.dp)
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                        )
                                    }
                                }

                                HorizontalDivider(color = Color(0xFF14202E), modifier = Modifier.padding(vertical = 4.dp))

                                // Network
                                TelemetryRow(
                                    icon = Icons.Default.Wifi,
                                    label = "Network Link",
                                    value = diagnostics.networkType,
                                    valueColor = if (diagnostics.isOnline) JarvisGreen else JarvisDanger
                                )

                                // Memory
                                TelemetryRow(
                                    icon = Icons.Default.Memory,
                                    label = "RAM Capacity",
                                    value = "${diagnostics.ramAvailGb} GB avail / ${diagnostics.ramTotalGb} GB total"
                                )

                                // CPU Cores
                                TelemetryRow(
                                    icon = Icons.Default.Speed,
                                    label = "Compute Cores",
                                    value = "${diagnostics.cpuCores} Multithreaded Cores"
                                )
                            }
                        }

                        // Quick Actions from prototype
                        FuturisticCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "⚡ QUICK ACTIONS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisGold,
                                    letterSpacing = 1.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    QuickActionChip(
                                        symbol = "▶",
                                        label = "YouTube",
                                        onClick = { openExternal("https://youtube.com") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    QuickActionChip(
                                        symbol = "📍",
                                        label = "Maps",
                                        onClick = { openExternal("https://maps.google.com") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    QuickActionChip(
                                        symbol = "✉",
                                        label = "Gmail",
                                        onClick = { openExternal("https://mail.google.com") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    QuickActionChip(
                                        symbol = "✦",
                                        label = "ChatGPT",
                                        onClick = { openExternal("https://chat.openai.com") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = JarvisTextPrimary
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF0E1C2C)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = label, fontSize = 12.5.sp, color = JarvisTextSecondary, modifier = Modifier.weight(1f))
        Text(text = value, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
    HorizontalDivider(color = Color(0xFF14202E), modifier = Modifier.padding(vertical = 2.dp))
}

@Composable
fun QuickActionChip(
    symbol: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0A111B))
            .border(1.dp, JarvisBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Text(symbol, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = JarvisTextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}
