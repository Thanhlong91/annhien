# Mô Hình Dữ Liệu An Nhiên (An Nhien Data Model)

Cơ sở dữ liệu được xây dựng trên nền tảng **Room Persistence Library** kết hợp mã hóa tầng thấp **SQLCipher for Android**. Gồm 28 bảng (Entities) với quan hệ toàn vẹn, ràng buộc khóa ngoại (Foreign Keys) và chỉ mục tìm kiếm tối ưu (Indices).

---

## 1. Danh Mục Các Bảng Dữ Liệu

| STT | Tên Bảng (Entity) | Mục Đích Lưu Trữ | Khóa Ngoại / Chỉ Mục |
|---|---|---|---|
| 1 | `user_profile` | Thông tin người dùng, đơn vị đo, giai đoạn hiện tại | PrimaryKey: `id` |
| 2 | `module_preference` | Bật/tắt các phân hệ chức năng | Index: `moduleId` |
| 3 | `life_stage_history` | Lịch sử chuyển đổi giai đoạn cuộc đời | Foreign key: `user_profile` |
| 4 | `period_episode` | Các kỳ kinh nguyệt (bắt đầu, kết thúc, cờ bất thường) | Indices: `startDate`, `isOngoing` |
| 5 | `bleeding_observation` | Dấu hiệu xuất huyết / máu báo ngoài kỳ | Foreign key: `period_episode` |
| 6 | `symptom_definition` | Danh mục triệu chứng thể chất & tâm lý | Index: `category` |
| 7 | `journal_entry` | Nhật ký sức khỏe theo ngày | Index: `eventDate` |
| 8 | `symptom_observation` | Bản ghi chi tiết triệu chứng và độ nặng (1-5) | Foreign key: `journal_entry` (CASCADE) |
| 9 | `mood_entry` | Tâm trạng trong ngày và các nhãn cảm xúc | Foreign key: `journal_entry` (CASCADE) |
| 10 | `sleep_entry` | Giờ ngủ, giờ thức, tổng phút và chất lượng | Foreign key: `journal_entry` (CASCADE) |
| 11 | `measurement_entry` | Các chỉ số đo (BBT, huyết áp, cân nặng, đường huyết) | Foreign key: `journal_entry` (CASCADE) |
| 12 | `fertility_observation` | Chất nhầy cổ tử cung, vị trí CTC, dấu hiệu thụ thai | Foreign key: `journal_entry` (CASCADE) |
| 13 | `fertility_estimate_settings` | Cấu hình bật/tắt và trạng thái thẩm định y khoa | PrimaryKey: `id = 1` |
| 14 | `test_result` | Kết quả que thử rụng trứng (LH) & que thử thai (hCG) | Index: `testDate` |
| 15 | `pregnancy_profile` | Hồ sơ thai kỳ, ngày dự sinh, số tuần tuổi | Index: `isActive` |
| 16 | `contraction_entry` | Đo cơn gò chuyển dạ (thời lượng, giãn cách, cường độ) | Foreign key: `pregnancy_profile` |
| 17 | `postpartum_profile` | Hồ sơ phục hồi sau sinh, ngày sinh, phương pháp sinh | Index: `isActive` |
| 18 | `feeding_entry` | Lịch sử cữ bú của bé (vú trái, vú phải, bình sữa) | Foreign key: `postpartum_profile` |
| 19 | `daily_habit` | Thói quen sống (uống nước, vận động, uống thuốc) | Index: `isActive` |
| 20 | `habit_log` | Bản ghi thực hiện thói quen theo ngày | Indices: `habitId`, `logDate` |
| 21 | `relaxation_session` | Lịch sử thực hiện bài tập thở (Box, 4-7-8) | Index: `startTime` |
| 22 | `medical_visit` | Sổ khám bệnh, ngày hẹn, câu hỏi bác sĩ, dặn dò | Index: `visitDate` |
| 23 | `medical_document` | Tệp đính kèm kết quả xét nghiệm, đơn thuốc | Foreign key: `medical_visit` (SET_NULL) |
| 24 | `medication_schedule` | Lịch uống thuốc, liều dùng, nhắc nhở | Index: `isActive` |
| 25 | `reminder` | Cấu hình báo thức/nhắc nhở nội bộ | Indices: `reminderType`, `isActive` |
| 26 | `knowledge_article` | Thư viện bài viết y khoa ngoại tuyến | Indices: `category`, `reviewStatus` |
| 27 | `bookmark` | Đánh dấu bài viết yêu thích | Index: `articleId` |
| 28 | `attachment` | Siêu dữ liệu tệp nhị phân mã hóa trong ứng dụng | Index: `linkedEntityId` |

---

## 2. Type Converters (Bộ chuyển đổi kiểu dữ liệu)
Toàn bộ kiểu dữ liệu thời gian hiện đại của Java 8+ được lưu trữ an toàn dưới dạng số nguyên epoch milli hoặc chuỗi chuẩn ISO:
- `LocalDate`: Chuyển đổi thành chuỗi `YYYY-MM-DD` để sắp xếp và truy vấn theo ngày cực nhanh.
- `LocalTime`: Chuyển đổi thành chuỗi `HH:mm:ss`.
- `Instant`: Chuyển đổi thành số nguyên `Long` (Epoch Milliseconds).

---

## 3. Quy Tắc Toàn Vẹn & Khôi Phục
- **Không bao giờ xóa mất lịch sử**: Khi người dùng tắt một phân hệ (ví dụ: tắt phân hệ Thai kỳ khi chuyển sang Sau sinh), dữ liệu lịch sử thai kỳ vẫn được giữ nguyên vẹn trong cơ sở dữ liệu và chỉ ẩn trên giao diện hiển thị.
- **Xóa liên hoàn (CASCADE) có kiểm soát**: Chỉ áp dụng cho các thành phần con trực thuộc bản ghi nhật ký trong ngày (`symptom_observation`, `mood_entry`, `sleep_entry`). Khi xóa một ngày nhật ký, các chi tiết của ngày đó sẽ được dọn dẹp sạch sẽ để tránh dữ liệu rác.
