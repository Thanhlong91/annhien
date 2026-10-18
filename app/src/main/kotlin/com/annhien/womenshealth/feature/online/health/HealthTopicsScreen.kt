package com.annhien.womenshealth.feature.online.health

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel

// ─── Data Models for Vietnamese Medical Handbook ─────────────────────────────

data class VietnameseHealthArticle(
    val id: String,
    val title: String,
    val category: String,
    val emoji: String,
    val readTime: String,
    val summary: String,
    val keyTakeaways: List<String>,
    val fullContent: String,
    val medicalSource: String
)

private val VIETNAMESE_ARTICLES = listOf(
    VietnameseHealthArticle(
        id = "vn_art_1",
        title = "Dinh dưỡng & Bổ sung Vi chất trong 3 Tháng Đầu Thai Kỳ",
        category = "Thai kỳ",
        emoji = "🤰",
        readTime = "4 phút đọc",
        summary = "3 tháng đầu là giai đoạn hình thành ống thần kinh và các cơ quan trọng yếu của thai nhi. Bổ sung đúng vi chất quyết định sự phát triển của bé.",
        keyTakeaways = listOf(
            "Axit Folic (B9): 400 - 600 mcg/ngày ngăn ngừa 70% dị tật ống thần kinh.",
            "Sắt: 30 - 60 mg/ngày phòng thiếu máu dinh dưỡng cho mẹ.",
            "Không tự ý uống Vitamin A liều cao vì có nguy cơ gây dị tật bẩm sinh.",
            "Mốc siêu âm quan trọng: 11 tuần 6 ngày - 13 tuần 6 ngày để đo độ mờ da gáy."
        ),
        fullContent = """
Trong tam cá nguyệt thứ nhất (tuần 1 đến tuần 13), thai nhi bắt đầu phân chia tế bào thần tốc và hình thành các cơ quan sơ khai: tim, não bộ, tủy sống và các chi.

1. Bổ sung Axit Folic (Vitamin B9):
Axit folic là vi chất quan trọng nhất. Nếu thiếu hụt, thai nhi có nguy cơ nứt đốt sống hoặc vô sọ. Khuyến nghị uống viên bổ sung 400 - 600 mcg mỗi ngày kết hợp ăn rau xanh thẫm (cải bó xôi, súp lơ), quả bơ và các loại đậu.

2. Bổ sung Sắt hữu cơ:
Thể tích máu của mẹ bầu tăng 40-50% trong thai kỳ. Bổ sung sắt giúp cung cấp oxy cho nhau thai. Lưu ý: Uống sắt cùng nước cam/chanh (Vitamin C) để tăng hấp thu; KHÔNG uống chung với sữa, trà hoặc cà phê (Canxi và Tanin ức chế hấp thu sắt).

3. Giảm nghén an toàn:
Chia nhỏ 5-6 bữa ăn trong ngày, uống trà gừng ấm vào buổi sáng, tránh để bụng quá đói hoặc quá no. Nếu nôn ói liên tục không giữ được nước, cần đến bệnh viện kiểm tra tránh mất điện giải.

⚠️ DẤU HIỆU CẦN ĐI KHÁM NGAY:
• Ra máu âm đạo (dù chỉ vài giọt nâu hoặc đỏ tươi).
• Đau thắt bụng dưới từng cơn dữ dội.
• Sốt cao trên 38.5°C hoặc nôn ói kiệt sức.
        """.trimIndent(),
        medicalSource = "Hướng dẫn Quốc gia về Chăm sóc Sức khỏe Sinh sản — Bộ Y Tế & ACOG"
    ),
    VietnameseHealthArticle(
        id = "vn_art_2",
        title = "Hội Chứng Buồng Trứng Đa Nang (PCOS) & Cách Điều Hòa Tự Nhiên",
        category = "Kinh nguyệt & PCOS",
        emoji = "🌸",
        readTime = "5 phút đọc",
        summary = "PCOS là rối loạn nội tiết phổ biến nhất ở phụ nữ trong độ tuổi sinh sản, gây kinh thưa, rụng tóc, mụn trứng cá và khó thụ thai.",
        keyTakeaways = listOf(
            "Căn nguyên cốt lõi là tình trạng Kháng Insulin và tăng Androgen (nội tiết nam).",
            "Chế độ ăn Low GI (chỉ số đường huyết thấp) là chìa khóa vàng cải thiện bệnh.",
            "Giảm 5-10% trọng lượng cơ thể có thể khôi phục lại chu kỳ rụng trứng tự nhiên.",
            "Bổ sung Myo-Inositol và Magie giúp tăng độ nhạy insulin buồng trứng."
        ),
        fullContent = """
Hội chứng buồng trứng đa nang (Polycystic Ovary Syndrome - PCOS) ảnh hưởng đến 1/10 phụ nữ. Nang trứng không thể chín và phóng noãn đúng chu kỳ, khiến buồng trứng xuất hiện nhiều nang nhỏ li ti trên siêu âm.

1. Nhận biết các dấu hiệu điển hình:
• Chu kỳ kinh kéo dài trên 35 ngày (kinh thưa) hoặc mất kinh nhiều tháng.
• Da nhiều dầu, mụn bọc vùng quai hàm, rậm lông vùng mép/bụng hoặc tóc mỏng rụng đỉnh đầu.
• Khó kiểm soát cân nặng, tích mỡ nhiều ở vùng bụng dưới.

2. Phác đồ dinh dưỡng cải thiện PCOS:
• Hạn chế tối đa đường tinh luyện, trà sữa, bánh ngọt và tinh bột trắng.
• Ưu tiên tinh bột chậm: gạo lứt, yến mạch, khoai lang, các loại đậu.
• Tăng cường chất béo tốt (Omega-3 từ cá hồi, hạt chia, dầu ô liu) để kháng viêm buồng trứng.
• Tập luyện sức bền nhẹ nhàng (Pilates, Yoga, đi bộ nhanh 30 phút mỗi ngày) để tăng độ nhạy insulin.

3. Điều hòa kinh nguyệt:
Theo dõi ngày kinh đều đặn trên ứng dụng An Nhiên để nắm bắt nhịp điệu cơ thể và chia sẻ nhật ký chu kỳ cùng bác sĩ chuyên khoa sản phụ khoa.
        """.trimIndent(),
        medicalSource = "Hiệp hội Nội tiết Lâm sàng Hoa Kỳ (AACE) & Viện Dinh Dưỡng Quốc Gia"
    ),
    VietnameseHealthArticle(
        id = "vn_art_3",
        title = "Tầm Soát Ung Thư Cổ Tử Cung & Tự Khám Vú Tại Nhà",
        category = "Phụ khoa & Tầm soát",
        emoji = "🩺",
        readTime = "4 phút đọc",
        summary = "Ung thư cổ tử cung và ung thư vú hoàn toàn có thể chữa khỏi trên 95% nếu được phát hiện ở giai đoạn sớm qua tầm soát định kỳ.",
        keyTakeaways = listOf(
            "99.7% ung thư cổ tử cung bắt nguồn từ virus HPV lây truyền qua đường tình dục.",
            "Xét nghiệm Pap smear định kỳ mỗi 2-3 năm; xét nghiệm HPV DNA mỗi 5 năm.",
            "Tự khám vú tại nhà đều đặn vào ngày thứ 3 đến thứ 5 sau khi sạch kinh.",
            "Tiêm vắc xin HPV càng sớm càng tốt (tốt nhất từ 9 - 26 tuổi, mở rộng đến 45 tuổi)."
        ),
        fullContent = """
Chủ động phòng ngừa và tầm soát là món quà bảo vệ sức khỏe quý giá nhất mà mỗi người phụ nữ có thể tự dành cho chính mình.

1. Tầm soát Ung thư Cổ tử cung:
• Pap smear (phết tế bào cổ tử cung): Giúp phát hiện sớm các tế bào loạn sản tiền ung thư trước khi chúng tiến triển thành ác tính.
• HPV DNA test: Tìm sự hiện diện của các chủng HPV nguy cơ cao (đặc biệt là chủng 16 và 18).
• Khuyến cáo: Phụ nữ từ 21 tuổi đã có quan hệ tình dục nên khám phụ khoa và làm xét nghiệm tầm soát định kỳ.

2. Hướng dẫn Tự Khám Vú Tại Nhà (Mỗi tháng 1 lần):
• Thời điểm vàng: Ngày 3 - 5 sau khi sạch kinh nguyệt (lúc mô tuyến vú mềm nhất, không bị cương cứng do hormone).
• Bước 1: Đứng trước gương, quan sát hình dạng, màu sắc da vú và núm vú xem có bị co rút, lõm hay chảy dịch bất thường không.
• Bước 2: Nâng một cánh tay lên, dùng 3 ngón tay khép của tay kia xoa nắn theo vòng tròn xoắn ốc từ ngoài vào trong núm vú và kiểm tra hố nách.
• Bước 3: Lặp lại tư thế khi nằm ngửa kê gối mỏng dưới vai.

⚠️ DẤU HIỆU CẢNH BÁO CẦN ĐI KHÁM CHUYÊN KHOA NGAY:
• Sờ thấy cục cứng, không đau ở vú hoặc nách.
• Núm vú bị tụt vào trong hoặc tiết dịch mủ/máu bất thường.
• Da vú sần sùi như vỏ quả cam hoặc đỏ rát cục bộ.
        """.trimIndent(),
        medicalSource = "Tổ chức Y tế Thế giới (WHO) & Bệnh viện Ung bướu Trung ương"
    ),
    VietnameseHealthArticle(
        id = "vn_art_4",
        title = "Phục Hồi Sàn Chậu Sau Sinh & Kỹ Thuật Kegel Đúng Chuẩn",
        category = "Sàn chậu Kegel",
        emoji = "🧘‍♀️",
        readTime = "3 phút đọc",
        summary = "Quá trình mang thai và sinh nở khiến cơ sàn chậu bị giãn căng, dễ dẫn đến són tiểu khi cười/ho và sa tử cung nếu không được tập luyện.",
        keyTakeaways = listOf(
            "Cơ sàn chậu nâng đỡ toàn bộ tử cung, bàng quang và trực tràng.",
            "Tập Kegel đúng: Thắt chặt cơ như đang nhịn tiểu, không siết bụng, đùi hay mông.",
            "Tập 3 hiệp mỗi ngày (mỗi hiệp 10 lần co - giữ 5 giây - thả lỏng 5 giây).",
            "Có thể bắt đầu sau sinh thường 2-3 ngày hoặc sau sinh mổ khi vết mổ đã khô lành."
        ),
        fullContent = """
Cơ sàn chậu giống như một chiếc võng cơ bắp nâng đỡ các cơ quan nội tạng vùng chậu. Dù sinh thường hay sinh mổ, áp lực trọng lượng thai nhi trong 9 tháng đều làm suy yếu hệ thống cơ này.

1. Lợi ích khi tập phục hồi sàn chậu:
• Chấm dứt triệt để tình trạng són tiểu phiền toái khi hắt hơi, ho hoặc chạy bộ.
• Ngăn ngừa sa sinh dục (sa thành âm đạo, sa tử cung) khi bước vào tuổi trung niên.
• Tăng tuần hoàn máu vùng đáy chậu, giúp vết rạch tầng sinh môn mau lành.
• Cải thiện chất lượng và cảm giác trong đời sống vợ chồng.

2. Kỹ thuật co cơ sàn chậu (Kegel) chuẩn y khoa:
• Tìm đúng nhóm cơ: Hãy tưởng tượng bạn đang đi tiểu và muốn chủ động ngừng dòng nước tiểu lại giữa chừng. Nhóm cơ đang co thắt đó chính là cơ sàn chậu.
• Quy tắc hít thở: Hít sâu thả lỏng -> Thở ra nhẹ nhàng đồng thời co siết cơ sàn chậu kéo lên trên.
• Giữ cơ siết chặt trong 3-5 giây, sau đó thả lỏng hoàn toàn trong 5 giây. Lặp lại 10 lần.
• Tuyệt đối KHÔNG gồng cơ bụng, không bóp chặt hai bên mông hay cơ đùi trong khi tập.

Ứng dụng An Nhiên đã tích hợp sẵn công cụ bấm giờ nhịp thở Kegel tại mục "Tiện ích -> Bài tập Kegel", giúp bạn tập đúng nhịp mỗi ngày.
        """.trimIndent(),
        medicalSource = "Hội Sản Phụ khoa Việt Nam (VAGO) & Trường Đại học Y Dược TP.HCM"
    ),
    VietnameseHealthArticle(
        id = "vn_art_5",
        title = "Thiếu Máu Thiếu Sắt: Nhận Biết Da Xanh Xao, Rụng Tóc Ở Nữ Giới",
        category = "Dinh dưỡng",
        emoji = "🩸",
        readTime = "4 phút đọc",
        summary = "Mất máu kinh nguyệt hàng tháng kết hợp ăn uống kiêng khem khiến phụ nữ là nhóm đối tượng có tỷ lệ thiếu máu thiếu sắt cao nhất cộng đồng.",
        keyTakeaways = listOf(
            "Các triệu chứng: Mệt mỏi triền miên, hoa mắt khi đứng dậy, tim đập nhanh, rụng tóc nhiều.",
            "Xét nghiệm cần làm: Công thức máu (Hemoglobin) và Ferritin (dự trữ sắt).",
            "Thịt bò nạc (2.7mg Fe), nghêu sò, lòng đỏ trứng, cải bina là nguồn thực phẩm giàu sắt.",
            "Uống sắt chung với Vitamin C để nhân đôi hiệu suất hấp thụ vào tủy xương."
        ),
        fullContent = """
Sắt là thành phần cấu tạo nên Hemoglobin trong hồng cầu, có nhiệm vụ vận chuyển oxy từ phổi đến từng tế bào trong cơ thể. Khi thiếu sắt, cơ thể thiếu oxy dẫn đến trạng thái kiệt sức mãn tính.

1. Bạn có đang bị thiếu máu thiếu sắt?
• Thường xuyên chóng mặt, choáng váng khi thay đổi tư thế đột ngột.
• Da dẻ xanh xao, niêm mạc mắt và lòng bàn tay nhợt nhạt.
• Móng tay mỏng, dễ gãy khuyết hình thìa, tóc khô rụng nhiều.
• Hay hồi hộp, thở dốc khi leo cầu thang dù trước đây không bị.
• Cảm giác thèm ăn những thứ kỳ lạ (đá lạnh, gạo sống) - hội chứng Pica.

2. Chiến lược bổ sung sắt thông minh:
• Nguồn sắt Heme (hấp thu cao 20-30%): Thịt bò thăn, thịt gà tây, gan động vật sạch, cá biển, hàu biển.
• Nguồn sắt Non-Heme (hấp thu 5-10%): Đậu lăng, mộc nhĩ, cải bó xôi, hạt bí ngô, hạt chia.
• Bí quyết: Luôn vắt chanh hoặc ăn cam, ổi cùng bữa ăn để Vitamin C chuyển sắt Fe3+ thành Fe2+ dễ hấp thụ.
• Khoảng cách an toàn: Tránh uống trà xanh, trà đen, cà phê và sữa trong vòng 2 giờ trước và sau khi bổ sung sắt vì chất Tanin và Canxi sẽ kết tủa sắt.
        """.trimIndent(),
        medicalSource = "Viện Huyết học - Truyền máu Trung ương & WHO Guidelines"
    ),
    VietnameseHealthArticle(
        id = "vn_art_6",
        title = "Giai Đoạn Tiền Mãn Kinh: Bốc Hỏa, Mất Ngủ & Liệu Pháp Tự Nhiên",
        category = "Tiền mãn kinh",
        emoji = "🌅",
        readTime = "4 phút đọc",
        summary = "Từ sau tuổi 40, buồng trứng bắt đầu giảm sản xuất Estrogen, kéo theo hàng loạt biến đổi sinh học ảnh hưởng đến tâm sinh lý phụ nữ.",
        keyTakeaways = listOf(
            "Cơn bốc hỏa (Hot flashes): Cảm giác nóng bừng đột ngột từ ngực lên mặt kéo dài 2-5 phút.",
            "Dao động hormone gây mất ngủ, đổ mồ hôi đêm, lo âu và giảm mật độ xương.",
            "Bổ sung Phytoestrogen từ mầm đậu nành, hạt lanh và cỏ ba lá đỏ tự nhiên.",
            "Tập thể dục chịu lực và bổ sung Canxi + Vitamin D3 để chống loãng xương."
        ),
        fullContent = """
Tiền mãn kinh là một bước chuyển tự nhiên của vòng đời người phụ nữ, không phải là bệnh lý. Hiểu rõ nguyên nhân giúp chị em đón nhận giai đoạn này với tâm thế nhẹ nhàng và chủ động.

1. Các biểu hiện đặc trưng:
• Rối loạn kinh nguyệt: Chu kỳ lúc ngắn (21 ngày), lúc dài (40-60 ngày), lượng máu kinh thất thường.
• Cơn bốc hỏa và đổ mồ hôi đêm: Đột ngột nóng bừng vùng mặt cổ, vã mồ hôi kèm tim đập nhanh, sau đó thấy ớn lạnh.
• Thay đổi tâm trạng: Dễ cáu gắt, giảm khả năng tập trung, hay quên tạm thời.
• Khô hạn âm đạo, giảm ham muốn và dễ bị viêm đường tiết niệu tái phát.

2. Các giải pháp tự nhiên và an toàn:
• Dinh dưỡng giàu Phytoestrogen: Sữa đậu nành nguyên chất, đậu phụ, hạt lanh nghiền, quả óc chó giúp cân bằng thụ thể estrogen nhẹ nhàng.
• Giữ phòng ngủ thoáng mát: Mặc quần áo lụa hoặc cotton mỏng thoáng khí, tắm nước ấm trước khi ngủ.
• Bảo vệ xương khớp: Bổ sung 1000 - 1200mg Canxi nguyên tố mỗi ngày kết hợp tắm nắng sáng để hấp thu Vitamin D.
• Thực hành chánh niệm & Thở sâu: Dành 15 phút mỗi ngày thiền định hoặc nghe nhạc sóng não thư giãn trong ứng dụng An Nhiên.
        """.trimIndent(),
        medicalSource = "Hội Mãn kinh Bắc Mỹ (NAMS) & Bệnh viện Từ Dũ"
    ),
    VietnameseHealthArticle(
        id = "vn_art_7",
        title = "Vệ Sinh Phụ Khoa Đúng Cách & Giữ Cân Bằng Hệ Vi Sinh Âm Đạo",
        category = "Phụ khoa",
        emoji = "🌿",
        readTime = "3 phút đọc",
        summary = "Âm đạo là một cơ quan có khả năng tự làm sạch tuyệt vời nhờ hàng tỷ lợi khuẩn Lactobacillus duy trì môi trường pH axit nhẹ 3.8 - 4.5.",
        keyTakeaways = listOf(
            "Tuyệt đối KHÔNG thụt rửa sâu bên trong âm đạo vì sẽ tiêu diệt lợi khuẩn bảo vệ.",
            "Chỉ dùng dung dịch vệ sinh dịu nhẹ có độ pH 3.8 - 4.5 rửa vùng da bên ngoài.",
            "Luôn lau từ trước ra sau sau khi đi vệ sinh để tránh đưa vi khuẩn từ hậu môn lên.",
            "Chọn đồ lót cotton thoáng khí, thay băng vệ sinh mỗi 3-4 giờ trong ngày đèn đỏ."
        ),
        fullContent = """
Nhiều chị em vì quá lo lắng về mùi tự nhiên mà lạm dụng các sản phẩm xịt thơm hoặc thụt rửa sâu, vô tình phá vỡ hàng rào bảo vệ sinh học tự nhiên của cơ thể.

1. Hệ vi sinh âm đạo hoạt động thế nào?
Vi khuẩn có lợi Lactobacillus chiếm 90% quần thể vi sinh vật vùng kín. Chúng chuyển hóa glycogen thành Axit Lactic, tạo nên môi trường có tính axit (pH 3.8 - 4.5). Độ axit này là vũ khí tối thượng ức chế các loại vi nấm Candida và vi khuẩn có hại phát triển.

2. Những sai lầm tai hại thường gặp:
• Thụt rửa âm đạo bằng vòi xịt: Nước đẩy vi khuẩn sâu vào cổ tử cung, gây viêm vùng chậu và ứ dịch vòi trứng.
• Dùng xà phòng tắm, sữa tắm có chất tẩy rửa mạnh để vệ sinh vùng kín: Gây khô ráp và kiềm hóa pH.
• Mặc quần lót bó sát làm bằng sợi tổng hợp: Gây bí bách, tăng nhiệt độ và độ ẩm, tạo ổ sinh sản cho nấm men.

3. Nhận diện khí hư bất thường:
• Bình thường: Khí hư trong suốt như lòng trắng trứng hoặc trắng sữa, không mùi hôi khó chịu.
• Bệnh lý nấm Candida: Khí hư trắng đục vón cục như bã đậu, ngứa rát âm hộ dữ dội.
• Nhiễm trùng roi Trichomonas: Khí hư màu vàng xanh, có bọt khí kèm mùi tanh nồng.
• Viêm âm đạo do vi khuẩn (BV): Khí hư loãng xám, mùi hôi tanh tanh rõ rệt sau khi quan hệ.
        """.trimIndent(),
        medicalSource = "Hướng dẫn Y tế Dự phòng — Trung tâm Kiểm soát Bệnh tật Hoa Kỳ (CDC) & Bộ Y Tế"
    )
)

