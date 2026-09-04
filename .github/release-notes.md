## فارسی

### نسخهٔ ۱.۲.۱ (جدید)
**اسکرول روان تمام‌صفحه در بخش‌های تست سرعت و اصلاح نمایش و خوانایی کامل فیلد جستجو.**

**تغییرات و بهبودهای نسخه ۱.۲.۱:**
- **اسکرول یکپارچه تمام‌صفحه در تب‌های تست سرعت:**
  - تبدیل هر دو تب «تست رزولوشن (NSLookup)» و «بنچمارک پینگ (Ping)» به یک محفظه اسکرول یکپارچه (`LazyColumn`).
  - رفع مشکل شناور و ثابت ماندن کارت‌های بالای صفحه؛ اکنون با اسکرول به سمت پایین، کارت‌های تنظیمات و تراشه‌های دسته‌بندی به راحتی به بالا حرکت کرده و ۱۰۰٪ ارتفاع صفحه به فهرست سرورها اختصاص می‌یابد.
  - تعبیه ارگونومیک دکمه شناور تست سریع (FAB) در گوشه پایین صفحه متناسب با سیستم‌های ناوبری اندروید.
- **بهبود کامل فیلد جستجوی سرورها:**
  - بازطراحی نوار جستجو با `BasicTextField` و کانتینر iOS؛ حذف لایه‌های اضافی پدینگ داخلی که مانع دید کامل متن می‌شد.
  - نمایش کاملاً واضح، خوانا و بدون بریدگی حروف فارسی و انگلیسی در متن ورودی و پلیس‌هولدر.

---

### نسخهٔ ۱.۲.۰
- **فونت اختصاصی IRANSansX Eco:** پیاده‌سازی تایپوگرافی اصیل و حرفه‌ای فارسی با فونت ایران‌سنس ایکس اکو (شامل ارقام فارسی FaNum برای زمان و پینگ) در تمامی صفحات و المان‌های برنامه با کرنینگ و تراز خطی دقیق.
- **موتور تست واقعی رزولوشن دامنه (RFC 1035 UDP DNS Engine):**
  - شبیه‌ساز واقعی ابزار NSLookup ویندوز و دیگ لینوکس بر بستر پکت‌های خام UDP بدون اتکا به پینگ‌های ICMP ساده یا کش سیستم‌عامل.
  - سنجش میلی‌ثانیه‌ای زمان پاسخ واقعی، نمایش وضعیت RCODE (نظیر NOERROR)، خواندن زمان انقضای TTL پکت‌ها و استخراج آی‌پی‌های بازگشتی.
  - تست زنده دامنه در صفحه اصلی با تراشه‌های سریع (گوگل، داکر، شکن، ویکی‌پدیا).
  - حالت بنچمارک رزولوشن در صفحه تست سرعت با قابلیت تست همزمان تمام سرورها، نشان طلایی سریع‌ترین پاسخ‌دهنده و دکمه اتصال مستقیم ۱-کلیکه.
- **پشتیبانی کامل و استاندارد از انواع سیستم‌های ناوبری اندروید:**
  - تطبیق‌پذیری هوشمند با حالت ۳ دکمه کلاسیک (Back, Home, Recents) و حالت حرکتی (Gesture Navigation) با WindowInsets استاندارد، جلوگیری از تداخل و کشیدگی نوار ناوبری پایین.
- **اصلاح جهت فلش‌ها در زبان فارسی (RTL):** هدایت درست و استاندارد جهت فلش کارد سرور انتخابی و سلول‌های تنظیمی رو به لبه خارجی صفحه در هر دو زبان فارسی (`<`) و انگلیسی (`>`).
- **دکمه قطع اتصال فوری در اعلانات با عملکرد ۱۰۰٪ تضمین‌شده:** ارسال مستقیم اینتنت سرویس جهت قطع لحظه‌ای اتصال بدون هیچ‌گونه تاخیر یا عدم پاسخگویی در اندرویدهای جدید.
- **بخش کامل توسعه‌دهنده و درباره برنامه (مطابق با اکوسیستم MyCard):**
  - پروفایل مصطفی افروزی با بیوگرافی و تخصص در هر دو زبان فارسی و انگلیسی.
  - وب‌سایت رسمی شخصی با ردیاب‌های اختصاصی زبان (`utm_source=dnsmaster&utm_medium=app&utm_campaign=about_fa`).
  - لینک‌های معتبر به گیت‌هاب، لینکدین و پروفایل‌های توسعه‌دهنده در کافه‌بازار و مایکت.

---

### نسخهٔ ۱.۱.۰
**بازطراحی کامل بر پایه استانداردهای اپل iOS و بهبود پایداری هسته DNS.**

**تغییرات و بهینه‌سازی‌های نسخه ۱.۱.۰:**
- **طراحی به سبک Apple iOS HIG:** خروج کامل از طراحی‌های شلوغ و نئونی مصنوعی و بازطراحی اصیل، تمیز و مینیمال بر پایه دستورالعمل طراحی سیستم‌عامل iOS اپل:
  - کارت‌های استاندارد Inset Grouped با جداسازهای خط‌مویی (Hairline Dividers).
  - کنترل‌های تفکیک‌شده (Segmented Controls) برای زبان و تم ظاهری با انیمیشن‌های نرم.
  - پالت رنگی رسمی سیستم‌عامل اپل شامل Apple Blue (`#007AFF`)، Apple Green (`#34C759`)، مشکی مطلق OLED (`#000000`) و پس‌زمینه استاندارد لایت (`#F2F2F7`).
  - کلید اتصال دایره‌ای با فیزیک جهشی کشسانی (`MediumBouncy`) و هاله نور سبز متصل.
