## فارسی

نسخهٔ **۱.۱.۰** — بازطراحی کامل بر پایه استانداردهای اپل iOS و بهبود پایداری هسته DNS.

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

Version **1.1.0** — Complete Apple iOS Human Interface Guidelines (HIG) Redesign & High-Performance DNS Engine.

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
