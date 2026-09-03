package com.afrouzi.dnsmaster.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afrouzi.dnsmaster.core.utils.NetworkUtils
import com.afrouzi.dnsmaster.model.SpeedTestResult
import com.afrouzi.dnsmaster.model.SpeedTestStatus
import com.afrouzi.dnsmaster.theme.*

@Composable
fun SpeedTestItem(
    result: SpeedTestResult,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
    isPersian: Boolean = false
) {
    val borderColor = if (result.isFastest) NeonAmber else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val cardBackground = if (result.isFastest) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        modifier = modifier
            .fillMaxWidth()
            .border(if (result.isFastest) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Left: Title, IPs, and Fastest Badge
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (result.isFastest) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Fastest",
                            tint = NeonAmber,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Text(
                        text = result.dnsItem.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${result.dnsItem.primaryIp}${if (result.dnsItem.secondaryIp.isNotBlank()) " | ${result.dnsItem.secondaryIp}" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Right: Latency & Action
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (result.status) {
                    SpeedTestStatus.TESTING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp,
                            color = NeonCyan
                        )
                    }
                    SpeedTestStatus.SUCCESS -> {
                        val ping = result.pingMs ?: 0L
                        val badgeColor = when {
                            ping < 40 -> PingGreat
                            ping < 80 -> PingGood
                            ping < 150 -> PingFair
                            else -> PingPoor
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeColor.copy(alpha = 0.15f))
                                .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = NetworkUtils.formatPing(result.pingMs),
                                color = badgeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onApply,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (result.isFastest) NeonAmber else MaterialTheme.colorScheme.primaryContainer,
                                contentColor = if (result.isFastest) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isPersian) "اتصال" else "Apply",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    SpeedTestStatus.TIMEOUT, SpeedTestStatus.ERROR -> {
                        Text(
                            text = if (isPersian) "تایم‌اوت" else "Timeout",
                            color = PingPoor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    SpeedTestStatus.IDLE -> {
                        Text(
                            text = "--",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
