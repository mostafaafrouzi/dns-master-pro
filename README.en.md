# DNS Master Pro | Android DNS Changer

[فارسی](README.md) · **English**

A modern, high-performance, and privacy-first Android application to change DNS servers and benchmark ping latency. Features a cyber-neon Material 3 interface, pre-configured anti-sanction servers, and zero ads.

<div dir="ltr">

[![Latest Release](https://img.shields.io/github/v/release/mostafaafrouzi/android-dns-changer?style=flat-square&color=00F0FF)](https://github.com/mostafaafrouzi/android-dns-changer/releases/latest)
[![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024%2B)-3DDC84?style=flat-square&logo=android)](https://android.com)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)
[![Ad--Free](https://img.shields.io/badge/Ads-100%25%20Free-success?style=flat-square)](#privacy--security)
[![Material 3](https://img.shields.io/badge/Design-Material%203%20Compose-purple?style=flat-square)](https://m3.material.io)

</div>

---

## Why DNS Master Pro?

Most DNS changing applications on Google Play are cluttered with **intrusive full-screen video ads**, outdated layouts from Android 5/6, and lack regional developer resolvers.

**DNS Master Pro** is built with modern Jetpack Compose Material 3, glowing cyber-neon accents, and instant responsiveness. It runs a lightweight local VPN tunnel strictly for port 53 (DNS) queries. **Your downloads, browsing sessions, and personal traffic are NEVER proxied through remote servers, ensuring 100% maximum download speeds and zero latency penalty.**

---

## Key Features

### 1. Pre-configured Global & Anti-Sanction Resolvers
- **Anti-Sanction Resolvers:** Shecan, 403 Online, Electro, Begzar, and Radar Game for accessing restricted developer services, libraries, and gaming servers.
- **Top Global Providers:** Cloudflare (1.1.1.1), Google Public DNS, Quad9 Security, Cisco OpenDNS, AdGuard, Mullvad, and CleanBrowsing.
- **Dual-Stack IPv4 & IPv6 Support:** Official IPv6 addresses for modern dual-stack networks.

### 2. Multi-Server Ping Benchmark
- Real-time UDP socket ping tester measuring accurate round-trip time (RTT in ms).
- Automated **Golden Trophy Spotlight Card** highlighting the lowest-latency server for your network.
- Instant **"Apply ⚡"** button on every benchmark card for 1-tap activation.
- Category filter chips: **All**, **Anti-Sanction**, **Gaming**, **Fast**, and **Security**.
- Floating Action Button (FAB) to instantly rerun benchmarks without scrolling.

### 3. Live Network Diagnostics
- Real-time inspection of active transport (**Wi-Fi** or **Mobile Cellular**).
- Online internet connectivity verification badge.
- Active IP protocol detection (**IPv4** and **IPv6**).
- Local device IP address display.

### 4. 100% Local Tunnel Guarantee
- Complete transparency: DNS Master operates as a local resolver without proxying web traffic or collecting telemetry.

### 5. Quick Settings Tile
- Dedicated Android Quick Panel Tile allowing you to toggle DNS protection directly from the system notification shade.

### 6. Session Disconnection Summary
- Clean dialog displayed upon disconnect showing the exact duration of the session and the server disconnected.

### 7. Custom DNS Management
- Add custom DNS entries with title, category, and automatic IPv4/IPv6 address validation.

---

## Screenshots

<div align="center">

| Home Screen (Connected & Live Ping) | Network Diagnostics & Local Guarantee |
| :---: | :---: |
| <img src="docs/screenshots/01_home_connected.png" width="320"/> | <img src="docs/screenshots/02_network_diagnostics.png" width="320"/> |

| Speed Benchmark with Instant Connect | Category Filter (Gaming & Anti-Sanction) |
| :---: | :---: |
| <img src="docs/screenshots/03_speedtest_benchmark.png" width="320"/> | <img src="docs/screenshots/04_speedtest_filter.png" width="320"/> |

| Pre-configured Servers List | Custom DNS Manager |
| :---: | :---: |
| <img src="docs/screenshots/05_server_list.png" width="320"/> | <img src="docs/screenshots/06_custom_dns.png" width="320"/> |

| Session Disconnection Summary | System Notification Drawer |
| :---: | :---: |
| <img src="docs/screenshots/07_session_summary.png" width="320"/> | <img src="docs/screenshots/08_notification.png" width="320"/> |

</div>

---

## Privacy & Security

- **100% Ad-Free:** Zero advertisement SDKs, zero banners, and zero interstitial videos.
- **Zero Telemetry:** No analytics, tracking tokens, or user data collection.
- **Local Tunnel:** Uses Android's `VpnService` exclusively for routing UDP packets on port 53.

### Permissions Used

| Permission | Purpose |
| :--- | :--- |
| `BIND_VPN_SERVICE` | Required to create the local tunnel for redirecting DNS queries |
| `FOREGROUND_SERVICE` | Keeps the DNS service alive reliably in the background |
| `POST_NOTIFICATIONS` | Displays the connection status notification on Android 13+ |
| `RECEIVE_BOOT_COMPLETED` | Allows optional automatic re-connection after device restart |

---

## Developer Guide

### Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose with Material 3
- **Design Language:** Custom Dark Neon & Glassmorphism theme
- **Architecture:** Clean Architecture + MVVM + Kotlin Coroutines & StateFlow
- **DNS Tunneling:** Android `VpnService` handling raw IP packets via `FileInputStream` and `FileOutputStream`
- **Ping Engine:** Custom non-blocking UDP socket sending DNS query for `google.com`
- **Storage:** Jetpack DataStore Preferences & Kotlinx Serialization

<div dir="ltr">

| Spec | Value |
| :--- | :--- |
| Application ID | `com.afrouzi.dnsmaster` |
| minSdk / targetSdk / compileSdk | 24 / 36 / 36 |
| Kotlin / Compose Compiler | 2.0.21 |
| Version | 1.0.0 (Build 1) |

</div>

### Build & Run Locally

```bash
# Clone the repository
git clone https://github.com/mostafaafrouzi/android-dns-changer.git
cd android-dns-changer

# Build debug APK
./gradlew assembleDebug

# Install on connected device or emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Build Signed Release

To build a signed release locally, create a `keystore.properties` in the project root:

```properties
storeFile=keystore/dnsmaster-release.jks
storePassword=...
keyAlias=dnsmaster
keyPassword=...
```

Then run:

```bash
./gradlew assembleRelease bundleRelease
```

---

## Download Latest Release

- **GitHub Releases:** [Download Latest APK](https://github.com/mostafaafrouzi/android-dns-changer/releases/latest)

---

## License

This project is open-source software licensed under the [MIT License](LICENSE).
