package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.chat.ChatScreen
import com.example.ui.chat.ChatViewModel
import com.example.ui.hub.WebAppsHubScreen
import com.example.ui.monitor.DeviceMonitorScreen
import com.example.ui.theme.JarvisBlack
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisOrange
import com.example.ui.theme.JarvisPanelDark
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.video.VeoVideoScreen
import com.example.ui.voice.LiveVoiceScreen

enum class JarvisTab(val label: String, val icon: ImageVector) {
    CHAT("Chat", Icons.AutoMirrored.Filled.Chat),
    LIVE_VOICE("Live API", Icons.Default.Mic),
    VEO_VIDEO("Veo 3", Icons.Default.Videocam),
    APPS_HUB("Apps", Icons.Default.Apps),
    SYSTEM("System", Icons.Default.MonitorHeart)
}

@Composable
fun JarvisMainScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableStateOf(JarvisTab.CHAT) }

    // Intercept back button to return to CHAT tab
    BackHandler(enabled = selectedTab != JarvisTab.CHAT) {
        selectedTab = JarvisTab.CHAT
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(JarvisBlack)) {
        val isWideScreen = maxWidth > 600.dp

        if (isWideScreen) {
            // Tablet / Foldable layout with NavigationRail
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = JarvisPanelDark,
                    contentColor = JarvisOrange,
                    modifier = Modifier
                        .fillMaxHeight()
                        .border(width = 1.dp, color = JarvisBorder)
                ) {
                    JarvisTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    tint = if (isSelected) JarvisOrange else JarvisTextMuted
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontSize = 11.sp,
                                    color = if (isSelected) JarvisOrange else JarvisTextMuted
                                )
                            },
                            colors = NavigationRailItemDefaults.colors(
                                indicatorColor = JarvisOrange.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_rail_${tab.name.lowercase()}")
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ScreenContent(selectedTab = selectedTab, viewModel = viewModel)
                }
            }
        } else {
            // Standard Phone layout with Bottom NavigationBar
            Scaffold(
                containerColor = JarvisBlack,
                bottomBar = {
                    NavigationBar(
                        containerColor = JarvisPanelDark,
                        contentColor = JarvisOrange,
                        tonalElevation = 6.dp,
                        modifier = Modifier.border(width = 1.dp, color = JarvisBorder)
                    ) {
                        JarvisTab.values().forEach { tab ->
                            val isSelected = selectedTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { selectedTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.label,
                                        tint = if (isSelected) JarvisOrange else JarvisTextMuted,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        fontSize = 10.sp,
                                        color = if (isSelected) JarvisOrange else JarvisTextMuted
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = JarvisOrange.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    ScreenContent(selectedTab = selectedTab, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun ScreenContent(
    selectedTab: JarvisTab,
    viewModel: ChatViewModel
) {
    when (selectedTab) {
        JarvisTab.CHAT -> ChatScreen(viewModel = viewModel)
        JarvisTab.LIVE_VOICE -> LiveVoiceScreen(viewModel = viewModel)
        JarvisTab.VEO_VIDEO -> VeoVideoScreen(viewModel = viewModel)
        JarvisTab.APPS_HUB -> WebAppsHubScreen(viewModel = viewModel)
        JarvisTab.SYSTEM -> DeviceMonitorScreen(viewModel = viewModel)
    }
}
