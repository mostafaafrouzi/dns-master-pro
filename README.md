# دی‌ان‌اس مستر پرو | DNS Master Pro

**فارسی** · [English](README.en.md)

اپلیکیشن اندروید فوق‌حرفه‌ای، سریع و مینیمال برای تغییر DNS و بنچمارک سرعت سرورها، با تایپوگرافی اختصاصی **ایران‌سنس ایکس (IRANSansX Eco)**، موتور تست واقعی رزولوشن **NSLookup RFC 1035**، اعلان زنده سبک v2rayNG همراه با کرونومتر زمان اتصال، کاشی تنظیمات سریع هوشمند (Quick Settings Tile)، معماری طراحی برگرفته از **Apple iOS Human Interface Guidelines (HIG)**، پشتیبانی پیش‌فرض از غنی‌ترین لیست سرورهای تحریم‌شکن ایرانی و معتبرترین DNSهای جهان، کاملاً متن‌باز و بدون تبلیغات.

<div dir="ltr">

[![Latest Release](https://img.shields.io/github/v/release/mostafaafrouzi/dns-master-pro?style=flat-square&color=007AFF)](https://github.com/mostafaafrouzi/dns-master-pro/releases/latest)
[![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024%2B)-3DDC84?style=flat-square&logo=android)](https://android.com)
[![Design](https://img.shields.io/badge/Design-Apple%20iOS%20HIG-007AFF?style=flat-square&logo=apple)](https://developer.apple.com/design/human-interface-guidelines/)
[![Font](https://img.shields.io/badge/Font-IRANSansX-007AFF?style=flat-square)](https://fontiran.com)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)
[![Ad--Free](https://img.shields.io/badge/Ads-100%25%20Free-success?style=flat-square)](#حریم-خصوصی-و-امنیت)

</div>

---

## چرا دی‌ان‌اس مستر پرو؟

بیشتر برنامه‌های تغییر DNS در مارکت‌های اندروید آکنده از **تبلیغات اجباری ویدیویی**، قطعی مداوم، مصرف بی‌رویه باتری و رابط‌های کاربری شلوغ هستند.

**«دی‌ان‌اس مستر پرو»** با رویکرد مینیمال و استانداردهای طراحی اپل iOS توسعه داده شده است. موتور مسیریابی محلی آن، ترافیک اینترنت را دست‌نخورده باقی گذاشته و **تنها پکت‌های کم‌حجم پورت ۵۳ (DNS)** را به سرور دلخواه هدایت می‌کند؛ بنابراین کوچک‌ترین افت سرعتی در دانلود یا وبگردی ایجاد نمی‌شود و مصرف باتری در نزدیک‌ترین حالت به صفر قرار دارد.

---

## قابلیت‌های کلیدی

### ۱. اعلان زنده پیشرفته سبک v2rayNG
- نمایش وضعیت اتصال به صورت زنده به همراه **کرونومتر دقیق زمان اتصال (Live Chronometer)**.
- نمایش نام سرور متصل و آی‌پی‌های فعال به شکل بسیار خوانا و تمیز.
- دکمه فوری و پایدار **«قطع اتصال»** درون اعلان، با قابلیت قطع آنی از پنل اعلان‌ها بدون نیاز به باز کردن برنامه.

### ۲. کاشی تنظیمات سریع هوشمند (Smart Quick Settings Tile)
- دکمه اختصاصی در پنل اعلان‌های اندروید (Quick Settings) برای روشن و خاموش کردن سریع DNS.
- همگام‌سازی لحظه‌ای وضعیت فعال/غیرفعال بودن آیکون در پنل نوتیفیکیشن.
- پشتیبانی از لمس طولانی (Long Click) برای ورود مستقیم به داخل اپلیکیشن.

### ۳. تایپوگرافی اصیل با فونت ایران‌سنس ایکس (IRANSansX Eco)
- ادغام کامل فونت خانواده **ایران‌سنس ایکس اکو** برای متون فارسی با تنظیمات دقیق `includeFontPadding = false` و تراز عمودی خطوط.
- نمایش اعداد و حروف با خوانایی فوق‌العاده در تمام اندازه‌ها و وزن‌های نوشتاری.

### ۴. موتور تست واقعی رزولوشن دامنه (NSLookup RFC 1035)
- شبیه‌ساز واقعی دستور `nslookup` از طریق ارسال مستقیم پکت‌های خام UDP بر روی پورت ۵۳.
- استخراج و نمایش آی‌پی‌های پاسخ داده شده، وضعیت پاسخ سرور (RCODE مانند NOERROR / SERVFAIL / TIMEOUT)، تاخیر رفت‌وبرگشت (Latency) و مدت اعتبار رکورد (TTL).
- **تاییدیه عبور از تحریم (Anti-Sanction Verified):** اعتبارسنجی خودکار توانایی سرورها در دور زدن تحریم دامنه‌هایی چون `docker.com` و `developer.android.com` و نمایش نشان سبز اعتبارسنجی.
- ابزار بازرسی زنده در صفحه اصلی با چیپ‌های پیشنهادی اسکرول‌پذیر و بنچمارک مقایسه‌ای همه‌جانبه در صفحه تست سرعت با اسکرول کامل.

### ۵. لیست جامع و غنی سرورهای ضدتحریم و بین‌المللی
- **سرویس‌های ضد تحریم ایرانی:** شکن (Shecan)، ۴۰۳ آنلاین (403.online)، بگذر (Begzar)، الکترو (Electro)، رادار گیم (Radar Game) و سایر سرورهای معتبر داخلی.
- **سرویس‌های برتر جهانی:** Cloudflare (1.1.1.1)، Google Public DNS (8.8.8.8)، Quad9 Security، OpenDNS سیسکو، AdGuard، Mullvad، Control D و CleanBrowsing.

### ۶. پایداری ۱۰۰٪ و بهینه‌سازی هوشمند باتری
- دیالوگ هوشمند درخواست استثنا شدن از محدودیت باتری (Battery Optimization Exemption) طبق آخرین استانداردهای گوگل جهت جلوگیری از کشته شدن سرویس در پس‌زمینه.
- معماری ایزوله‌سازی روتینگ روی اینترفیس محلی TUN (`192.0.2.1/32` و `192.0.2.53/32`)؛ وب‌سایت‌ها و اپلیکیشن‌ها با ۰٪ پکت‌لاست و با نهایت سرعت بارگذاری می‌شوند.

### ۷. زبان طراحی مینیمال اپل (Apple iOS HIG)
- **کارت‌های چندلایه Inset Grouped:** چیدمان منظم، فاصله حاشیه‌ای استاندارد و تفکیک محتوا.
- **کنترل‌های تفکیک‌شده (Segmented Controls):** سوئیچ بین حالت‌های تست و تنظیمات با انیمیشن‌های نرم iOS.
- **پالت رنگی رسمی Apple System:** رنگ‌های `#007AFF` (Apple Blue)، `#34C759` (Apple Green)، `#FF9500` (Apple Orange) و مشکی مطلق OLED (`#000000`) در حالت دارک، همراه با حالت لایت یکدست (`#F2F2F7`).
- هماهنگی کامل با ناوبری ۳ دکمه‌ای و ژست‌های حرکتی اندروید بدون هرگونه تداخل یا فضای مرده.

### ۸. تشخیص خودکار زبان سیستم و چندزبانگی
- تشخیص هوشمند زبان پیش‌فرض سیستم‌عامل (فارسی / انگلیسی) در اجرای نخست و امکان جابجایی دستی در هر زمان.

---

## گالری تصاویر

<div align="center">

| صفحه اصلی (تیره - متصل) | بازرس زنده NSLookup | بنچمارک رزولوشن دامنه |
| :---: | :---: | :---: |
| <img src="docs/screenshots/01_home_connected.png" width="260"/> | <img src="docs/screenshots/04_nslookup_home.png" width="260"/> | <img src="docs/screenshots/05_nslookup_benchmark.png" width="260"/> |

| درباره توسعه‌دهنده و لینک‌ها | سازگاری با ناوبری ۳ دکمه‌ای | لیست کامل سرورها |
| :---: | :---: | :---: |
| <img src="docs/screenshots/07_developer_about.png" width="260"/> | <img src="docs/screenshots/10_threebutton_navigation.png" width="260"/> | <img src="docs/screenshots/03_server_list.png" width="260"/> |

</div>

---

## حریم خصوصی و امنیت

- **کاملاً بدون تبلیغات:** هیچ بنر یا تبلیغی درون برنامه گنجانده نشده است.
- **بدون ترکر و آنالیتیکس:** داده‌های هویتی، آدرس‌های وب و گزارشات شما ذخیره یا به سرور ثالثی ارسال نمی‌شوند.
- **مسیریابی فقط-DNS:** این اپ فیلترشکن نیست؛ دانلودها و ترافیک شخصی شما مستقیماً توسط اینترنت خودتان جابجا می‌شوند و از سرورهای واسط عبور نمی‌کنند.

---

## برای توسعه‌دهندگان

<div dir="ltr">

| مشخصه | مقدار |
| :--- | :--- |
| Application ID | `com.afrouzi.dnsmaster` |
| minSdk / targetSdk / compileSdk | 24 / 36 / 36 |
| Kotlin / Compose Compiler | 2.0.21 |
| Version | 1.0.0 |
| Version Code | 1 |

</div>

### بیلد و نصب محلی

```bash
# دریافت سورس کد
git clone https://github.com/mostafaafrouzi/dns-master-pro.git
cd dns-master-pro

# بیلد نسخه دیباگ
./gradlew assembleDebug

# بیلد نسخه نهایی (Release)
./gradlew assembleRelease

# نصب روی دستگاه یا امولاتور
adb install -r app/build/outputs/apk/release/app-release.apk
```

---

## دریافت آخرین نسخه

- **GitHub Releases:** [دانلود فایل APK از صفحه انتشار گیت‌هاب](https://github.com/mostafaafrouzi/dns-master-pro/releases/latest)

---

## درباره سازنده

طراحی و توسعه‌یافته توسط **مصطفی افروزی**:
- وب‌سایت: [afrouzi.ir](https://afrouzi.ir/?utm_source=dnsmaster&utm_medium=github_readme&utm_campaign=dnsmaster)
- گیت‌هاب: [github.com/mostafaafrouzi](https://github.com/mostafaafrouzi)
- لینکدین: [linkedin.com/in/mostafaafrouzi](https://linkedin.com/in/mostafaafrouzi)
- برنامه‌های من در کافه‌بازار: [cafebazaar.ir/developer/057657612999](https://cafebazaar.ir/developer/057657612999)
- برنامه‌های من در مایکت: [myket.ir/developer/dev-102174](https://myket.ir/developer/dev-102174)

---

## مجوز (License)

این پروژه تحت مجوز [MIT License](LICENSE) منتشر شده است.
