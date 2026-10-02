package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisBorderGlow
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisOrange
import com.example.ui.theme.JarvisPanelDark
import com.example.ui.theme.JarvisPanelSecondary

@Composable
fun FuturisticCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    glowBorder: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = JarvisPanelDark.copy(alpha = 0.92f)
        ),
        border = BorderStroke(
            1.dp,
            if (glowBorder) JarvisOrange.copy(alpha = 0.5f) else JarvisBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            JarvisPanelDark.copy(alpha = 0.95f),
                            JarvisPanelSecondary.copy(alpha = 0.98f)
                        )
                    )
                )
        ) {
            content()
        }
    }
}

@Composable
fun OnlineStatusBadge(
    isOnline: Boolean = true,
    text: String = if (isOnline) "ONLINE" else "OFFLINE"
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(JarvisPanelSecondary)
            .border(1.dp, JarvisBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (isOnline) JarvisGreen else Color.Gray)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = if (isOnline) JarvisGreen else Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}
