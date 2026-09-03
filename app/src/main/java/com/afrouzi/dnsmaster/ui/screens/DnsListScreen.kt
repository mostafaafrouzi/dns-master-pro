package com.afrouzi.dnsmaster.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afrouzi.dnsmaster.data.DefaultDnsServers
import com.afrouzi.dnsmaster.data.repository.DnsRepository
import com.afrouzi.dnsmaster.model.DnsCategory
import com.afrouzi.dnsmaster.model.DnsItem
import com.afrouzi.dnsmaster.model.VpnConnectionState
import com.afrouzi.dnsmaster.service.DnsVpnService
import com.afrouzi.dnsmaster.theme.NeonCyan
import com.afrouzi.dnsmaster.ui.components.DnsCard
import kotlinx.coroutines.launch

@Composable
fun DnsListScreen(
    repository: DnsRepository,
    onNavigateToAddCustom: () -> Unit,
    modifier: Modifier = Modifier,
    isPersian: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val selectedDnsId by repository.selectedDnsIdFlow.collectAsState(initial = "cloudflare")
    val customList by repository.customDnsListFlow.collectAsState(initial = emptyList())
    val favorites by repository.favoritesFlow.collectAsState(initial = emptySet())
    val connectionState by DnsRepository.connectionState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(DnsCategory.ALL) }
    var itemToDelete by remember { mutableStateOf<DnsItem?>(null) }

    // Merge default and custom servers, sync favorite state
    val allServers = remember(customList, favorites) {
        val list = customList + DefaultDnsServers.list
        list.map { it.copy(isFavorite = favorites.contains(it.id)) }
    }

    val filteredServers = remember(allServers, searchQuery, selectedCategory) {
        allServers.filter { item ->
            val matchesCategory = when (selectedCategory) {
                DnsCategory.ALL -> true
                DnsCategory.CUSTOM -> item.isCustom
                else -> item.category == selectedCategory.name
            }
            val matchesSearch = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.primaryIp.contains(searchQuery, ignoreCase = true) ||
                    item.secondaryIp.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddCustom,
                containerColor = NeonCyan,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Custom DNS")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = if (isPersian) "جستجوی سرور یا آی‌پی..." else "Search DNS name or IP...",
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(DnsCategory.values()) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                text = if (isPersian) category.titleFa else category.titleEn,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                            selectedLabelColor = NeonCyan
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Server Count Header
            Text(
                text = "${filteredServers.size} ${if (isPersian) "سرور در دسترس" else "servers available"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // List of DNS items
            if (filteredServers.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = if (isPersian) "سروری مطابق با جستجو پیدا نشد" else "No matching servers found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredServers, key = { it.id }) { item ->
                        val isSelected = item.id == selectedDnsId
                        DnsCard(
                            item = item,
                            isSelected = isSelected,
                            onSelect = {
                                coroutineScope.launch {
                                    repository.setSelectedDnsId(item.id)
                                    if (connectionState == VpnConnectionState.CONNECTED) {
                                        val updateIntent = Intent(context, DnsVpnService::class.java).apply {
                                            action = DnsVpnService.ACTION_UPDATE
                                            putExtra(DnsVpnService.EXTRA_DNS_ID, item.id)
                                        }
                                        context.startService(updateIntent)
                                    }
                                }
                            },
                            onToggleFavorite = {
                                coroutineScope.launch {
                                    repository.toggleFavorite(item.id)
                                }
                            },
                            onDelete = if (item.isCustom) {
                                { itemToDelete = item }
                            } else null,
                            isPersian = isPersian
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (itemToDelete != null) {
        val target = itemToDelete!!
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Text(
                    text = if (isPersian) "حذف دی‌ان‌اس سفارشی" else "Delete Custom DNS",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isPersian)
                        "آیا از حذف سرور «${target.name}» اطمینان دارید؟"
                    else
                        "Are you sure you want to delete '${target.name}'?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            repository.deleteCustomDns(target.id)
                            itemToDelete = null
                        }
                    }
                ) {
                    Text(text = if (isPersian) "حذف" else "Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(text = if (isPersian) "انصراف" else "Cancel")
                }
            }
        )
    }
}
