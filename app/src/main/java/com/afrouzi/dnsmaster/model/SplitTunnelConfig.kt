package com.afrouzi.dnsmaster.model

import android.graphics.drawable.Drawable

enum class SplitTunnelMode {
    ALL_APPS,          // All apps route DNS through DNS Master (default)
    BYPASS_SELECTED,   // Selected apps bypass DNS Master (e.g. banking apps)
    ONLY_SELECTED      // Only selected apps use DNS Master (e.g. games, browsers)
}

data class AppInfoItem(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean = false,
    val icon: Drawable? = null,
    val isSelected: Boolean = false
)
