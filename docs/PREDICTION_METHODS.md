# Phương Pháp Tính Toán & Dự Đoán Sinh Học (Prediction Methods)

Tài liệu này mô tả chi tiết nền tảng thuật toán, công thức toán học và các giới hạn y khoa được áp dụng trong ứng dụng An Nhiên.

---

## 1. Động Cơ Dự Đoán Chu Kỳ Kinh Nguyệt (CyclePredictionEngine)

### 1.1. Nguyên Lý Tính Toán
1. **Trích xuất các chu kỳ hoàn chỉnh**:
   - Một chu kỳ hợp lệ được tính từ ngày bắt đầu của một kỳ kinh nguyệt ($StartDate_i$) đến ngày ngay trước ngày bắt đầu của kỳ kinh nguyệt kế tiếp ($StartDate_{i+1} - 1\text{ ngày}$).
   - Độ dài chu kỳ: $CycleLength_i = StartDate_{i+1} - StartDate_i$.
   - Độ dài kỳ hành kinh: $PeriodLength_i = EndDate_i - StartDate_i + 1$.
2. **Tiêu chuẩn dữ liệu hợp lệ**:
   - Loại bỏ các kỳ bị đánh dấu là "dữ liệu không chắc chắn" (`isUncertain = true`) hoặc "kỳ bị bỏ sót" (`isMissedMarker = true`).
   - Yêu cầu tối thiểu: Phải có **ít nhất 3 chu kỳ hoàn chỉnh hợp lệ** mới đủ điều kiện thực hiện tính toán dự đoán. Nếu chưa đủ 3 chu kỳ, thuật toán sẽ trả về trạng thái `Ineligible.INSUFFICIENT_HISTORY`.
3. **Sử dụng số Trung Vị (Median) thay vì Trung Bình Cộng (Mean)**:
   - Trong sinh học phụ nữ, chu kỳ kinh nguyệt rất dễ bị nhiễu do stress, ốm sốt hoặc thay đổi múi giờ, tạo ra các giá trị ngoại lai (outliers) như chu kỳ 45 ngày hoặc 18 ngày.
   - Số trung vị giúp triệt tiêu hoàn toàn tác động của các giá trị ngoại lai, mang lại độ chính xác thực tế cao hơn nhiều so với trung bình cộng số học thông thường.
4. **Khoảng tin cậy (Confidence Interval)**:
   - Dựa trên độ lệch tứ phân vị (Interquartile Range - IQR) hoặc nửa khoảng biến thiên giữa chu kỳ dài nhất và ngắn nhất trong lịch sử:
     $$ConfidenceIntervalDays = \max\left(2, \min\left(5, \left\lceil \frac{MaxCycle - MinCycle}{2} \right\rceil\right)\right)$$
5. **Dự đoán kỳ tiếp theo**:
   - Ngày bắt đầu dự kiến: $ExpectedStartDate = LastStartDate + MedianCycleLength$.
   - Ngày kết thúc dự kiến: $ExpectedEndDate = ExpectedStartDate + MedianPeriodLength - 1$.

---

## 2. Ước Tính Khả Năng Thụ Thai (FertilityEstimateEngine)

### 2.1. Giới Hạn Nghiêm Ngặt & Khóa Chuyên Môn
- Tính năng ước tính cửa sổ thụ thai **mặc định bị khóa ở trạng thái `PENDING_METHOD_APPROVAL`** (đang chờ phê chuẩn lâm sàng độc lập).
- Khi chưa có sự phê chuẩn chuyên môn, ứng dụng chỉ cung cấp công cụ tự ghi nhận thông số thực tế (nhiệt độ cơ thể nền BBT, kết quả que thử LH, quan sát chất nhầy CTC) và **không hiển thị bất kỳ ngày dự đoán rụng trứng tự động nào**.

### 2.2. Quy Tắc Không "Ngày An Toàn" (No Safe Days Prohibition)
- **Quy tắc tuyệt đối**: Ứng dụng **không bao giờ gắn nhãn bất kỳ ngày nào trong chu kỳ là "ngày an toàn"**.
- Lý do y khoa: Khả năng sống sót của tinh trùng trong đường sinh dục nữ có thể kéo dài tới 5 ngày; thời điểm phóng noãn có thể dịch chuyển bất ngờ mà không có dấu hiệu báo trước. Việc gắn nhãn "ngày an toàn" trên phần mềm là nguyên nhân hàng đầu dẫn đến việc mang thai ngoài ý muốn khi áp dụng các biện pháp tránh thai tự nhiên sai cách.

---

## 3. Quản Lý Ngày Nhuận & Múi Giờ
- Toàn bộ phép toán ngày tháng sử dụng chuẩn `java.time.LocalDate` độc lập với múi giờ, xử lý chính xác năm nhuận (ngày 29 tháng 2) và thời điểm chuyển giao giữa các tháng có 28, 29, 30, hoặc 31 ngày.
