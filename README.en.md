# DNS Master Pro | Android DNS Changer

[فارسی](README.md) · **English**

A high-performance, minimalist Android DNS changer and live multi-server benchmark app, crafted with **Apple iOS Human Interface Guidelines (HIG)** aesthetic, pre-configured anti-sanction resolvers, dual-stack IPv4/IPv6 support, and zero ads.

<div dir="ltr">

[![Latest Release](https://img.shields.io/github/v/release/mostafaafrouzi/android-dns-changer?style=flat-square&color=007AFF)](https://github.com/mostafaafrouzi/android-dns-changer/releases/latest)
[![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024%2B)-3DDC84?style=flat-square&logo=android)](https://android.com)
[![Design](https://img.shields.io/badge/Design-Apple%20iOS%20HIG-007AFF?style=flat-square&logo=apple)](https://developer.apple.com/design/human-interface-guidelines/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)
[![Ad--Free](https://img.shields.io/badge/Ads-100%25%20Free-success?style=flat-square)](#privacy--security)

</div>

---

## Why DNS Master Pro?

Most DNS changing applications on Google Play are cluttered with **intrusive full-screen video ads**, heavy battery consumption, and outdated user interfaces.

**DNS Master Pro** has been completely reimagined around Apple iOS Human Interface Guidelines (HIG) — featuring Inset Grouped cards, segmented controls, spring physics action buttons, and official Apple System color tokens. Its high-efficiency local TUN forwarder operates strictly on port 53 (DNS) with zero packet loss. **Your downloads, browsing traffic, and personal data never touch third-party proxy servers, guaranteeing full internet bandwidth and zero latency impact.**

---

## Key Features

### 1. Apple iOS HIG Minimalist Design System
- **Inset Grouped Layout:** Clean, structured cards with iOS-standard paddings and dividers.
- **Native Segmented Controls:** Smooth switching between English / Persian and Light / Dark themes.
- **Apple System Color Palette:** Featuring official `#007AFF` (Apple Blue), `#34C759` (Apple Green), `#FF9500` (Apple Orange), pure OLED black (`#000000`), and Apple's signature `#F2F2F7` grouped background.
- **Spring Physics Connection Orb:** Bouncy haptic feel (`MediumBouncy` damping) with gentle radiant aura in connected state.

### 2. High-Performance, Zero-Packet-Loss DNS Engine
- Clean TUN interface loopback isolation (`192.0.2.1/32` and `192.0.2.53/32`) ensuring standard web traffic remains 100% untouched.
- Direct stream buffer pipeline avoiding GC drops and heap buffer underflows.

### 3. Pre-configured Global & Anti-Sanction Resolvers
- **Anti-Sanction Resolvers:** Shecan, 403 Online, Electro, Begzar, and Radar Game for accessing restricted developer tools, cloud platforms, and gaming networks.
- **Top Global Providers:** Cloudflare (1.1.1.1), Google Public DNS (8.8.8.8), Quad9 Security, Cisco OpenDNS, AdGuard, Mullvad, and CleanBrowsing.
- **Dual-Stack IPv4 & IPv6:** Full primary and secondary IPv4 & IPv6 configuration.

### 4. Multi-Server Ping Benchmark
- Concurrently pings multiple resolvers using real UDP DNS queries to calculate precise round-trip latency (ms).
- Automatically highlights the fastest server with a spotlight banner and 1-tap **"Connect"** button.
- Categorized filtering: **All**, **Anti-Sanction**, **Gaming**, **Fast**, and **Security**.

### 5. Quick Settings Tile
- Toggle DNS protection directly from Android's Quick Settings notification panel.

### 6. Live Network Diagnostics
- Real-time detection of active network interface (Wi-Fi / Cellular).
- Online connectivity check, active protocol badges (IPv4 / IPv6), and local device IP.

### 7. Session Disconnection Summary
- Detailed modal summary showing connection duration and server used upon disconnecting.

### 8. Custom DNS Management
- Easily register custom DNS servers with category classification and IPv4/IPv6 validation.

---

## Screenshots

<div align="center">

| Home (Dark - Connected) | Home (Light - Connected) | Server Directory |
| :---: | :---: | :---: |
| <img src="docs/screenshots/01_home_connected.png" width="260"/> | <img src="docs/screenshots/02_home_light.png" width="260"/> | <img src="docs/screenshots/03_server_list.png" width="260"/> |

| Live Ping Benchmark | Custom DNS Manager | Session Summary Modal |
| :---: | :---: | :---: |
| <img src="docs/screenshots/04_speedtest_benchmark.png" width="260"/> | <img src="docs/screenshots/05_custom_dns.png" width="260"/> | <img src="docs/screenshots/06_session_summary.png" width="260"/> |

| Settings (Dark Mode) | Quick Action Notification | Settings (Light Mode) |
| :---: | :---: | :---: |
| <img src="docs/screenshots/07_settings_dark.png" width="260"/> | <img src="docs/screenshots/08_notification.png" width="260"/> | <img src="docs/screenshots/09_settings_light.png" width="260"/> |

</div>

---

## Privacy & Security

- **100% Ad-Free:** Zero advertisement SDKs, zero popups.
- **Zero Telemetry:** No tracking, telemetry, or user analytics.
- **DNS-Only Routing:** Only domain lookup requests are routed through the designated DNS resolver.

### Permissions Used

| Permission | Purpose |
| :--- | :--- |
| `BIND_VPN_SERVICE` | Required to create the local tunnel for redirecting DNS queries |
| `FOREGROUND_SERVICE` | Keeps the DNS service alive reliably in the background |
| `POST_NOTIFICATIONS` | Displays connection status notification with disconnect button on Android 13+ |
| `RECEIVE_BOOT_COMPLETED` | Allows optional automatic connection after device reboot |

---

## For Developers

### Technical Architecture
- **Language:** Kotlin 2.0
- **UI Framework:** Jetpack Compose + Apple iOS Human Interface Guidelines
- **Architecture:** Clean Architecture + MVVM + StateFlow + Coroutines
- **Networking:** Android `VpnService` + direct UDP socket ping benchmark
- **Preferences:** Jetpack DataStore Preferences

<div dir="ltr">

| Specification | Value |
| :--- | :--- |
| Application ID | `com.afrouzi.dnsmaster` |
| minSdk / targetSdk / compileSdk | 24 / 36 / 36 |
| Kotlin / Compose Compiler | 2.0.21 |
| Version | 1.1.0 |

</div>

### Local Build & Installation

```bash
# Clone the repository
git clone https://github.com/mostafaafrouzi/android-dns-changer.git
cd android-dns-changer

# Build debug APK
./gradlew assembleDebug

# Install on connected device or emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Download Latest Release

- **GitHub Releases:** [Download APK from GitHub Releases](https://github.com/mostafaafrouzi/android-dns-changer/releases/latest)

---

## License

This project is licensed under the [MIT License](LICENSE).
