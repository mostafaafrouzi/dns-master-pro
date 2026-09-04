package com.afrouzi.dnsmaster.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afrouzi.dnsmaster.model.VpnConnectionState
import com.afrouzi.dnsmaster.theme.*

@Composable
fun ConnectionOrb(
    connectionState: VpnConnectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPersian: Boolean = false
) {
    val isConnected = connectionState == VpnConnectionState.CONNECTED
    val isConnecting = connectionState == VpnConnectionState.CONNECTING || connectionState == VpnConnectionState.DISCONNECTING

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth press spring
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pressScale"
    )

    // Gentle breathing pulse when connected
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isConnected) 1.04f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val buttonColor by animateColorAsState(
        targetValue = when (connectionState) {
            VpnConnectionState.CONNECTED -> AppleGreen
            VpnConnectionState.CONNECTING, VpnConnectionState.DISCONNECTING -> AppleBlue
            VpnConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(400),
        label = "buttonColor"
    )

    val iconColor by animateColorAsState(
        targetValue = when (connectionState) {
            VpnConnectionState.CONNECTED -> Color.White
            VpnConnectionState.CONNECTING, VpnConnectionState.DISCONNECTING -> Color.White
            VpnConnectionState.DISCONNECTED -> AppleGray
        },
        animationSpec = tween(300),
        label = "iconColor"
    )

    val statusDotColor by animateColorAsState(
        targetValue = when (connectionState) {
            VpnConnectionState.CONNECTED -> AppleGreen
            VpnConnectionState.CONNECTING, VpnConnectionState.DISCONNECTING -> AppleBlue
            VpnConnectionState.DISCONNECTED -> AppleGray
        },
        label = "dotColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(190.dp)
                .scale(pressScale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
        ) {
            // Translucent gentle aura when active
            if (isConnected || isConnecting) {
                Box(
                    modifier = Modifier
                        .size(186.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(buttonColor.copy(alpha = 0.15f))
                )
            }

            // Primary Apple-style Circular Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(156.dp)
                    .shadow(
                        elevation = if (isConnected) 12.dp else 4.dp,
                        shape = CircleShape,
                        spotColor = if (isConnected) AppleGreen.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.1f)
                    )
                    .clip(CircleShape)
                    .background(buttonColor)
                    .border(
                        width = if (isConnected || isConnecting) 0.dp else 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        shape = CircleShape
                    )
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(52.dp),
                        color = Color.White,
                        strokeWidth = 3.5.dp
                    )
                } else {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.Shield else Icons.Default.PowerSettingsNew,
                        contentDescription = "Power",
                        tint = iconColor,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // iOS Status Pill Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (isConnected) AppleGreen.copy(alpha = 0.12f)
                    else if (isConnecting) AppleBlue.copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(horizontal = 16.dp, vertical = 7.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusDotColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (connectionState) {
                        VpnConnectionState.CONNECTED -> if (isPersian) "متصل و محافظت‌شده" else "Connected & Protected"
                        VpnConnectionState.CONNECTING -> if (isPersian) "در حال اتصال..." else "Connecting..."
                        VpnConnectionState.DISCONNECTING -> if (isPersian) "در حال قطع ارتباط..." else "Disconnecting..."
                        VpnConnectionState.DISCONNECTED -> if (isPersian) "جهت اتصال لمس کنید" else "Tap to Connect"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when (connectionState) {
                        VpnConnectionState.CONNECTED -> AppleGreen
                        VpnConnectionState.CONNECTING, VpnConnectionState.DISCONNECTING -> AppleBlue
                        VpnConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}
