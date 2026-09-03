package com.afrouzi.dnsmaster.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afrouzi.dnsmaster.core.utils.NetworkUtils
import com.afrouzi.dnsmaster.data.repository.DnsRepository
import com.afrouzi.dnsmaster.model.DnsCategory
import com.afrouzi.dnsmaster.model.DnsItem
import com.afrouzi.dnsmaster.theme.NeonCyan
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDnsScreen(
    repository: DnsRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    isPersian: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var primaryIp by remember { mutableStateOf("") }
    var secondaryIp by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(DnsCategory.FAST) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var primaryIpError by remember { mutableStateOf<String?>(null) }
    var secondaryIpError by remember { mutableStateOf<String?>(null) }

    val validateAndSave = {
        var isValid = true

        if (name.trim().isBlank()) {
            nameError = if (isPersian) "لطفاً نام سرور را وارد کنید" else "Please enter a server title"
            isValid = false
        } else {
            nameError = null
        }

        if (!NetworkUtils.isValidIp(primaryIp.trim())) {
            primaryIpError = if (isPersian) "فرمت آی‌پی نامعتبر است (IPv4 یا IPv6)" else "Invalid IP format (IPv4 or IPv6)"
            isValid = false
        } else {
            primaryIpError = null
        }

        if (secondaryIp.trim().isNotBlank() && !NetworkUtils.isValidIp(secondaryIp.trim())) {
            secondaryIpError = if (isPersian) "فرمت آی‌پی ثانویه نامعتبر است" else "Invalid secondary IP format"
            isValid = false
        } else {
            secondaryIpError = null
        }

        if (isValid) {
            val customItem = DnsItem(
                id = "custom_" + UUID.randomUUID().toString().take(8),
                name = name.trim(),
                primaryIp = primaryIp.trim(),
                secondaryIp = secondaryIp.trim(),
                category = selectedCategory.name,
                descriptionEn = description.trim(),
                descriptionFa = description.trim(),
                isCustom = true,
                isFavorite = true
            )

            coroutineScope.launch {
                repository.addCustomDns(customItem)
                repository.setSelectedDnsId(customItem.id)
                onNavigateBack()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Back Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isPersian) "تعریف DNS سفارشی" else "Add Custom DNS",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Card Container
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError != null) nameError = null
                    },
                    label = { Text(if (isPersian) "عنوان یا نام سرور" else "Server Name / Title") },
                    placeholder = { Text(if (isPersian) "مثال: کلودفلر شخصی" else "e.g. My Private DNS") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Primary IP Field
                OutlinedTextField(
                    value = primaryIp,
                    onValueChange = {
                        primaryIp = it
                        if (primaryIpError != null) primaryIpError = null
                    },
                    label = { Text(if (isPersian) "آدرس آی‌پی اصلی (IPv4 یا IPv6)" else "Primary DNS IP (IPv4 or IPv6)") },
                    placeholder = { Text("1.1.1.1") },
                    isError = primaryIpError != null,
                    supportingText = primaryIpError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Secondary IP Field
                OutlinedTextField(
                    value = secondaryIp,
                    onValueChange = {
                        secondaryIp = it
                        if (secondaryIpError != null) secondaryIpError = null
                    },
                    label = { Text(if (isPersian) "آدرس آی‌پی کمکی (اختیاری)" else "Secondary DNS IP (Optional)") },
                    placeholder = { Text("1.0.0.1") },
                    isError = secondaryIpError != null,
                    supportingText = secondaryIpError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selector
                Text(
                    text = if (isPersian) "دسته‌بندی" else "Category",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = if (isPersian) selectedCategory.titleFa else selectedCategory.titleEn,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        DnsCategory.values().filter { it != DnsCategory.ALL }.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(if (isPersian) cat.titleFa else cat.titleEn) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Optional Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (isPersian) "توضیحات (اختیاری)" else "Description (Optional)") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button
                Button(
                    onClick = validateAndSave,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPersian) "ذخیره و انتخاب سرور" else "Save & Select Server",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
