package com.afrouzi.dnsmaster.model

enum class DnsCategory(val titleEn: String, val titleFa: String) {
    ALL("All", "همه"),
    FAST("Fast & General", "سریع و عمومی"),
    ANTI_SANCTION("Anti-Sanction & Dev", "تحریم‌شکن و برنامه‌نویسان"),
    GAMING("Gaming (Low Ping)", "گیمینگ و پینگ پایین"),
    PRIVACY("Privacy & AdBlock", "حریم خصوصی و ضدردیابی"),
    FAMILY("Family & Safe", "امنیت خانواده و کودکان"),
    CUSTOM("Custom DNS", "سفارشی")
}
