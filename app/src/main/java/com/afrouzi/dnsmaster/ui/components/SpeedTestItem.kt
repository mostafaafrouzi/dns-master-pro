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
import androidx.compose.ui.text.font.FontFamily
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
    val borderColor = if (result.isFastest) AppleOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val cardBackground = if (result.isFastest) {
        AppleOrange.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        modifier = modifier
            .fillMaxWidth()
            .border(if (result.isFastest) 1.5.dp else 0.5.dp, borderColor, RoundedCornerShape(14.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 13.dp)
                .fillMaxWidth()
        ) {
            // Left: Title, IPs, and Fastest Badge
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (result.isFastest) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Fastest",
                            tint = AppleOrange,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Text(
                        text = result.dnsItem.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "${result.dnsItem.primaryIp}${if (result.dnsItem.secondaryIp.isNotBlank()) " • ${result.dnsItem.secondaryIp}" else ""}",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Right: Latency & Action
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (result.status) {
                    SpeedTestStatus.TESTING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = AppleBlue
                        )
                    }
                    SpeedTestStatus.SUCCESS -> {
                        val ping = result.pingMs ?: 0L
                        val badgeColor = when {
                            ping < 100 -> AppleGreen
                            ping < 250 -> AppleBlue
                            else -> AppleOrange
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = badgeColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "${ping}ms",
                                    color = badgeColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            if (result.packetLoss != null || result.jitterMs != null) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (result.packetLoss != null) {
                                        val lossColor = if (result.packetLoss == 0) AppleGreen else AppleRed
                                        Text(
                                            text = "${result.packetLoss}% ${if (isPersian) "اتلاف" else "loss"}",
                                            color = lossColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    if (result.jitterMs != null) {
                                        Text(
                                            text = "±${result.jitterMs}ms",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onApply,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (result.isFastest) AppleOrange else AppleBlue,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = if (isPersian) "اتصال" else "Use",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    SpeedTestStatus.TIMEOUT,
                    SpeedTestStatus.ERROR -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AppleRed.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = if (result.status == SpeedTestStatus.TIMEOUT) {
                                    if (isPersian) "مهلت تمام" else "Timeout"
                                } else {
                                    if (isPersian) "خطا" else "Error"
                                },
                                color = AppleRed,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    SpeedTestStatus.IDLE -> {
                        Text(
                            text = "--",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
