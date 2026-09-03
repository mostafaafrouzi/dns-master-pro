package com.afrouzi.dnsmaster.data

import com.afrouzi.dnsmaster.model.DnsCategory
import com.afrouzi.dnsmaster.model.DnsItem

object DefaultDnsServers {
    val list: List<DnsItem> = listOf(
        // Fast & General
        DnsItem(
            id = "cloudflare",
            name = "Cloudflare (1.1.1.1)",
            primaryIp = "1.1.1.1",
            secondaryIp = "1.0.0.1",
            category = DnsCategory.FAST.name,
            descriptionEn = "Ultra-fast global DNS by Cloudflare with strict privacy.",
            descriptionFa = "سریع‌ترین دی‌ان‌اس عمومی جهان متعلق به کلودفلر با حفظ حریم خصوصی.",
            isFavorite = true,
            primaryIpv6 = "2606:4700:4700::1111",
            secondaryIpv6 = "2606:4700:4700::1001"
        ),
        DnsItem(
            id = "google",
            name = "Google Public DNS",
            primaryIp = "8.8.8.8",
            secondaryIp = "8.8.4.4",
            category = DnsCategory.FAST.name,
            descriptionEn = "Reliable, resilient, and widely distributed global DNS by Google.",
            descriptionFa = "دی‌ان‌اس پایدار و قدرتمند گوگل با پوشش سرورهای جهانی.",
            isFavorite = true,
            primaryIpv6 = "2001:4860:4860::8888",
            secondaryIpv6 = "2001:4860:4860::8844"
        ),
        DnsItem(
            id = "quad9",
            name = "Quad9 Security",
            primaryIp = "9.9.9.9",
            secondaryIp = "149.112.112.112",
            category = DnsCategory.FAST.name,
            descriptionEn = "Blocks malicious domains, botnets, and phishing automatically.",
            descriptionFa = "مسدودکننده بدافزارها، فیشینگ و سایت‌های مخرب توسط بنیاد Quad9.",
            isFavorite = false,
            primaryIpv6 = "2620:fe::fe",
            secondaryIpv6 = "2620:fe::9"
        ),
        DnsItem(
            id = "opendns",
            name = "OpenDNS (Cisco)",
            primaryIp = "208.67.222.222",
            secondaryIp = "208.67.220.220",
            category = DnsCategory.FAST.name,
            descriptionEn = "High performance with built-in security features from Cisco.",
            descriptionFa = "دی‌ان‌اس بسیار باکیفیت و ایمن متعلق به شرکت معتبر سیسکو.",
            isFavorite = false,
            primaryIpv6 = "2620:119:35::35",
            secondaryIpv6 = "2620:119:53::53"
        ),
        DnsItem(
            id = "controld",
            name = "Control D",
            primaryIp = "76.76.2.0",
            secondaryIp = "76.76.10.0",
            category = DnsCategory.FAST.name,
            descriptionEn = "Fast uncensored DNS without telemetry or logging.",
            descriptionFa = "سرویس مدرن و پرسرعت بدون ذخیره گزارش و لاگ کاربر.",
            isFavorite = false
        ),
        DnsItem(
            id = "level3",
            name = "Level3 (Lumen)",
            primaryIp = "209.244.0.3",
            secondaryIp = "209.244.0.4",
            category = DnsCategory.FAST.name,
            descriptionEn = "Enterprise backbone tier-1 provider DNS.",
            descriptionFa = "دی‌ان‌اس مستقیم از ستون فقرات شبکه جهانی اینترنت (Tier-1).",
            isFavorite = false
        ),
        DnsItem(
            id = "gcore",
            name = "G-Core DNS",
            primaryIp = "95.85.95.85",
            secondaryIp = "2.56.220.2",
            category = DnsCategory.FAST.name,
            descriptionEn = "Anycast infrastructure DNS with low global latency.",
            descriptionFa = "زیرساخت ابری Anycast با تأخیر بسیار پایین در خاورمیانه و جهان.",
            isFavorite = false
        ),
        DnsItem(
            id = "dnswatch",
            name = "DNS.WATCH",
            primaryIp = "84.200.69.80",
            secondaryIp = "84.200.70.40",
            category = DnsCategory.FAST.name,
            descriptionEn = "Uncensored, neutral, and privacy-friendly DNS hosted in Germany.",
            descriptionFa = "دی‌ان‌اس بدون سانسور، بی‌طرف و حامی حریم شخصی در آلمان.",
            isFavorite = false
        ),

        // Anti-Sanction & Developer (تحریم‌شکن‌های اختصاصی)
        DnsItem(
            id = "shecan",
            name = "شکن (Shecan)",
            primaryIp = "178.22.122.100",
            secondaryIp = "185.51.200.2",
            category = DnsCategory.ANTI_SANCTION.name,
            descriptionEn = "Bypasses international technological sanctions on developer services.",
            descriptionFa = "سامانه تحریم‌شکن ایرانی برای دسترسی به سایت‌های تحریم‌شده برای ایرانیان.",
            isFavorite = true
        ),
        DnsItem(
            id = "403online",
            name = "۴۰۳ آنلاین (403.online)",
            primaryIp = "10.202.10.202",
            secondaryIp = "10.202.10.102",
            category = DnsCategory.ANTI_SANCTION.name,
            descriptionEn = "Specialized anti-sanction service for developers, designers, and webmasters.",
            descriptionFa = "سرویس رفع تحریم‌های اعمال‌شده علیه برنامه‌نویسان، طراحان و مدیران سایت.",
            isFavorite = true
        ),
        DnsItem(
            id = "electro",
            name = "الکترو (Electro)",
            primaryIp = "78.157.42.101",
            secondaryIp = "78.157.42.100",
            category = DnsCategory.ANTI_SANCTION.name,
            descriptionEn = "Fast proxy DNS for bypassing sanctions, gaming, and downloading apps.",
            descriptionFa = "دی‌ان‌اس پرسرعت الکترو برای رفع تحریم‌ها، دانلود و دسترسی به سرویس‌ها.",
            isFavorite = true
        ),
        DnsItem(
            id = "begzar",
            name = "بگذر (Begzar)",
            primaryIp = "185.55.226.26",
            secondaryIp = "185.55.225.25",
            category = DnsCategory.ANTI_SANCTION.name,
            descriptionEn = "Popular anti-sanction DNS for accessing developer platforms and tools.",
            descriptionFa = "سامانه بگذر برای عبور از سدهای تحریمی ابزارها و پلتفرم‌های کاربردی.",
            isFavorite = false
        ),
        DnsItem(
            id = "radar",
            name = "رادار (Radar)",
            primaryIp = "10.202.10.10",
            secondaryIp = "10.202.10.11",
            category = DnsCategory.ANTI_SANCTION.name,
            descriptionEn = "Optimized routing for gaming and international sanction bypass.",
            descriptionFa = "بهینه‌سازی مسیرها و رفع تحریم‌های اعمال‌شده بر اینترنت ایران.",
            isFavorite = false
        ),

        // Gaming (پینگ پایین مخصوص بازی)
        DnsItem(
            id = "radar_game",
            name = "Radar Game (رادار گیم)",
            primaryIp = "10.202.10.10",
            secondaryIp = "10.202.10.11",
            category = DnsCategory.GAMING.name,
            descriptionEn = "Dedicated game server routes with reduced ping and packet loss.",
            descriptionFa = "مسیرهای اختصاصی بازی‌های آنلاین برای کاهش پینگ و حل پکت‌لاس.",
            isFavorite = true
        ),
        DnsItem(
            id = "electro_game",
            name = "Electro Gaming",
            primaryIp = "78.157.42.100",
            secondaryIp = "78.157.42.101",
            category = DnsCategory.GAMING.name,
            descriptionEn = "Low latency gaming DNS resolving global multiplayer matchmaking.",
            descriptionFa = "بهینه‌شده برای ورود به لابی‌ها و مچ‌میکینگ سریع بازی‌های آنلاین.",
            isFavorite = true
        ),
        DnsItem(
            id = "cloudflare_gaming",
            name = "Cloudflare Gaming",
            primaryIp = "1.1.1.1",
            secondaryIp = "1.0.0.1",
            category = DnsCategory.GAMING.name,
            descriptionEn = "Low jitter and fast query response ideal for real-time competitive gaming.",
            descriptionFa = "پاسخ‌دهی آنی و حداقل نوسان پینگ مناسب بازی‌های رقابتی.",
            isFavorite = false
        ),
        DnsItem(
            id = "google_gaming",
            name = "Google Gaming DNS",
            primaryIp = "8.8.8.8",
            secondaryIp = "8.8.4.4",
            category = DnsCategory.GAMING.name,
            descriptionEn = "Global edge caching for consistent game matchmaking and updates.",
            descriptionFa = "اتصال پایدار و کش قدرتمند برای پچ‌ها و بازی‌های آنلاین سراسری.",
            isFavorite = false
        ),

        // Privacy & Ad-Blocking
        DnsItem(
            id = "adguard_default",
            name = "AdGuard DNS (AdBlock)",
            primaryIp = "94.140.14.14",
            secondaryIp = "94.140.15.15",
            category = DnsCategory.PRIVACY.name,
            descriptionEn = "Blocks banners, popups, video ads, trackers, and telemetry.",
            descriptionFa = "مسدودسازی تبلیغات مزاحم وبسایت‌ها، بنرها و سیستم‌های ردیابی آنلاین.",
            isFavorite = true,
            primaryIpv6 = "2a10:50c0::ad1:ff",
            secondaryIpv6 = "2a10:50c0::ad2:ff"
        ),
        DnsItem(
            id = "nextdns",
            name = "NextDNS",
            primaryIp = "45.90.28.0",
            secondaryIp = "45.90.30.0",
            category = DnsCategory.PRIVACY.name,
            descriptionEn = "Modern security and privacy DNS with customizable firewall capabilities.",
            descriptionFa = "سرویس پیشرفته محافظت از حریم خصوصی با فایروال ضدجاسوسی.",
            isFavorite = false
        ),
        DnsItem(
            id = "mullvad",
            name = "Mullvad DNS",
            primaryIp = "194.242.2.2",
            secondaryIp = "193.19.108.2",
            category = DnsCategory.PRIVACY.name,
            descriptionEn = "Privacy-first zero-log DNS hosted in Sweden by Mullvad VPN.",
            descriptionFa = "دی‌ان‌اس ضدجاسوسی و بدون لاگ توسط شرکت خوش‌نام مول‌واد در سوئد.",
            isFavorite = false
        ),
        DnsItem(
            id = "alternate_dns",
            name = "Alternate DNS",
            primaryIp = "76.76.19.19",
            secondaryIp = "76.223.122.150",
            category = DnsCategory.PRIVACY.name,
            descriptionEn = "Blocks annoying ads before they reach your browser or apps.",
            descriptionFa = "مسدودکننده تبلیغات قبل از بارگذاری در برنامه‌ها و مرورگر.",
            isFavorite = false
        ),

        // Family & Safe Search
        DnsItem(
            id = "cleanbrowsing_family",
            name = "CleanBrowsing Family",
            primaryIp = "185.228.168.168",
            secondaryIp = "185.228.169.168",
            category = DnsCategory.FAMILY.name,
            descriptionEn = "Blocks adult content, enables Google SafeSearch, and protects children.",
            descriptionFa = "فیلتر محتوای نامناسب، فعال‌سازی جستجوی امن و مراقبت از کودکان.",
            isFavorite = true
        ),
        DnsItem(
            id = "adguard_family",
            name = "AdGuard Family",
            primaryIp = "94.140.14.15",
            secondaryIp = "94.140.15.16",
            category = DnsCategory.FAMILY.name,
            descriptionEn = "Ad-blocking combined with strict family-safe filters.",
            descriptionFa = "مسدودسازی تبلیغات به همراه حفاظت و فیلترینگ ایمن برای کل خانواده.",
            isFavorite = false
        ),
        DnsItem(
            id = "opendns_family",
            name = "OpenDNS FamilyShield",
            primaryIp = "208.67.222.123",
            secondaryIp = "208.67.220.123",
            category = DnsCategory.FAMILY.name,
            descriptionEn = "Pre-configured adult domain blocking from Cisco OpenDNS.",
            descriptionFa = "حفاظت خودکار از خانواده و مسدودسازی پیش‌فرض سایت‌های مخرب توسط سیسکو.",
            isFavorite = false
        )
    )
}