- **حل کامل مشکل لود نشدن سایت‌ها:** اصلاح و ارتقای کامل هسته `DnsPacketForwarder` با بافرهای استریم مستقیم و روتینگ ایزوله تونل محلی؛ حل مشکل افت پکت و تضمین باز شدن بی‌نقص تمام وب‌سایت‌ها و اپلیکیشن‌ها با ۰٪ پکت‌لاست.
- **بهبود کارت‌های لیست سرورها و بنچمارک:** فیلتر دسته‌بندی با برچسب‌های شفاف و نشانگرهای عددی تمیز پینگ میلی‌ثانیه‌ای.
- **طراحی مجدد فرم تعریف DNS سفارشی:** فرم گروه‌بندی‌شده سبک iOS با دکمه ذخیره در نوار بالا و اعتبارسنجی آنی.
- **پایداری بالاتر سرویس:** جلوگیری از بسته‌شدن ناخواسته فایل‌دسکریپتور VPN توسط Garbage Collector.

---

## English

### Version 1.2.1 (Latest)
**Unified Full-Height Scrollable Speed Test Layout & Unclipped Server Search Input.**

**What's New in v1.2.1:**
- **Unified Full-Screen Scrolling on Speed Test Tabs:**
  - Integrated both "Resolution Test (NSLookup)" and "Ping Benchmark" modes into a single top-to-bottom scrollable container (`LazyColumn`).
  - Configuration cards and filter pills now naturally scroll upward with the page, dedicating 100% of the screen height to the server list.
  - The Quick Benchmark Floating Action Button (FAB) comfortably docks in the bottom-end corner above system navigation.
- **Enhanced Server Search Input Display & Readability:**
  - Redesigned search bar using `BasicTextField` with custom iOS-styled container, eliminating cramped default paddings.
  - Text and placeholder are now fully visible, perfectly centered vertically, and crisp with zero character clipping in both Persian and English.

---

### Version 1.2.0
- **IRANSansX Eco Typography:** Integrated premium Persian typography with the official IranSansX Eco font family (including FaNum Persian digits for counters and latencies) across all screens and UI components with precise line height and baseline metrics.
- **RFC 1035 UDP DNS Lookup Engine & Benchmark:**
  - Custom raw UDP socket query builder & parser operating identically to Windows `nslookup` and Linux `dig`.
  - Accurately inspects response time in milliseconds, extracts resolved IP addresses, reads record TTL, and reports RCODE status (`NOERROR`, `NXDOMAIN`, etc.).
  - Interactive live domain inspector on HomeScreen with quick chips (`google.com`, `docker.com`, `shecan.ir`, `wikipedia.org`).
  - Concurrent multi-server resolution benchmark on SpeedTestScreen with fastest resolver trophy badge and 1-tap connect buttons.
- **Adaptive System Navigation Bar Insets:** Full edge-to-edge support seamlessly accommodating both classic 3-Button navigation and modern Gesture Navigation across all Android versions without clipping or content overlap.
- **RTL-Aware Outward Chevron Arrows:** Selected server and settings list chevrons now reliably point outward to the screen edge in both Persian (`<`) and English (`>`).
- **100% Reliable Foreground Notification Disconnect:** Direct service PendingIntent stop command terminating the DNS tunnel and displaying session summary instantly across all Android versions.
- **Developer Profile & About Section:**
  - Complete Mostafa Afrouzi developer profile (bio, official portfolio website with language-specific UTM parameters, GitHub, and LinkedIn).
  - Direct developer store links for CafeBazaar and Myket.

---

### Version 1.1.0
**Complete Apple iOS Human Interface Guidelines (HIG) Redesign & High-Performance DNS Engine.**

**What's New in v1.1.0:**
- **Apple iOS HIG Design Aesthetic:** Overhauled user interface to an authentic, minimalist Apple iOS aesthetic:
  - Inset Grouped cards with clean hairline dividers and standard iOS margins.
  - Native Segmented Controls for language (Persian / English) and theme switching with fluid animations.
  - Official Apple System Colors including Apple Blue (`#007AFF`), Apple Green (`#34C759`), pure OLED black (`#000000`), and Apple Light Grouped Background (`#F2F2F7`).
  - Spring-physics circular connection orb with `MediumBouncy` damping and gentle radiant aura.
- **DNS Resolution Engine Fixed:** Completely resolved website loading / buffer underflow issues in `DnsPacketForwarder`. Standard web and app traffic now flows with 0% packet loss and full connection bandwidth.
- **Enhanced Benchmark & Server List:** Streamlined iOS list cells, category filter pills, and clean ping latency badges.
- **Refined Custom DNS Screen:** Inset grouped form with header action buttons and inline input validation.
- **Enhanced Engine Stability:** Prevented premature file descriptor reclamation by Android's Garbage Collector.
