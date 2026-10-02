package com.example.ui.plans

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class PricingPlan(
    val id: String,
    val name: String,
    val priceUsd: String,
    val priceInr: String,
    val totalCredits: Int,
    val tag: String?,
    val features: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionPlansScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userProfile by viewModel.userProfile.collectAsState()

    var isAnnual by remember { mutableStateOf(false) }
    var currencyTab by remember { mutableStateOf(0) } // 0 for INR (Razorpay), 1 for USD (Stripe)
    var checkoutPlan by remember { mutableStateOf<PricingPlan?>(null) }
    var isProcessingPayment by remember { mutableStateOf(false) }
    var paymentReceipt by remember { mutableStateOf<String?>(null) }

    val currentPlanId = userProfile?.planId ?: "PRO"
    val creditsUsed = userProfile?.creditsUsed ?: 28
    val creditsTotal = userProfile?.creditsTotal ?: 500

    val plans = listOf(
        PricingPlan(
            id = "FREE",
            name = "Starter",
            priceUsd = "$0",
            priceInr = "₹0",
            totalCredits = 50,
            tag = null,
            features = listOf(
                "Standard speed neural queries",
                "Basic English & Hindi chat",
                "50 credits per month",
                "Web Apps Hub access",
                "Device telemetry monitor"
            )
        ),
        PricingPlan(
            id = "PRO",
            name = "JARVIS Pro",
            priceUsd = if (isAnnual) "$11.99/mo" else "$14.99/mo",
            priceInr = if (isAnnual) "₹799/mo" else "₹999/mo",
            totalCredits = 500,
            tag = "MOST POPULAR",
            features = listOf(
                "High-speed priority response",
                "Multimodal image analysis & OCR",
                "Document & PDF summarization",
                "Natural voice STT & TTS",
                "500 mission credits / month",
                "Continuous multi-turn memory",
                "Custom web apps shortcuts"
            )
        ),
        PricingPlan(
            id = "ULTRA",
            name = "Ultra Enterprise",
            priceUsd = if (isAnnual) "$23.99/mo" else "$29.99/mo",
            priceInr = if (isAnnual) "₹1599/mo" else "₹1999/mo",
            totalCredits = 2500,
            tag = "COMMANDER TIER",
            features = listOf(
                "Unrestricted compute headroom",
                "Largest document context window",
                "Full Admin & Developer Dashboard",
                "2,500 credits with rollover",
                "Custom system prompt tuning",
                "Dedicated server link & priority SLA"
            )
        )
    )

    Scaffold(
        containerColor = JarvisBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SUBSCRIPTIONS & CREDITS",
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
            // Usage Card
            FuturisticCard(
                modifier = Modifier.fillMaxWidth(),
                glowBorder = true
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "ACTIVE SUBSCRIPTION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisGold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tier: $currentPlanId PROTOCOL",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = JarvisTextPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(JarvisGreen.copy(alpha = 0.15f))
                                .border(1.dp, JarvisGreen, RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = JarvisGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val usedRatio = (creditsUsed.toFloat() / creditsTotal.toFloat()).coerceIn(0f, 1f)
                    Text(
                        text = "Credits Consumed: $creditsUsed / $creditsTotal",
                        fontSize = 13.sp,
                        color = JarvisTextSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { usedRatio },
                        color = if (usedRatio > 0.8f) JarvisDanger else JarvisOrange,
                        trackColor = Color(0xFF152232),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Billing cycle refreshes in 24 days. Server credentials authenticated.",
                        fontSize = 11.sp,
                        color = JarvisTextMuted
                    )
                }
            }

            // Billing Region Switcher
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = currencyTab,
                    containerColor = JarvisPanelDark,
                    contentColor = JarvisOrange,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, JarvisBorder, RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = currencyTab == 0,
                        onClick = { currencyTab = 0 },
                        text = { Text("India (INR / Razorpay)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = currencyTab == 1,
                        onClick = { currencyTab = 1 },
                        text = { Text("Global (USD / Stripe)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            // Plan Cards
            plans.forEach { plan ->
                val isCurrent = plan.id == currentPlanId
                val priceText = if (currencyTab == 0) plan.priceInr else plan.priceUsd

                FuturisticCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowBorder = isCurrent || plan.tag != null
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                if (plan.tag != null) {
                                    Text(
                                        text = plan.tag,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = JarvisGold,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Text(
                                    text = plan.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisTextPrimary
                                )
                            }

                            Text(
                                text = priceText,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = JarvisOrange
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Features List
                        plan.features.forEach { feat ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 3.dp)
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = JarvisGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = feat,
                                    fontSize = 12.5.sp,
                                    color = JarvisTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isCurrent) {
                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "Current active plan", Toast.LENGTH_SHORT).show()
                                },
                                enabled = false,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Current Plan", color = JarvisGold)
                            }
                        } else {
                            Button(
                                onClick = { checkoutPlan = plan },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (plan.id == "ULTRA") JarvisGold else JarvisOrange
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("upgrade_${plan.id.lowercase()}_button")
                            ) {
                                Text(
                                    text = if (plan.id == "FREE") "Switch to Starter" else "Activate ${plan.name}",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Checkout / Payment Modal Simulation
    if (checkoutPlan != null) {
        val plan = checkoutPlan!!
        val provider = if (currencyTab == 0) "Razorpay Gateway (UPI / NetBanking / Cards)" else "Stripe Secure Checkout"
        val price = if (currencyTab == 0) plan.priceInr else plan.priceUsd

        AlertDialog(
            onDismissRequest = { if (!isProcessingPayment) checkoutPlan = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payment, contentDescription = null, tint = JarvisOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Secure Checkout", color = JarvisOrange, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Plan: ${plan.name}", color = JarvisTextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("Amount: $price", color = JarvisGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Provider: $provider", color = JarvisCyan, fontSize = 12.sp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF070C14))
                            .border(1.dp, JarvisBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "🔒 256-bit Encrypted Tokenization.\nCredentials are verified server-side with zero exposure of API or secret keys.",
                            color = JarvisTextMuted,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }

                    if (isProcessingPayment) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Verifying payment webhook...", color = JarvisCyan, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isProcessingPayment = true
                        scope.launch {
                            kotlinx.coroutines.delay(1200) // Realistic secure handshake
                            val txnId = "TXN_" + UUID.randomUUID().toString().take(10).uppercase()
                            viewModel.repository.upgradeSubscription(plan.id, plan.totalCredits)
                            isProcessingPayment = false
                            checkoutPlan = null
                            paymentReceipt = txnId
                            Toast.makeText(context, "${plan.name} Activated successfully!", Toast.LENGTH_LONG).show()
                        }
                    },
                    enabled = !isProcessingPayment,
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisOrange)
                ) {
                    Text("Confirm & Pay", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { checkoutPlan = null },
                    enabled = !isProcessingPayment
                ) {
                    Text("Cancel", color = JarvisTextMuted)
                }
            },
            containerColor = JarvisPanelDark
        )
    }

    // Payment Success Receipt Modal
    if (paymentReceipt != null) {
        AlertDialog(
            onDismissRequest = { paymentReceipt = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = JarvisGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Payment Verified", color = JarvisGreen, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Your subscription is active with full neural bandwidth.", color = JarvisTextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Receipt ID: $paymentReceipt", color = JarvisCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Timestamp: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}", color = JarvisTextMuted, fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { paymentReceipt = null },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisGreen)
                ) {
                    Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = JarvisPanelDark
        )
    }
}
