# Kiến Trúc Ứng Dụng An Nhiên (An Nhien Architecture)

## 1. Triết Lý Thiết Kế Cốt Lõi
- **100% Ngoại Tuyến (Zero-Network Architecture)**: Ứng dụng không khai báo quyền `android.permission.INTERNET` trong `AndroidManifest.xml`. Không có SDK phân tích (Analytics), không có Crashlytics, không gửi bất kỳ gói tin nào ra mạng ngoài.
- **Mã Hóa Đa Tầng (Multi-layered Security)**:
  - Tầng dữ liệu: SQLite database được mã hóa hoàn toàn bằng SQLCipher (AES-256).
  - Tầng khóa: Master Key được bảo vệ bởi phần cứng Android Keystore (KeyStoreProvider).
  - Tầng truy cập: PIN được băm PBKDF2-HMAC-SHA256 (100.000 vòng) kết hợp Sinh trắc học (BiometricPrompt).
  - Tầng hiển thị: Chế độ `FLAG_SECURE` ngăn chụp màn hình và làm mờ cửa sổ trong trình chuyển đổi tác vụ (Recent Apps).
- **Kiến Trúc Sạch (Clean Architecture & MVI/MVVM)**: Phân tách độc lập giữa Domain (thuần Kotlin), Data (Room, Repositories), và UI (Jetpack Compose).

---

## 2. Sơ Đồ Khối Kiến Trúc

```
┌─────────────────────────────────────────────────────────────┐
│                    Tầng Giao Diện (UI Layer)               │
│  - Jetpack Compose + Material 3                             │
│  - Navigation Compose (Type-safe NavHost)                   │
│  - ViewModels (StateFlow, UiState, SavedStateHandle)         │
│  - Quản lý trạng thái: HomeScreen, Calendar, Journal...     │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                   Tầng Nghiệp Vụ (Domain Layer)            │
│  - Pure Kotlin (Không phụ thuộc Android Framework)          │
│  - CyclePredictionEngine (Dự đoán chu kỳ theo trung vị)    │
│  - FertilityEstimateEngine (Khóa chờ thẩm định lâm sàng)    │
│  - Repository Interfaces                                    │
│  - Domain Models (PeriodEpisode, CycleSummary, ...)         │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    Tầng Dữ Liệu (Data Layer)                │
│  - Room Database (AnNhienDatabase - 28 Entities, 28 DAOs)   │
│  - SQLCipher SupportFactory (Mã hóa toàn bộ đĩa lưu trữ)    │
│  - Concrete Repositories (CycleRepositoryImpl, ...)         │
│  - AppPreferences (Jetpack DataStore Preferences)           │
│  - KeystoreManager & PinManager                             │
│  - BackupManager (ZIP container + AES-GCM + HMAC-SHA256)    │
│  - NotificationScheduler & AlarmReceivers (Offline alarms)  │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Cấu Trúc Gói Nguồn (Package Structure)
```
com.annhien.womenshealth
├── AnNhienApplication.kt       # Application class, khởi tạo WorkManager & Hilt
├── MainActivity.kt             # Activity duy nhất, Edge-to-Edge, FLAG_SECURE, App Lock
├── core/
│   ├── backup/                 # Sao lưu/khôi phục mã hóa ngoại tuyến
│   ├── common/                 # AppResult, tiện ích dùng chung
│   ├── database/               # Room database, 28 entities, 28 DAOs, type converters
│   ├── design/                 # Theme Material 3, bảng màu Sage/Rose, Typography
│   ├── notifications/          # BootReceiver, ReminderAlarmReceiver, NotificationScheduler
│   ├── preferences/            # DataStore lưu cài đặt người dùng
│   ├── reports/                # Xuất báo cáo PDF (iText 7) & CSV (OpenCSV)
│   ├── security/               # KeystoreManager, PinManager
│   └── time/                   # DateTimeProvider (hỗ trợ giả lập kiểm thử)
├── data/
│   └── repository/             # Triển khai các Repository giao tiếp Room
├── di/
│   ├── AppModule.kt            # Cung cấp Database, DAOs, Security singletons
│   └── RepositoryModule.kt     # Bindings Repository interfaces
├── domain/
│   ├── model/                  # Domain Data classes & Enums
│   ├── repository/             # Repository interfaces
│   └── usecase/                # CyclePredictionEngine, FertilityEstimateEngine
├── feature/                    # Các màn hình chức năng Compose
│   ├── calendar/
│   ├── conception/
│   ├── cycle/
│   ├── dailyhealth/
│   ├── home/
│   ├── journal/
│   ├── knowledge/
│   ├── lock/
│   ├── medicalrecords/
│   ├── onboarding/
│   ├── perimenopause/
│   ├── postpartum/
│   ├── pregnancy/
│   ├── relaxation/
│   ├── reports/
│   ├── settings/
│   └── utilities/
└── navigation/                 # Screen sealed class, NavGraph, BottomNavScaffold
```

---

## 4. Nguyên Tắc An Toàn Y Khoa
1. **Dự đoán chu kỳ**: Sử dụng số trung vị (Median) và khoảng biến thiên thực tế, không bao giờ tự ý áp đặt con số mặc định 28 ngày.
2. **Không bao giờ hiển thị 'Ngày an toàn'**: Cấm hoàn toàn việc gắn nhãn ngày an toàn nhằm phòng ngừa mang thai ngoài ý muốn.
3. **Cơ chế khóa tính năng ước tính thụ thai**: Đặt mặc định trạng thái `PENDING_METHOD_APPROVAL` kèm cảnh báo y tế bắt buộc, yêu cầu xác nhận chuyên môn trước khi được phép kích hoạt ước tính mở rộng.
