package com.afrouzi.dnsmaster.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afrouzi.dnsmaster.model.VpnConnectionState
import com.afrouzi.dnsmaster.theme.NeonCyan
import com.afrouzi.dnsmaster.theme.NeonEmerald
import com.afrouzi.dnsmaster.theme.NeonRose

@Composable
fun ConnectionOrb(
    connectionState: VpnConnectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPersian: Boolean = false
) {
    val isConnected = connectionState == VpnConnectionState.CONNECTED
    val isConnecting = connectionState == VpnConnectionState.CONNECTING || connectionState == VpnConnectionState.DISCONNECTING

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnected) 1.08f else if (isConnecting) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val glowColor by animateColorAsState(
        targetValue = when (connectionState) {
            VpnConnectionState.CONNECTED -> NeonEmerald
            VpnConnectionState.CONNECTING, VpnConnectionState.DISCONNECTING -> NeonCyan
            VpnConnectionState.DISCONNECTED -> Color.Gray.copy(alpha = 0.3f)
        },
        label = "glowColor"
    )

    val orbGradient = when (connectionState) {
        VpnConnectionState.CONNECTED -> listOf(NeonEmerald, Color(0xFF047857))
        VpnConnectionState.CONNECTING, VpnConnectionState.DISCONNECTING -> listOf(NeonCyan, Color(0xFF0284C7))
        VpnConnectionState.DISCONNECTED -> listOf(Color(0xFF1F2937), Color(0xFF111827))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(200.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        ) {
            // Outer Pulsing Glow Ring
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(glowColor.copy(alpha = if (isConnected || isConnecting) 0.25f else 0.05f))
                    .border(2.dp, glowColor.copy(alpha = if (isConnected || isConnecting) 0.6f else 0.2f), CircleShape)
            )

            // Middle Ring
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(3.dp, glowColor.copy(alpha = 0.8f), CircleShape)
            )

            // Inner Interactive Core
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(130.dp)
                    .shadow(16.dp, CircleShape, spotColor = glowColor)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(orbGradient))
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(60.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.Shield else Icons.Default.PowerSettingsNew,
                        contentDescription = "Connection Button",
                        tint = if (isConnected || isConnecting) Color.White else Color.Gray,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val statusText = when (connectionState) {
            VpnConnectionState.CONNECTED -> if (isPersian) "متصل و محافظت‌شده" else "CONNECTED & PROTECTED"
            VpnConnectionState.CONNECTING -> if (isPersian) "در حال اتصال به سرور..." else "CONNECTING..."
            VpnConnectionState.DISCONNECTING -> if (isPersian) "در حال قطع اتصال..." else "DISCONNECTING..."
            VpnConnectionState.DISCONNECTED -> if (isPersian) "جهت اتصال لمس کنید" else "TAP TO CONNECT"
        }

        Text(
            text = statusText,
            color = if (isConnected) NeonEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
    }
}
