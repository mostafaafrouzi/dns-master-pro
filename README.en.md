# DNS Master Pro | Android DNS Changer

[فارسی](README.md) · **English**

A high-performance, minimalist Android DNS changer and live multi-server benchmark app, crafted with **Apple iOS Human Interface Guidelines (HIG)** aesthetic, native **IRANSansX Eco** Persian typography, **RFC 1035 UDP NSLookup** resolver engine, v2rayNG-style sticky notification with live chronometer and instant disconnect, smart Quick Settings Tile, comprehensive anti-sanction and global DNS provider list, adaptive system navigation support, dual-stack IPv4/IPv6 support, and zero ads.

<div dir="ltr">

[![Latest Release](https://img.shields.io/github/v/release/mostafaafrouzi/dns-master-pro?style=flat-square&color=007AFF)](https://github.com/mostafaafrouzi/dns-master-pro/releases/latest)
[![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024%2B)-3DDC84?style=flat-square&logo=android)](https://android.com)
[![Design](https://img.shields.io/badge/Design-Apple%20iOS%20HIG-007AFF?style=flat-square&logo=apple)](https://developer.apple.com/design/human-interface-guidelines/)
[![Font](https://img.shields.io/badge/Font-IRANSansX-007AFF?style=flat-square)](https://fontiran.com)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)
[![Ad--Free](https://img.shields.io/badge/Ads-100%25%20Free-success?style=flat-square)](#privacy--security)

</div>

---

## Why DNS Master Pro?

Most DNS changing applications on Google Play are cluttered with **intrusive full-screen video ads**, heavy battery consumption, and outdated user interfaces.

**DNS Master Pro** has been completely reimagined around Apple iOS Human Interface Guidelines (HIG) — featuring Inset Grouped cards, segmented controls, spring physics action buttons, and official Apple System color tokens. Its high-efficiency local TUN forwarder operates strictly on port 53 (DNS) with zero packet loss. **Your downloads, browsing traffic, and personal data never touch third-party proxy servers, guaranteeing full internet bandwidth and near-zero battery usage.**

---

## Key Features

### 1. v2rayNG-Style Live Notification with Chronometer
- Real-time connection status display featuring a **live connection chronometer**.
- Clean presentation of active server name and configured DNS IP addresses.
- Instant, persistent **"Disconnect"** action button operating reliably across all Android versions without launching the app.

### 2. Smart Quick Settings Tile
- Dedicated Quick Settings Tile in Android's notification shade for one-tap DNS toggle.
- Real-time tile state synchronization (Active / Inactive).
- Direct app entry on Long Press gesture.

### 3. Authentic Persian Typography (IRANSansX Eco)
- Dedicated integration of the **IRANSansX Eco** font family for all Persian content.
- Pixel-perfect vertical baseline alignment and `includeFontPadding = false` for clean, professional Persian text rendering.

### 4. Real RFC 1035 UDP NSLookup Engine
- Beyond simple ICMP/UDP ping: sends authentic raw RFC 1035 DNS queries directly to port 53.
- Extracts and displays resolved IPv4 addresses, response status (NOERROR / SERVFAIL / TIMEOUT), TTL (Time-To-Live), and millisecond latency.
- **Anti-Sanction Verification:** Automatically verifies whether DNS resolvers successfully unblock restricted developer services like `docker.com` and `developer.android.com`, displaying a green verified badge.
- Interactive live inspector on the Home Screen with horizontally scrollable suggestion chips, plus a full multi-server benchmark mode on the Speed Test Screen.

### 5. Comprehensive Anti-Sanction & Global Resolvers
- **Anti-Sanction Resolvers:** Shecan, 403 Online, Electro, Begzar, Radar Game, and more for accessing restricted developer tools, cloud platforms, and gaming networks.
- **Top Global Providers:** Cloudflare (1.1.1.1), Google Public DNS (8.8.8.8), Quad9 Security, Cisco OpenDNS, AdGuard, Mullvad, Control D, and CleanBrowsing.

### 6. Battery Optimization Exemption
- Standard, non-intrusive Android battery optimization exemption prompt ensuring uninterrupted background operation without OS kills.

### 7. Apple iOS HIG Minimalist Design System
- **Inset Grouped Layout:** Clean, structured cards with iOS-standard paddings and hairline dividers.
- **Segmented Controls:** Smooth switching between modes and settings with fluid iOS animations.
- **Apple System Colors:** `#007AFF` (Apple Blue), `#34C759` (Apple Green), `#FF9500` (Apple Orange), OLED pure black (`#000000`), and Apple's signature `#F2F2F7` grouped background.
- Seamless compatibility with both 3-Button and Gesture navigation.

### 8. System Default Language Detection
- Automatic detection of device language (Persian / English) on first launch, with manual language toggle available in Settings.

---

## Screenshots

<div align="center">

| Home Screen (Dark - Connected) | Live NSLookup Inspector | Real NSLookup Benchmark |
| :---: | :---: | :---: |
| <img src="docs/screenshots/01_home_connected.png" width="260"/> | <img src="docs/screenshots/04_nslookup_home.png" width="260"/> | <img src="docs/screenshots/05_nslookup_benchmark.png" width="260"/> |

| Developer & About Section | 3-Button Navigation Mode | Full Server List |
| :---: | :---: | :---: |
| <img src="docs/screenshots/07_developer_about.png" width="260"/> | <img src="docs/screenshots/10_threebutton_navigation.png" width="260"/> | <img src="docs/screenshots/03_server_list.png" width="260"/> |

</div>

---

## Privacy & Security

- **Zero Advertising:** Absolutely no ads, banners, or interstitials.
- **No Trackers or Analytics:** Your queries and data never leave your device.
- **DNS-Only Routing:** Standard web browsing, games, and downloads are untouched and travel directly through your provider.

---

## For Developers

<div dir="ltr">

| Property | Value |
| :--- | :--- |
| Application ID | `com.afrouzi.dnsmaster` |
| minSdk / targetSdk / compileSdk | 24 / 36 / 36 |
| Kotlin / Compose Compiler | 2.0.21 |
| Version | 1.0.0 |
| Version Code | 1 |

</div>

### Local Build & Install

```bash
# Clone repository
git clone https://github.com/mostafaafrouzi/dns-master-pro.git
cd dns-master-pro

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device or emulator
adb install -r app/build/outputs/apk/release/app-release.apk
```

---

## Download Latest Version

- **GitHub Releases:** [Download APK from GitHub Releases](https://github.com/mostafaafrouzi/dns-master-pro/releases/latest)

---

## About the Creator

Designed & Developed by **Mostafa Afrouzi**:
- Website: [afrouzi.ir/en](https://afrouzi.ir/en/?utm_source=dnsmaster&utm_medium=github_readme&utm_campaign=dnsmaster)
- GitHub: [github.com/mostafaafrouzi](https://github.com/mostafaafrouzi)
- LinkedIn: [linkedin.com/in/mostafaafrouzi](https://linkedin.com/in/mostafaafrouzi)
- Other apps on CafeBazaar: [cafebazaar.ir/developer/057657612999](https://cafebazaar.ir/developer/057657612999)
- Other apps on Myket: [myket.ir/developer/dev-102174](https://myket.ir/developer/dev-102174)

---

## License

This project is licensed under the [MIT License](LICENSE).
