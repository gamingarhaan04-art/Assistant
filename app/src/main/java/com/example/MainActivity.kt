package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.chat.ChatViewModel
import com.example.ui.navigation.JarvisMainScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.theme.JarvisBlack
import com.example.ui.theme.JarvisTheme

class MainActivity : ComponentActivity() {

    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            JarvisTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = JarvisBlack
                ) {
                    val userProfile by chatViewModel.userProfile.collectAsState()
                    var hasCompletedOnboarding by remember { mutableStateOf(false) }

                    LaunchedEffect(userProfile) {
                        if (userProfile != null) {
                            hasCompletedOnboarding = true
                        }
                    }

                    // Audio permission launcher
                    val audioPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { _ ->
                        // Permission result handled
                    }

                    LaunchedEffect(Unit) {
                        val hasMic = ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!hasMic) {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }

                    if (!hasCompletedOnboarding && userProfile == null) {
                        OnboardingScreen(
                            viewModel = chatViewModel,
                            onFinish = { hasCompletedOnboarding = true }
                        )
                    } else {
                        JarvisMainScreen(viewModel = chatViewModel)
                    }
                }
            }
        }
    }
}
