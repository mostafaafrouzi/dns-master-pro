package com.afrouzi.dnsmaster.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import com.afrouzi.dnsmaster.theme.*
import com.afrouzi.dnsmaster.ui.components.IosGroupedCard
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
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // iOS Navigation Bar Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AppleBlue
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isPersian) "تعریف سرور جدید" else "New DNS Server",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            TextButton(
                onClick = validateAndSave,
                colors = ButtonDefaults.textButtonColors(contentColor = AppleBlue)
            ) {
                Text(
                    text = if (isPersian) "ذخیره" else "Save",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 1: Server Identity
        Text(
            text = (if (isPersian) "اطلاعات سرور" else "SERVER INFO").uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
        )

        IosGroupedCard(cornerRadius = 14.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError != null) nameError = null
                    },
                    label = { Text(if (isPersian) "نام یا عنوان سرور" else "Server Name") },
                    placeholder = { Text(if (isPersian) "مثال: کلودفلر اختصاصی" else "e.g. My Secure DNS") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it, color = AppleRed) } },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (isPersian) "توضیحات (اختیاری)" else "Description (Optional)") },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 2: IP Addresses
        Text(
            text = (if (isPersian) "آدرس‌های DNS" else "DNS ADDRESSES").uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
        )

        IosGroupedCard(cornerRadius = 14.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = primaryIp,
                    onValueChange = {
                        primaryIp = it
                        if (primaryIpError != null) primaryIpError = null
                    },
                    label = { Text(if (isPersian) "آی‌پی اصلی (اجباری)" else "Primary DNS (Required)") },
                    placeholder = { Text("1.1.1.1") },
                    isError = primaryIpError != null,
                    supportingText = primaryIpError?.let { { Text(it, color = AppleRed) } },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = secondaryIp,
                    onValueChange = {
                        secondaryIp = it
                        if (secondaryIpError != null) secondaryIpError = null
                    },
                    label = { Text(if (isPersian) "آی‌پی ثانویه (اختیاری)" else "Secondary DNS (Optional)") },
                    placeholder = { Text("1.0.0.1") },
                    isError = secondaryIpError != null,
                    supportingText = secondaryIpError?.let { { Text(it, color = AppleRed) } },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 3: Category
        Text(
            text = (if (isPersian) "دسته‌بندی" else "CATEGORY").uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
        )

        IosGroupedCard(cornerRadius = 14.dp) {
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = if (isPersian) selectedCategory.titleFa else selectedCategory.titleEn,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
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
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Primary Save Button
        Button(
            onClick = validateAndSave,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppleBlue, contentColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isPersian) "ذخیره و استفاده" else "Save & Apply",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
