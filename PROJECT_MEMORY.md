# BỘ NHỚ DỰ ÁN (PROJECT MEMORY) — AN NHIÊN (WOMEN'S HEALTH APP)

> **Mục đích tài liệu:** Lưu trữ toàn bộ thông tin quan trọng của dự án, kiến trúc cốt lõi, danh sách các bug gặp phải, nguyên nhân gốc rễ và giải pháp sửa chữa để phục vụ quá trình bảo trì, mở rộng và ghi nhớ lịch sử phát triển.

---

## 1. THÔNG TIN TỔNG QUAN DỰ ÁN

| Thuộc tính | Chi tiết |
| :--- | :--- |
| **Tên ứng dụng** | **An Nhiên** (Women's Health App) |
| **Package Name** | `com.annhien.womenshealth` |
| **Ngôn ngữ chính** | Kotlin (100%), Kotlin Coroutines & Flow |
| **Giao diện (UI)** | Jetpack Compose + Material Design 3 (Edge-to-Edge) |
| **Phiên bản SDK** | minSdk: `26` (Android 8.0) \| targetSdk & compileSdk: `35` (Android 15) |
| **Cơ sở dữ liệu** | Room Database `2.6.1` kết hợp mã hóa **SQLCipher `4.5.4`** |
| **Dependency Injection** | Dagger Hilt `2.52` + Hilt Navigation Compose |
| **Quản lý cấu hình** | Jetpack DataStore Preferences |
| **Bảo mật sinh trắc học** | AndroidX BiometricPrompt (`1.2.0-alpha05`) |
| **Bộ công cụ Build** | Gradle 8.7.0, KSP 2.0.21-1.0.27, JDK 17 |
| **Triết lý sản phẩm** | **100% Offline (Zero-Network Architecture)**. Tuyệt đối không xin quyền `INTERNET`, không thu thập dữ liệu (no telemetry/analytics), bảo vệ quyền riêng tư y tế tối đa. |

---

## 2. KIẾN TRÚC & CÁC FILE TRỌNG YẾU

```
annhien/
├── app/src/main/
│   ├── AndroidManifest.xml                        # Khai báo permissions, Receiver, Application, MainActivity
│   ├── kotlin/com/annhien/womenshealth/
│   │   ├── AnNhienApplication.kt                  # Entry point Application, khởi tạo SQLCipher, cấu hình WorkManager
│   │   ├── MainActivity.kt                        # AppCompatActivity (cho BiometricPrompt), Compose Entry, FLAG_SECURE
│   │   ├── core/
│   │   │   ├── security/KeystoreManager.kt        # Quản lý khóa Android Keystore (AES-GCM-256), bọc mật mã DB
│   │   │   ├── database/AnNhienDatabase.kt        # Room Database với 28 Entities, TypeConverters
│   │   │   ├── backup/BackupManager.kt            # Sao lưu / Khôi phục tệp .annhien_backup (AES-GCM + HMAC-SHA256)
│   │   │   ├── preferences/AppPreferences.kt      # DataStore lưu trạng thái onboarding, khóa PIN, sinh trắc, theme
│   │   │   ├── notifications/                     # ReminderAlarmReceiver, NotificationScheduler (AlarmManager)
│   │   │   └── design/Theme.kt                    # Bảng màu Sage Green, Typography, AnNhienTheme
│   │   ├── di/AppModule.kt                        # Hilt Singleton: Room + SQLCipher SupportFactory, DAOs
│   │   ├── navigation/NavGraph.kt                 # Quản lý điều hướng màn hình (Onboarding, Home Tabs, Sub-modules)
│   │   └── feature/                               # 12+ màn hình chuyên đề (Kinh nguyệt, Thụ thai, Thai kỳ, v.v.)
│   └── res/values/themes.xml                      # Cấu hình theme hệ thống (Theme.AppCompat.DayNight.NoActionBar)
├── build_apk.ps1                                  # Script PowerShell tự động build APK và copy ra root
├── build_apk.bat                                  # Script Batch Windows tự động build APK
└── app-debug.apk                                  # Tệp APK hoàn chỉnh sẵn sàng cài đặt
```

---

## 3. NHẬT KÝ BUG VÀ CÁC LẦN SỬA LỖI (BUG FIX LOG)

### 📌 BUG #01: Ứng dụng bị "Bắt buộc dừng" (Force Close / Crash on Launch) ngay khi cài trên điện thoại thật
* **Ngày phát hiện:** 17/10/2026
* **Triệu chứng:** Người dùng cài đặt file APK trên điện thoại Android thật, khi bấm vào biểu tượng app thì lập tức văng ra màn hình chính kèm thông báo hệ thống: *"Ứng dụng An Nhiên liên tục dừng"* / *"Bắt buộc dừng"*.
* **Phân tích nguyên nhân gốc rễ (Root Causes):**
  1. **Xung đột Theme với `AppCompatActivity` (Nguyên nhân chính):**
     - Trong [`MainActivity.kt`](file:///c:/Users/taikh/OneDrive/Desktop/Antigravity/App%20%C4%91i%E1%BB%87n%20tho%E1%BA%A1i/S%E1%BB%A9c%20kh%E1%BB%8Fe/annhien/app/src/main/kotlin/com/annhien/womenshealth/MainActivity.kt): Activity kế thừa `AppCompatActivity` (cần thiết để tương thích với `BiometricPrompt`).
     - Tuy nhiên trong [`res/values/themes.xml`](file:///c:/Users/taikh/OneDrive/Desktop/Antigravity/App%20%C4%91i%E1%BB%87n%20tho%E1%BA%A1i/S%E1%BB%A9c%20kh%E1%BB%8Fe/annhien/app/src/main/res/values/themes.xml) và `values-night/themes.xml`: Lại cấu hình `parent="android:Theme.Material.Light.NoActionBar"`.
     - `android:Theme...` là theme framework thuần của Android, không kế thừa từ `Theme.AppCompat`. Do đó, khi `AppCompatActivity.onCreate()` khởi chạy, lớp kiểm tra nội bộ `AppCompatDelegate` đã ném ra ngoại lệ:
       `java.lang.IllegalStateException: You need to use a Theme.AppCompat theme (or descendant) with this activity.`
  2. **Thiếu nạp thư viện native của SQLCipher:**
     - SQLCipher phụ thuộc vào các file nhị phân `.so` (`libsqlcipher.so`). Nếu không gọi lệnh `SQLiteDatabase.loadLibs(context)` khi ứng dụng khởi chạy thì tại lần đầu Room truy xuất cơ sở dữ liệu sẽ ném `UnsatisfiedLinkError` hoặc crash native.
  3. **Mất mật khẩu Database trong bộ nhớ RAM (`passphrase.fill(0)`):**
     - Trong [`AppModule.kt`](file:///c:/Users/taikh/OneDrive/Desktop/Antigravity/App%20%C4%91i%E1%BB%87n%20tho%E1%BA%A1i/S%E1%BB%A9c%20kh%E1%BB%8Fe/annhien/app/src/main/kotlin/com/annhien/womenshealth/di/AppModule.kt), lệnh `val factory = SupportFactory(passphrase)` được theo sau ngay lập tức bởi `passphrase.fill(0)`.
     - Vì `SupportFactory` lưu trực tiếp tham chiếu tới mảng byte mà không clone, lệnh `fill(0)` làm mật khẩu bị đổi thành 32 số 0 trước khi Room kịp mở file database.
  4. **WorkManager `lateinit` crash rủi ro:**
     - Trong [`AnNhienApplication.kt`](file:///c:/Users/taikh/OneDrive/Desktop/Antigravity/App%20%C4%91i%E1%BB%87n%20tho%E1%BA%A1i/S%E1%BB%A9c%20kh%E1%BB%8Fe/annhien/app/src/main/kotlin/com/annhien/womenshealth/AnNhienApplication.kt), thuộc tính `workerFactory` được tiêm bởi Hilt. Nếu `workManagerConfiguration` được hệ điều hành truy vấn sớm trước khi hoàn tất injection, app sẽ bị `UninitializedPropertyAccessException`.
  5. **Ngoại lệ Keystore trên một số dòng máy (Samsung Knox, Xiaomi MIUI, Oppo ColorOS):**
     - Hàm lấy mật khẩu mã hóa trong [`KeystoreManager.kt`](file:///c:/Users/taikh/OneDrive/Desktop/Antigravity/App%20%C4%91i%E1%BB%87n%20tho%E1%BA%A1i/S%E1%BB%A9c%20kh%E1%BB%8Fe/annhien/app/src/main/kotlin/com/annhien/womenshealth/core/security/KeystoreManager.kt) chưa có khối try-catch bảo vệ trong trường hợp KeyStore phần cứng bị reset hoặc ném lỗi giải mã.

* **Các chỉnh sửa đã thực hiện (Fixes Applied):**
  1. Đổi `parent` trong `themes.xml` và `values-night/themes.xml` thành `Theme.AppCompat.DayNight.NoActionBar`.
  2. Bổ sung `net.sqlcipher.database.SQLiteDatabase.loadLibs(this)` trong `AnNhienApplication.onCreate()` và `AppModule.kt`.
  3. Xóa lệnh `passphrase.fill(0)` trong `AppModule.kt`, để `SupportFactory` tự xử lý xóa an toàn sau khi kết nối DB thành công.
  4. Thêm `.fallbackToDestructiveMigration()` vào `Room.databaseBuilder` để ngăn lỗi crash do lệch schema trong quá trình phát triển.
  5. Thêm kiểm tra an toàn `if (::workerFactory.isInitialized)` trong `AnNhienApplication.workManagerConfiguration`.
  6. Bọc try-catch phòng thủ trong `KeystoreManager.getDatabasePassphrase()`, tự phục hồi nếu key cũ bị lỗi.

---

### 📌 BUG #02: File APK ở thư mục gốc không tự cập nhật sau khi build
* **Ngày phát hiện:** 17/10/2026
* **Triệu chứng:** Người dùng chạy script build nhưng tệp `app-debug.apk` ngoài thư mục dự án vẫn giữ nguyên kích thước/timestamp cũ, khiến khi copy vào điện thoại vẫn là bản chưa được sửa lỗi.
* **Nguyên nhân:** Gradle xuất tệp ra `app/build/outputs/apk/debug/app-debug.apk`, script build cũ chỉ chạy `assembleDebug` mà chưa có lệnh copy tự động ra thư mục gốc.
* **Giải pháp đã fix:**
### 📌 BUG #03: Giao diện app bị lệch so với bản mô phỏng web & khó đánh dấu kỳ kinh nguyệt
* **Ngày phát hiện:** 17/10/2026
* **Triệu chứng:** Người dùng phản ánh giao diện khi cài lên máy không đồng bộ về màu sắc, độ hoàn thiện so với bản web mô phỏng (`web_simulator`); đồng thời việc đánh dấu kỳ kinh nguyệt trên lịch rất bất tiện, chỉ có nút text nhỏ khó nhận biết trạng thái đã đánh dấu hay chưa.
* **Nguyên nhân gốc rễ:**
  1. **Lệch bảng màu (Palette):** Giao diện web dùng màu Sage Green `#3D684E`, Mint Container `#D8E7DB`, Coral Red `#BA1A1A`, còn Android trước đó dùng Sage `#6B9E5E` nhạt và nền màu be ngà, thiếu gradient mềm mại ở `CycleHeroCard`.
  2. **Cơ chế đánh dấu kinh trên lịch bị lỗi logic:** Hàm cũ trên `CalendarViewModel` nếu chọn 1 ngày chưa có kinh thì tạo kỳ kinh với `endDate = null`, khiến hệ thống tự động bôi đỏ tất cả các ngày từ ngày đó đến hôm nay. Khi bấm lại vào một ngày bất kỳ thì lại xóa đứt toàn bộ cả kỳ kinh. Không có cách nào đánh dấu chu kỳ 4-5 ngày một cách nhanh gọn.
* **Giải pháp đã fix:**
  1. **Đồng bộ màu sắc 100% với Web Simulator:** Cập nhật [`Theme.kt`](file:///c:/Users/taikh/OneDrive/Desktop/Antigravity/App%20%C4%91i%E1%BB%87n%20tho%E1%BA%A1i/S%E1%BB%A9c%20kh%E1%BB%8Fe/annhien/app/src/main/kotlin/com/annhien/womenshealth/core/design/Theme.kt) chuẩn xác theo `styles.css` (Sage Green `#3D684E`, Container `#D8E7DB`, Coral Red `#BA1A1A`, Lavender `#5F5B7D`).
  2. **Nâng cấp `CycleHeroCard` ở Home:** Nền dốc gradient bạc hà thanh lịch, badge `🌸 Đang trong kỳ kinh nguyệt` hoặc `Ngày X của chu kỳ`, nút hành động lớn 50dp "Kỳ kinh bắt đầu hôm nay" / "Đánh dấu kết thúc kỳ kinh" 1 chạm.
  3. **Hệ thống đánh dấu kinh 1 chạm trên Lịch ([`CalendarScreen.kt`](file:///c:/Users/taikh/OneDrive/Desktop/Antigravity/App%20%C4%91i%E1%BB%87n%20tho%E1%BA%A1i/S%E1%BB%A9c%20kh%E1%BB%8Fe/annhien/app/src/main/kotlin/com/annhien/womenshealth/feature/calendar/CalendarScreen.kt)):**
     - Chạm vào ngày bất kỳ: Nút to rõ ràng hiển thị trạng thái hiện tại (`🌸 Ngày có kinh` / `⚪ Không có kinh`).
     - Bấm nút **"Đánh dấu có kinh" / "Hủy ngày kinh"**: Bật/tắt ngày đó ngay lập tức (tự động nối liền các ngày liên tiếp thành 1 chu kỳ).
     - Thêm nút **"Đặt nhiều ngày"**: Mở hộp thoại chọn nhanh chu kỳ (3 ngày, 4 ngày, 5 ngày, 6 ngày, 7 ngày) tính từ ngày được chọn, bấm "Lưu kỳ kinh" là hoàn tất.

---

## 4. QUY TẮC CẦN TUÂN THỦ KHI TIẾP TỤC CODE (DEVELOPMENT GUARDRAILS)

1. **Tuyệt đối không cấp quyền INTERNET:**
   - Dự án được cam kết bảo mật 100% offline. Không được thêm `<uses-permission android:name="android.permission.INTERNET" />` vào `AndroidManifest.xml`.
2. **Quy tắc về Theme & Activity:**
   - Nếu `MainActivity` vẫn dùng `AppCompatActivity` (để gọi `BiometricPrompt`), theme của app bắt buộc phải là con cháu của `Theme.AppCompat`.
3. **Quy tắc về Room & SQLCipher:**
   - Mọi thay đổi về Entity phải cập nhật version trong `AnNhienDatabase` và tạo migration tương ứng.
   - Luôn nạp thư viện `loadLibs` trước khi can thiệp vào SQLiteDatabase mã hóa.
4. **Quy tắc về Y học & Lâm sàng:**
   - Không được áp dụng chu kỳ 28 ngày cố định cho tất cả phụ nữ (phải dùng thuật toán Median tính từ lịch sử chu kỳ thực tế).
   - Tuyệt đối không hiển thị khái niệm "ngày quan hệ an toàn" để tránh rủi ro mang thai ngoài ý muốn.
   - Mọi dự đoán đều phải kèm theo tuyên bố từ chối trách nhiệm y tế (Medical Disclaimer).
5. **Cách build APK:**
   - Chạy lệnh: `powershell -ExecutionPolicy Bypass -File .\build_apk.ps1` hoặc click đúp file `build_apk.bat`.
   - File APK cài đặt hoàn chỉnh luôn nằm tại: `app-debug.apk` ngay tại thư mục gốc dự án.
