# An Nhiên — Nhật ký triển khai

## 2026-10-17 — Đánh giá và nâng cấp nền tảng dữ liệu

### Đánh giá lần đầu

Repository đã có:

- Dự án Android Kotlin/Jetpack Compose thực tế.
- Cấu trúc `core`, `domain`, `data`, `feature`, `di`.
- Room với SQLCipher.
- Android Keystore, PIN và biometric.
- Backup/restore.
- PDF/CSV export.
- Các thư mục cho 8 mô-đun sức khỏe.

### Vấn đề ưu tiên phát hiện

1. `BackupManager` sao lưu tệp database SQLCipher nhưng chưa thấy cơ chế chuyển đổi/khôi phục passphrase database trên thiết bị khác.
2. `KeystoreManager` tự xóa passphrase đã lưu và sinh passphrase mới khi lỗi; điều này có thể làm database cũ không mở được.
3. `AppModule` bật `fallbackToDestructiveMigration()` trong khi tài liệu ghi destructive migration bị tắt.
4. Restore dùng `copyTo(overwrite=true)` nhưng chưa có quy trình đóng database, kiểm tra staging, rollback và xử lý WAL/SHM.
5. Backup chưa bao gồm attachments và các thiết lập cần thiết.

### Kế hoạch nâng cấp

#### Đợt 1 — Nền tảng dữ liệu an toàn

- [x] Tách trạng thái khởi tạo lần đầu khỏi lỗi mở database.
- [x] Không tự xóa hoặc sinh lại passphrase khi lỗi.
- [x] Loại bỏ destructive migration khỏi cấu hình phát hành.
- [x] Thiết kế backup di chuyển thiết bị, bao gồm passphrase database được bảo vệ bằng mật khẩu backup.
- [x] Restore qua staging, giới hạn dung lượng và rollback.
- [x] Backup/restore attachments trong thư mục nội bộ của app.
- [x] Kiểm tra danh sách tài liệu trong manifest và chống đường dẫn độc hại.
- [x] Backup database và xử lý các tệp WAL/SHM liên quan.
- [x] Bổ sung unit test cơ bản cho backup/restore.

#### Đợt 2 — Nền tảng tiện ích trực tuyến

- [ ] Cấu trúc offline-first: lõi sức khỏe không phụ thuộc Internet.
- [ ] Quản lý nguồn API và trạng thái bật/tắt.
- [ ] Quản lý hạn mức, thời gian chờ và trạng thái bị giới hạn.
- [ ] Cache riêng theo nguồn, có thời điểm cập nhật và giới hạn dung lượng.
- [ ] Phân biệt mất mạng, hết hạn mức, sai key, không tìm thấy và lỗi dịch vụ.
- [ ] Thông báo trong app khi tiện ích bị giới hạn và cho thử lại ngày sau.

#### Đợt 3 — Việt hóa và tiện ích mới

- [ ] Việt hóa giao diện, lỗi, nhãn dữ liệu và thông báo.
- [ ] Từ điển dưỡng chất, chỉ số môi trường, nhóm bài tập.
- [ ] Tích hợp Open Food Facts, USDA FoodData Central, NIH DSLD, ODS.
- [ ] Tích hợp MyHealthfinder/MedlinePlus với nội dung có trạng thái rà soát.
- [ ] Tích hợp wger, MET Norway, OpenAQ, Open Beauty Facts theo điều kiện giấy phép.
- [ ] Tích hợp Openverse, RxTerms, DailyMed với giới hạn rõ.
- [ ] Không dịch tên thuốc; giữ nguyên tên thuốc, thương hiệu và mã sản phẩm.

### Quy tắc tích hợp API

- Chỉ dùng nguồn miễn phí phù hợp với mục đích phát hành.
- Không dùng gói bắt buộc billing hoặc trả phí khi phát hành.
- Không dùng nguồn chỉ miễn phí phi thương mại nếu An Nhiên có mục đích thương mại.
- Khi hết hạn mức: tạm dừng tiện ích liên quan, thông báo trong app, cho thử lại ngày sau hoặc muộn hơn theo yêu cầu nhà cung cấp.
- Không lách giới hạn bằng đổi key/IP/tài khoản.
- Không gửi toàn bộ hồ sơ sức khỏe cho dịch vụ tra cứu.
- Không tự chẩn đoán, không hiển thị "ngày an toàn".
- Nếu nguồn bắt đầu yêu cầu trả phí: tắt nguồn, không tự chấp nhận phí.

### Trạng thái hiện tại

- Đã tạo nhánh `upgrade/data-safety` từ `main`.
- Đã sửa `KeystoreManager`: không tự xóa hoặc sinh lại passphrase khi lỗi; thêm `importDatabasePassphrase`.
- Đã loại bỏ `fallbackToDestructiveMigration()` khỏi `AppModule`.
- Đã nâng backup lên phiên bản 2 và thêm passphrase database được bảo vệ bằng mật khẩu backup.
- Đã bổ sung giới hạn dung lượng khi đọc ZIP, staging và rollback trong restore.
- Đã backup/restore tài liệu trong `filesDir/attachments`, kiểm tra manifest và chống path traversal.
- Đã backup các tệp WAL/SHM liên quan database nếu tồn tại.
- Đã thêm kiểm thử cơ bản cho backup/restore.
- Chưa build hoặc chạy kiểm thử.
- Chưa tích hợp tiện ích trực tuyến.
