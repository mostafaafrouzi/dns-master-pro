package com.afrouzi.dnsmaster.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
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
import com.afrouzi.dnsmaster.model.DnsCategory
import com.afrouzi.dnsmaster.model.DnsItem
import com.afrouzi.dnsmaster.theme.*

@Composable
fun DnsCard(
    item: DnsItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isPersian: Boolean = false
) {
    val borderColor = if (isSelected) NeonCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val cardBackground = if (isSelected) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val categoryEnum = try {
        DnsCategory.valueOf(item.category)
    } catch (e: Exception) {
        DnsCategory.FAST
    }

    val categoryColor = when (categoryEnum) {
        DnsCategory.FAST -> NeonCyan
        DnsCategory.ANTI_SANCTION -> NeonRose
        DnsCategory.GAMING -> NeonEmerald
        DnsCategory.PRIVACY -> ElectricPurple
        DnsCategory.FAMILY -> NeonAmber
        DnsCategory.CUSTOM -> ElectricIndigo
        DnsCategory.ALL -> NeonCyan
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        modifier = modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onSelect() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Name, Category Chip, Favorite Star
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(categoryColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isPersian) categoryEnum.titleFa else categoryEnum.titleEn,
                            color = categoryColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Favorite Button
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (item.isFavorite) NeonAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (item.isCustom && onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = NeonRose,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // IPs Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "IP 1: ${item.primaryIp}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                if (item.secondaryIp.isNotBlank()) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "IP 2: ${item.secondaryIp}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            // Description
            val desc = if (isPersian && item.descriptionFa.isNotBlank()) item.descriptionFa else item.descriptionEn
            if (desc.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    lineHeight = 16.sp
                )
            }

            // Ping Badge if tested
            if (item.pingMs != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val pingColor = when {
                    item.pingMs < 40 -> PingGreat
                    item.pingMs < 80 -> PingGood
                    item.pingMs < 150 -> PingFair
                    else -> PingPoor
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(pingColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = NetworkUtils.formatPing(item.pingMs),
                        color = pingColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
