# An Nhiên — Ứng Dụng Sức Khỏe Phụ Nữ Ngoại Tuyến 100%

<p align="center">
  <b>Không gian chăm sóc sức khỏe phụ nữ độc lập, an toàn và riêng tư tuyệt đối trên Android.</b>
</p>

---

## Giới Thiệu Dự Án

**An Nhiên** (`com.annhien.womenshealth`) là ứng dụng chăm sóc sức khỏe phụ nữ toàn diện, hoạt động **100% ngoại tuyến (Zero-Network Architecture)**, được phát triển bằng **Kotlin**, **Jetpack Compose**, **Material 3** và **Room Database kết hợp mã hóa SQLCipher**.

Ứng dụng đáp ứng các tiêu chuẩn bảo mật y tế khắt khe nhất: không có tài khoản trực tuyến, không có SDK thu thập dữ liệu (no telemetry), không xin quyền `INTERNET`. Toàn bộ dữ liệu của bạn nằm trọn vẹn và an toàn trên thiết bị của chính bạn.

---

## Điểm Nổi Bật

- 🔒 **Bảo Mật Đa Lớp (Defense in Depth)**:
  - Cơ sở dữ liệu SQLite mã hóa toàn bộ bằng **SQLCipher (AES-256)** với mật mã quản lý qua **Android Keystore**.
  - Khóa ứng dụng bằng **Mã PIN (PBKDF2-HMAC-SHA256 120.000 vòng)** và **Sinh trắc học (BiometricPrompt)**.
  - Ngăn chụp màn hình và ẩn nội dung trong trình chuyển tác vụ với **FLAG_SECURE**.
  - Sao lưu và khôi phục dữ liệu ngoại tuyến dạng tệp `.annhien_backup` mã hóa **AES-256-GCM** kèm chữ ký toàn vẹn **HMAC-SHA256**.
- 🌸 **Chu Kỳ Kinh Nguyệt & Khoa Học Dự Đoán**:
  - Thuật toán `CyclePredictionEngine` tính toán dựa trên **số trung vị (Median)** các chu kỳ thực tế, loại bỏ ngoại lai.
  - Tuyệt đối **không hiển thị nhãn "ngày an toàn"** và **không áp đặt chu kỳ 28 ngày mặc định**.
  - Khóa chuyên môn lâm sàng `PENDING_METHOD_APPROVAL` cho tính năng ước tính thụ thai.
- 📱 **8 Chuyên Đề Sức Khỏe Toàn Diện**:
  1. **Chu kỳ kinh nguyệt**: Theo dõi ngày hành kinh, lịch sử chu kỳ và dự đoán.
  2. **Kế hoạch thụ thai**: Ghi nhiệt độ cơ thể nền (BBT) và kết quả que thử rụng trứng (LH).
  3. **Theo dõi thai kỳ**: Tuần tuổi thai, so sánh kích thước bé, bộ đếm thai máy và đồng hồ đo cơn gò chuyển dạ (Contractions Timer).
  4. **Phục hồi sau sinh**: Theo dõi sản dịch (Lochia), cữ cho bé bú và chăm sóc tinh thần hậu sản.
  5. **Tiền mãn kinh**: Đếm cơn bốc hỏa (Hot Flash Counter), theo dõi giấc ngủ và triệu chứng vận mạch.
  6. **Sức khỏe hàng ngày**: Theo dõi lượng nước uống, giấc ngủ, cân nặng và chỉ số BMI.
  7. **Thư giãn & Hơi thở**: Bài tập thở nhịp nhàng bằng hình động trực quan (Box Breathing 4-4-4-4 & Thư giãn 4-7-8, neo giữ thực tại 5-4-3-2-1) — không chèn audio giả.
  8. **Sổ khám bệnh**: Lưu trữ lịch sử khám bệnh, câu hỏi cho bác sĩ, kết luận và đơn thuốc.
