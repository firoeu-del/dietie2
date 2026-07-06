# ساخت dietie بدون Android Studio

## روش پیشنهادی: GitHub Actions

1. در github.com یک اکانت بساز یا وارد شو.
2. یک repository جدید بساز، مثلا با نام `dietie`.
3. محتوای پوشه `dietie_android` را داخل repository آپلود کن. خود فایل zip را آپلود نکن؛ فایل‌ها و پوشه‌ها را از حالت zip خارج کن.
4. اگر پوشه مخفی `.github` هنگام آپلود نیامد، از داخل GitHub روی **Add file > Create new file** بزن و نام فایل را دقیقا این بگذار:

   `.github/workflows/build-debug-apk.yml`

   سپس محتوای فایل موجود در همین پروژه را داخلش paste کن و Commit کن.
5. وارد تب **Actions** شو.
6. workflow با اسم **Build dietie APK** را باز کن.
7. روی **Run workflow** بزن.
8. بعد از سبز شدن build، پایین صفحه بخش **Artifacts** را باز کن.
9. فایل `dietie-debug-apk` را دانلود کن، از zip خارج کن و APK داخلش را روی گوشی نصب کن.

## خروجی کجاست؟

داخل artifact این مسیر ساخته می‌شود:

`app/build/outputs/apk/debug/app-debug.apk`

## نکته امنیتی

این APK نسخه debug است و برای تست و نصب شخصی خوب است. برای انتشار در Google Play باید نسخه release با signing key بسازیم.