// ─── Main Composable ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthTopicsScreen(
    onNavigateBack: () -> Unit,
    viewModel: HealthTopicsViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Tiếng Việt (Cẩm nang An Nhiên), 1: Quốc tế (MyHealthfinder)
    var selectedCategory by remember { mutableStateOf("Tất cả") }
    var viewingArticle by remember { mutableStateOf<VietnameseHealthArticle?>(null) }
    val categories = listOf("Tất cả", "Thai kỳ", "Kinh nguyệt & PCOS", "Phụ khoa", "Sàn chậu Kegel", "Dinh dưỡng", "Tiền mãn kinh")

    val uiState by viewModel.uiState.collectAsState()
    val primaryColor = Color(0xFF3D684E)

    val filteredArticles = remember(selectedCategory) {
        if (selectedCategory == "Tất cả") VIETNAMESE_ARTICLES
        else VIETNAMESE_ARTICLES.filter { it.category.contains(selectedCategory) || selectedCategory.contains(it.category) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đề tài sức khỏe phụ nữ", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .imePadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Dual Tab Switcher: Tiếng Việt vs Quốc Tế
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = primaryColor,
                modifier = Modifier.clip(RoundedCornerShape(16.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🇻🇳 Cẩm nang Y khoa", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌐 Nguồn Quốc tế (US)", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                        }
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            // TAB 0: VIETNAMESE MEDICAL HANDBOOK
            if (selectedTab == 0) {
                // Category filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val active = selectedCategory == cat
                        FilterChip(
                            selected = active,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor.copy(alpha = 0.15f),
                                selectedLabelColor = primaryColor
                            )
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredArticles, key = { it.id }) { article ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewingArticle = article },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(article.emoji, fontSize = 24.sp)
                                        Spacer(Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = primaryColor.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                article.category,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = primaryColor
                                            )
                                        }
                                    }
                                    Text(
                                        article.readTime,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(Modifier.height(10.dp))

                                Text(
                                    text = article.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = article.summary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 18.sp
                                )

                                Spacer(Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "✓ Y khoa kiểm chứng",
                                        fontSize = 11.sp,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Đọc toàn văn", fontSize = 12.sp, color = primaryColor, fontWeight = FontWeight.Bold)
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // TAB 1: US MYHEALTHFINDER API FEED
                when (val state = uiState) {
                    HealthTopicsUiState.Loading -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = primaryColor)
                            Spacer(Modifier.height(12.dp))
                            Text("Đang tải dữ liệu MyHealthfinder API...", fontSize = 13.sp)
                        }
                    }

                    is HealthTopicsUiState.Success -> {
                        if (state.topics.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Hiện chưa có chủ đề sức khỏe từ MyHealthfinder.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text("ℹ️", fontSize = 18.sp)
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "Dữ liệu trực tiếp từ Bộ Y tế & Dịch vụ Nhân sinh Hoa Kỳ (ODPHP / health.gov). Có thể nhấp vào để xem chi tiết.",
                                                fontSize = 12.sp,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }

                                items(state.topics, key = { it.id ?: it.title ?: "" }) { topic ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewingArticle = VietnameseHealthArticle(
                                                    id = topic.id ?: "myhealthfinder",
                                                    title = topic.title ?: "Chủ đề sức khỏe MyHealthfinder",
                                                    category = "Quốc tế (US)",
                                                    emoji = "🇺🇸",
                                                    readTime = "3 phút đọc",
                                                    summary = topic.summary ?: "Chuyên đề hướng dẫn sức khỏe từ Viện Y tế Quốc gia Hoa Kỳ.",
                                                    keyTakeaways = listOf(
                                                        "Cung cấp bởi: U.S. Department of Health and Human Services (ODPHP).",
                                                        "Nguồn truy cập tiêu chuẩn: MyHealthfinder API v4.",
                                                        "Liên kết tham khảo: ${topic.url ?: "health.gov"}"
                                                    ),
                                                    fullContent = """
Tiêu đề: ${topic.title}

Phân loại y khoa: ${topic.summary ?: "Tổng quan sức khỏe phụ nữ"}

Hướng dẫn chi tiết từ MyHealthfinder:
Các chủ đề MyHealthfinder được biên soạn dựa trên các khuyến nghị lâm sàng của U.S. Preventive Services Task Force (USPSTF). Bạn có thể tham vấn các chỉ dẫn này để đối chiếu với bác sĩ điều trị.

Để đọc bản đầy đủ trên trang chủ y tế Hoa Kỳ, vui lòng truy cập đường dẫn chính thức:
${topic.url ?: "https://health.gov/myhealthfinder"}
                                                    """.trimIndent(),
                                                    medicalSource = "U.S. ODPHP MyHealthfinder (health.gov)"
                                                )
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    ) {
                                        Column(Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("🇺🇸 MyHealthfinder v4", fontSize = 11.sp, color = primaryColor, fontWeight = FontWeight.Bold)
                                                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = primaryColor, modifier = Modifier.size(14.dp))
                                            }
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                text = topic.title ?: "Chủ đề sức khỏe",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            topic.summary?.let {
                                                Spacer(Modifier.height(4.dp))
                                                Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is HealthTopicsUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = viewModel::load, colors = ButtonDefaults.buttonColors(containerColor = primaryColor)) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
            }
        }
    }

    // ─── Full Article Reader Dialog ──────────────────────────────────────────
    viewingArticle?.let { article ->
        Dialog(
            onDismissRequest = { viewingArticle = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = RoundedCornerShape(12.dp), color = primaryColor.copy(alpha = 0.12f)) {
                            Text(
                                "${article.emoji} ${article.category}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                        IconButton(onClick = { viewingArticle = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Đóng")
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Scrollable article content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 28.sp
                        )

                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(4.dp))
                            Text(article.readTime, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(12.dp))
                            Text("•  ✓ Đã kiểm chứng y khoa", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                        }

                        Spacer(Modifier.height(14.dp))

                        // Key Takeaways Box
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text("💡 Điểm mấu chốt cần nhớ:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = primaryColor)
                                Spacer(Modifier.height(8.dp))
                                article.keyTakeaways.forEach { point ->
                                    Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                                        Text("• ", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 13.sp)
                                        Text(point, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Full Formatted Content
                        Text(
                            text = article.fullContent,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )

                        Spacer(Modifier.height(20.dp))

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(12.dp))

                        // Medical Source Citation
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text("Nguồn tham chiếu y khoa:", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = primaryColor)
                                Text(article.medicalSource, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    }

                    Button(
                        onClick = { viewingArticle = null },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Đã hiểu bài viết")
                    }
                }
            }
        }
    }
}