- 📊 **Báo Cáo Ngoại Tuyến (Offline Export)**:
  - Xuất báo cáo tổng quan sức khỏe dạng **PDF** chuẩn mực (sử dụng thư viện iText 7).
  - Xuất bảng dữ liệu chu kỳ dạng **CSV** (sử dụng thư viện OpenCSV).

---

## Cấu Trúc Dự Án

```
annhien/
├── app/
│   ├── build.gradle.kts                        # Cấu hình Gradle, SDK 35, MinSdk 26
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml             # Tuyên bố quyền (KHÔNG có INTERNET)
│       │   ├── kotlin/com/annhien/womenshealth/
│       │   │   ├── AnNhienApplication.kt       # Hilt Application + WorkManager
│       │   │   ├── MainActivity.kt             # Entry Point, Edge-to-Edge, FLAG_SECURE
│       │   │   ├── core/                       # Database, Security, Preferences, Export, Time
│       │   │   ├── data/repository/            # Concrete Repository implementations
│       │   │   ├── di/                         # Hilt dependency injection modules
│       │   │   ├── domain/                     # Pure Kotlin models, usecases, engines
│       │   │   ├── feature/                    # 12+ Jetpack Compose screens & ViewModels
│       │   │   └── navigation/                 # NavGraph, Screen sealed class, BottomNav
│       │   └── res/                            # Strings tiếng Việt, Themes, Launcher icons
│       └── test/                               # Unit test suite (JUnit, MockK)
├── docs/
│   ├── ARCHITECTURE.md                         # Kiến trúc Clean Architecture, Zero-network
│   ├── DATA_MODEL.md                           # Chi tiết 28 entities & schema quan hệ
│   ├── SECURITY.md                             # Khóa Keystore, SQLCipher, PBKDF2, FLAG_SECURE
│   ├── PREDICTION_METHODS.md                   # Đặc tả thuật toán trung vị, IQR, leap year
│   ├── MEDICAL_CONTENT_REVIEW.md               # Thẩm định y khoa, tuyên bố miễn trừ
│   └── IMPLEMENTATION_STATUS.md                # Bảng đối soát tiến độ 100% hoàn thành
├── gradle/libs.versions.toml                   # Ghim phiên bản tất cả dependency
└── README.md
```

---

## Hướng Dẫn Mở Dự Án & Build APK

> [!NOTE]
> Môi trường yêu cầu:
> - **Android Studio** (Bản Iguana, Jellyfish, Koala hoặc mới hơn).
> - **JDK 17** (Đã đi kèm sẵn trong Android Studio).
> - **Android SDK 35** (Android Studio sẽ tự động tải khi mở dự án).

### Các Bước Thực Hiện:
1. Mở **Android Studio**.
2. Chọn **Open** (hoặc `File -> Open`).
3. Điều hướng tới thư mục:
   ```
   C:\Users\taikh\OneDrive\Desktop\Antigravity\App điện thoại\Sức khỏe\annhien
   ```
4. Chờ Android Studio đồng bộ Gradle (Sync Project with Gradle Files). Lần đầu tiên Gradle sẽ tải các thư viện từ kho lưu trữ về máy.
5. Để build tệp APK cài đặt:
   - Trong Android Studio: Chọn menu **Build** -> **Build Bundle(s) / APK(s)** -> **Build APK(s)**.
   - Hoặc chạy lệnh trong Terminal:
     ```bash
     ./gradlew assembleDebug
     ```
   - Tệp APK hoàn chỉnh sẽ nằm tại: `app/build/outputs/apk/debug/app-debug.apk`.
6. Để chạy toàn bộ Unit Tests:
   ```bash
   ./gradlew test
   ```

---

## Bản Quyền & Cam Kết Bảo Mật
Dự án được thiết kế vì sức khỏe và sự riêng tư của phụ nữ Việt Nam. Mọi thông tin sức khỏe cá nhân mãi mãi thuộc về chính bạn.
