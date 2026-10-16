# Chính Sách & Kiến Trúc Bảo Mật An Nhiên (An Nhien Security Architecture)

Ứng dụng An Nhiên được xây dựng theo chuẩn mực bảo mật thông tin y tế cá nhân cao cấp nhất, tuân thủ nguyên tắc bảo vệ quyền riêng tư theo thiết kế (Privacy by Design).

---

## 1. Bảo Đảm Ngoại Tuyến Tuyệt Đối (Air-Gapped Privacy)
- **Không quyền mạng**: File cấu hình `AndroidManifest.xml` không chứa quyền `android.permission.INTERNET`.
- **Không thư viện thu thập dữ liệu (No Telemetry)**: Không sử dụng Google Analytics, Firebase, Facebook SDK hay bất kỳ SDK mạng bên thứ ba nào.
- **Không tạo tài khoản / Không định danh từ xa**: Không yêu cầu email, số điện thoại, tài khoản mạng xã hội hay máy chủ đám mây.

---

## 2. Mã Hóa Cơ Sở Dữ Liệu (SQLCipher for Android)
- Toàn bộ cơ sở dữ liệu Room được mã hóa toàn bộ trang (Full Page Encryption) bằng **SQLCipher** với thuật toán **AES-256-CBC** kết hợp hàm tạo khóa PBKDF2.
- **Quản lý mật mã qua Android Keystore**:
  - Mật khẩu truy cập cơ sở dữ liệu không bao giờ được viết cứng trong code.
  - Được sinh ngẫu nhiên 256-bit bằng `SecureRandom` khi người dùng cài đặt ứng dụng lần đầu.
  - Được mã hóa và niêm phong bằng khóa AES-256-GCM lưu trữ trong vùng bảo mật phần cứng an toàn của thiết bị (Android Keystore Provider).
  - Vùng nhớ chứa mật mã dạng mảng byte sẽ được ghi đè bằng `0` ngay sau khi mở kết nối cơ sở dữ liệu (`passphrase.fill(0)`).

---

## 3. Khóa Ứng Dụng (App Lock & Biometrics)
- **Băm mã PIN**:
  - Mã PIN do người dùng đặt không lưu dưới dạng văn bản thô.
  - Được băm bằng thuật toán chuẩn y tế **PBKDF2-HMAC-SHA256** với muối ngẫu nhiên (Salt 16 bytes) và **120.000 vòng lặp** (iterations).
  - So sánh mã PIN bằng thuật toán so khớp thời gian hằng số (`constantTimeEquals`) để ngăn chặn hoàn toàn tấn công kênh phụ phân tích thời gian (Timing Attacks).
- **Xác thực sinh trắc học**:
  - Sử dụng API chính thức `androidx.biometric.BiometricPrompt` của hệ điều hành Android.
  - Hỗ trợ cảm biến vân tay và nhận diện khuôn mặt an toàn phần cứng (Class 3 Biometrics).

---

## 4. Chống Rò Rỉ Màn Hình (Screen Security & FLAG_SECURE)
- Khi tính năng bảo vệ màn hình được bật, `MainActivity` tự động áp dụng cờ hệ thống:
  ```kotlin
  window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
  ```
- Tác dụng:
  - Ngăn chặn người dùng hoặc ứng dụng độc hại chụp ảnh màn hình (Screenshot) hoặc quay video màn hình.
  - Làm mờ hoặc ẩn hoàn toàn giao diện ứng dụng trong danh sách các ứng dụng gần đây (Recent Apps Task Switcher), ngăn ngừa người đứng cạnh nhìn trộm dữ liệu nhạy cảm.

---

## 5. Sao Lưu Ngoại Tuyến An Toàn (Encrypted Backup Architecture)
- Định dạng tệp: Container `.annhien_backup` (định dạng nén ZIP bảo mật).
- Quy trình mã hóa tệp sao lưu:
  1. Người dùng tự nhập mật khẩu sao lưu riêng.
  2. Dùng muối ngẫu nhiên 16 bytes sinh khóa AES 256-bit qua PBKDF2 (100.000 vòng).
  3. Mã hóa toàn bộ tệp dump cơ sở dữ liệu bằng **AES-256-GCM** (xác thực tính toàn vẹn và chống tráo đổi nội dung).
  4. Tính toán chữ ký băm **HMAC-SHA256** trên toàn bộ khối dữ liệu mã hóa.
  5. Đóng gói Manifest JSON + Khối dữ liệu mã hóa + Mã kiểm tra HMAC vào tệp `.annhien_backup`.
- Quy trình khôi phục:
  - Kiểm tra chữ ký HMAC trước khi giải mã. Nếu tệp bị hỏng hoặc bị can thiệp dù chỉ 1 bit, quá trình khôi phục sẽ bị từ chối ngay lập tức để bảo vệ an toàn cho thiết bị.
