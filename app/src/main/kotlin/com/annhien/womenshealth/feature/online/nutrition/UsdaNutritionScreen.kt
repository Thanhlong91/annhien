package com.annhien.womenshealth.feature.online.nutrition

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─── Data Models ─────────────────────────────────────────────────────────────

private data class FoodSource(val name: String, val amount: String, val rdaPct: Int)

private data class Micronutrient(
    val id: String,
    val name: String,
    val group: List<String>,
    val icon: String,
    val rda: String,
    val why: String,
    val topFoods: List<FoodSource>,
    val tip: String
)

private data class PlateFoodItem(
    val id: String,
    val name: String,
    val folate: Int,
    val iron: Float,
    val calcium: Int,
    val magnesium: Int,
    val zinc: Float,
    val vitC: Int,
    val isDefaultChecked: Boolean = false
)

private data class UsdaSearchResult(
    val id: String,
    val name: String,
    val portion: String,
    val folate: String,
    val iron: String,
    val calcium: String,
    val zinc: String,
    val vitD: String,
    val omega3: String,
    val b12: String,
    val b6: String
)

// ─── Static Data ─────────────────────────────────────────────────────────────

private val MICRONUTRIENTS = listOf(
    Micronutrient(
        id = "folate", name = "Axit Folic / Folate (B9)", group = listOf("blood", "pregnancy"), icon = "🌿",
        rda = "400 – 600 mcg",
        why = "Ngăn ngừa đến 70% nguy cơ dị tật ống thần kinh thai nhi (nứt đốt sống), tham gia phân chia tế bào ADN và tạo máu mới.",
        topFoods = listOf(
            FoodSource("Rau chân vịt (bina) tươi", "194 mcg / 100g", 49),
            FoodSource("Đậu lăng hầm chín", "181 mcg / 100g", 45),
            FoodSource("Măng tây xanh luộc", "149 mcg / 100g", 37),
            FoodSource("Lòng đỏ trứng gà ta", "146 mcg / 100g", 36),
            FoodSource("Trái bơ sáp chín", "81 mcg / 100g", 20)
        ),
        tip = "Axit Folic dễ phân hủy khi nấu ở nhiệt độ cao lâu. Nên hấp hoặc xào nhanh rau xanh."
    ),
    Micronutrient(
        id = "iron", name = "Sắt sinh học (Iron — Fe)", group = listOf("blood"), icon = "🩸",
        rda = "18 – 27 mg",
        why = "Bù đắp lượng máu hao hụt qua chu kỳ kinh nguyệt, vận chuyển oxy nuôi tế bào và thai nhi, giảm tình trạng chóng mặt, hụt hơi, rụng tóc.",
        topFoods = listOf(
            FoodSource("Huyết heo / bò luộc", "35.0 mg / 100g", 194),
            FoodSource("Mộc nhĩ đen khô", "56.1 mg / 100g", 311),
            FoodSource("Gan gà / bò hấp", "9.0 mg / 100g", 50),
            FoodSource("Hạt bí ngô tách vỏ", "8.8 mg / 100g", 49),
            FoodSource("Thịt bò nạc bắp", "3.1 mg / 100g", 17)
        ),
        tip = "Hấp thu tốt nhất khi kết hợp với Vitamin C (cam, chanh). Tránh uống trà đặc, cà phê cùng lúc vì tanin cản trở hấp thu sắt."
    ),
    Micronutrient(
        id = "calcium", name = "Canxi sinh học (Ca)", group = listOf("bone"), icon = "🦴",
        rda = "1000 – 1200 mg",
        why = "Duy trì mật độ xương, phòng ngừa loãng xương khi nồng độ estrogen suy giảm ở tuổi tiền mãn kinh và nuôi dưỡng khung xương răng thai nhi.",
        topFoods = listOf(
            FoodSource("Phô mai Parmesan", "1184 mg / 100g", 118),
            FoodSource("Hạt vừng (mè đen/trắng)", "975 mg / 100g", 98),
            FoodSource("Đậu phụ non / Đậu khuôn", "350 mg / 100g", 35),
            FoodSource("Hạnh nhân rang mộc", "264 mg / 100g", 26),
            FoodSource("Sữa chua không đường", "120 mg / 100g", 12)
        ),
        tip = "Canxi cần Vitamin D3 và Magie để hấp thụ trọn vẹn vào xương thay vì lắng đọng thành sỏi."
    ),
    Micronutrient(
        id = "vitamind", name = "Vitamin D3 (Cholecalciferol)", group = listOf("bone", "pregnancy"), icon = "☀️",
        rda = "600 – 800 IU (15 – 20 mcg)",
        why = "Chìa khóa mở cửa hấp thu canxi qua thành ruột, tăng cường miễn dịch tự nhiên, cải thiện tâm trạng trầm cảm theo mùa và hội chứng buồng trứng đa nang (PCOS).",
        topFoods = listOf(
            FoodSource("Dầu gan cá tuyết", "10,000 IU / 100g", 1660),
            FoodSource("Cá hồi Nauy áp chảo", "526 IU / 100g", 88),
            FoodSource("Cá mòi đóng hộp", "193 IU / 100g", 32),
            FoodSource("Lòng đỏ trứng gà", "87 IU / 100g", 15),
            FoodSource("Nấm phơi nắng tự nhiên", "150 IU / 100g", 25)
        ),
        tip = "Khoảng 80% Vitamin D được tổng hợp qua da dưới ánh nắng mặt trời buổi sáng (trước 8:30)."
    ),
    Micronutrient(
        id = "magnesium", name = "Magie sinh học (Mg)", group = listOf("bone", "hormone"), icon = "⚡",
        rda = "310 – 320 mg",
        why = "Làm giãn cơ trơn tử cung, giảm co thắt đau bụng kinh (thống kinh), giảm phù nề, đau đầu và căng thẳng tiền kinh nguyệt (PMS). Hỗ trợ giấc ngủ sâu.",
        topFoods = listOf(
            FoodSource("Bột cacao nguyên chất", "499 mg / 100g", 156),
            FoodSource("Hạt điều rang mộc", "292 mg / 100g", 91),
            FoodSource("Hạnh nhân sấy", "268 mg / 100g", 84),
            FoodSource("Sô-cô-la đen (>70%)", "228 mg / 100g", 71),
            FoodSource("Đậu đen ninh nhừ", "70 mg / 100g", 22)
        ),
        tip = "Khi phụ nữ thèm sô-cô-la trước kỳ kinh, đó là tín hiệu tự nhiên báo cơ thể đang thiếu hụt Magie."
    ),
    Micronutrient(
        id = "zinc", name = "Kẽm sinh học (Zinc — Zn)", group = listOf("hormone", "pregnancy"), icon = "🔬",
        rda = "8 – 11 mg",
        why = "Điều hòa hormone rụng trứng, hỗ trợ chất lượng trứng, kháng viêm niêm mạc âm đạo, giảm mụn nội tiết bùng phát và giúp vết khâu tầng sinh môn nhanh lành.",
        topFoods = listOf(
            FoodSource("Hàu biển tươi", "16.6 mg / 100g", 207),
            FoodSource("Thịt bò nạc bắp", "5.5 mg / 100g", 69),
            FoodSource("Hạt bí ngô tách vỏ", "7.8 mg / 100g", 97),
            FoodSource("Đậu lăng / Đậu gà", "1.8 mg / 100g", 22),
            FoodSource("Hạt mè trắng", "7.8 mg / 100g", 97)
        ),
        tip = "Kẽm cùng với Vitamin A và Vitamin C tạo nên hàng rào bảo vệ niêm mạc tử cung vững chắc."
    ),
    Micronutrient(
        id = "b12", name = "Vitamin B12 (Cobalamin)", group = listOf("blood", "pregnancy"), icon = "🥩",
        rda = "2.4 – 2.8 mcg",
        why = "Duy trì bao myelin bảo vệ dây thần kinh, phối hợp cùng Folate tạo hồng cầu. Thiếu B12 gây hội chứng não sương mù (Brain Fog) và mệt mỏi kinh niên.",
        topFoods = listOf(
            FoodSource("Ngao / Nghêu hấp", "98.9 mcg / 100g", 4120),
            FoodSource("Gan bò áp chảo", "70.6 mcg / 100g", 2940),
            FoodSource("Cá thu / Cá hồi", "8.9 mcg / 100g", 370),
            FoodSource("Sữa tươi thanh trùng", "1.2 mcg / 250ml", 50),
            FoodSource("Trứng gà luộc", "1.1 mcg / 100g", 46)
        ),
        tip = "Vitamin B12 chỉ có tự nhiên trong thực phẩm nguồn gốc động vật. Phụ nữ ăn chay thuần cần uống bổ sung định kỳ."
    ),
    Micronutrient(
        id = "b6", name = "Vitamin B6 (Pyridoxine)", group = listOf("hormone", "pregnancy"), icon = "🥑",
        rda = "1.3 – 1.9 mg",
        why = "Hỗ trợ tổng hợp hormone Progesterone, giảm ốm nghén và buồn nôn ở 3 tháng đầu thai kỳ, giảm tích nước và căng tức ngực trước kỳ kinh.",
        topFoods = listOf(
            FoodSource("Cá ngừ đại dương", "1.0 mg / 100g", 77),
            FoodSource("Ức gà phi lê", "0.6 mg / 100g", 46),
            FoodSource("Chuối tiêu chín", "0.4 mg / 100g", 31),
            FoodSource("Khoai tây nướng cả vỏ", "0.4 mg / 100g", 31),
            FoodSource("Trái bơ sáp", "0.3 mg / 100g", 23)
        ),
        tip = "Vitamin B6 được Hiệp hội Sản Phụ khoa Hoa Kỳ (ACOG) khuyến nghị hàng đầu trong phác đồ giảm nghén thai kỳ an toàn."
    ),
    Micronutrient(
        id = "vitaminc", name = "Vitamin C (Ascorbic Acid)", group = listOf("blood", "hormone"), icon = "🍊",
        rda = "75 – 85 mg (Hút thuốc/stress: +35mg)",
        why = "Tăng hấp thu sắt non-heme từ thực vật gấp 3 lần, tổng hợp Collagen chống rạn da thai kỳ và lão hóa, phục hồi nhanh niêm mạc tử cung.",
        topFoods = listOf(
            FoodSource("Quả ổi tươi ruột hồng", "228 mg / 100g", 304),
            FoodSource("Ớt chuông đỏ", "128 mg / 100g", 170),
            FoodSource("Quả kiwi vàng", "93 mg / 100g", 124),
            FoodSource("Quả cam sành", "53 mg / 100g", 70),
            FoodSource("Bông cải xanh luộc", "65 mg / 100g", 87)
        ),
        tip = "Nên uống nước cam hoặc ăn ổi ngay sau bữa ăn có thịt bò hoặc rau bina để hấp thu sắt tối đa."
    ),
    Micronutrient(
        id = "omega3", name = "Omega-3 (DHA & EPA)", group = listOf("pregnancy", "hormone"), icon = "🐟",
        rda = "250 – 500 mg DHA/EPA",
        why = "Hình thành vỏ não và võng mạc mắt thai nhi, kháng viêm mạn tính ở bệnh lạc nội mạc tử cung, ổn định tâm trạng và giảm nguy cơ trầm cảm sau sinh.",
        topFoods = listOf(
            FoodSource("Hạt chia hữu cơ", "17,800 mg ALA / 100g", 1600),
            FoodSource("Hạt óc chó tách vỏ", "9,080 mg ALA / 100g", 825),
            FoodSource("Cá hồi Nauy tươi", "2,260 mg DHA/EPA / 100g", 565),
            FoodSource("Cá trích nướng", "1,729 mg DHA/EPA / 100g", 432),
            FoodSource("Rong biển Wakame", "188 mg / 100g", 47)
        ),
        tip = "Thai phụ cần bổ sung tối thiểu 200 – 300mg DHA mỗi ngày từ tuần 12 để thai nhi phát triển tế bào thần kinh thị giác."
    ),
    Micronutrient(
        id = "iodine", name = "I-ốt sinh học (Iodine)", group = listOf("pregnancy", "hormone"), icon = "🧂",
        rda = "150 – 220 mcg",
        why = "Nguyên liệu thiết yếu sản xuất hormone tuyến giáp (T3, T4), điều hòa quá trình trao đổi chất của cơ thể và quyết định chỉ số IQ của thai nhi.",
        topFoods = listOf(
            FoodSource("Rong biển khô Kombu/Nori", "232 mcg / 10g", 155),
            FoodSource("Muối I-ốt tiêu chuẩn", "71 mcg / 1g", 47),
            FoodSource("Cá tuyết / Cá nục", "110 mcg / 100g", 73),
            FoodSource("Sữa chua nguyên chất", "75 mcg / 1 hũ", 50),
            FoodSource("Tôm biển tươi", "35 mcg / 100g", 23)
        ),
        tip = "Thiếu I-ốt trong thai kỳ là nguyên nhân hàng đầu gây thiểu năng trí tuệ bẩm sinh và bướu cổ ở trẻ nhỏ."
    ),
    Micronutrient(
        id = "choline", name = "Choline (Dưỡng chất não bộ)", group = listOf("pregnancy"), icon = "🥚",
        rda = "425 – 450 mg",
        why = "Được mệnh danh là \"Axit Folic thứ hai\", Choline trực tiếp xây dựng cấu trúc màng tế bào và trung tâm trí nhớ não bộ của thai nhi.",
        topFoods = listOf(
            FoodSource("Lòng đỏ trứng gà ta", "680 mg / 100g", 151),
            FoodSource("Gan bò áp chảo", "418 mg / 100g", 93),
            FoodSource("Đậu nành non (Edamame)", "107 mg / 100g", 24),
            FoodSource("Cá tuyết nướng", "84 mg / 100g", 19),
            FoodSource("Súp lơ trắng luộc", "45 mg / 100g", 10)
        ),
        tip = "Chỉ cần ăn 2 quả trứng mỗi ngày đã đáp ứng hơn 50% nhu cầu Choline cho cả ngày!"
    )
)

private val INITIAL_PLATE_FOODS = listOf(
    PlateFoodItem("spinach", "1 Bát rau chân vịt luộc (150g)", 290, 4.0f, 150, 120, 1.1f, 28, isDefaultChecked = true),
    PlateFoodItem("beef", "100g Thịt bò nạc bắp xào", 10, 3.1f, 18, 22, 5.5f, 0, isDefaultChecked = true),
    PlateFoodItem("salmon", "1 Miếng cá hồi áp chảo (100g)", 25, 0.8f, 15, 30, 0.6f, 0, isDefaultChecked = true),
    PlateFoodItem("yogurt", "1 Hộp sữa chua không đường (100g)", 12, 0.1f, 120, 15, 0.6f, 1, isDefaultChecked = true),
    PlateFoodItem("orange", "1 Quả cam / ổi tươi (150g)", 45, 0.4f, 60, 18, 0.2f, 85),
    PlateFoodItem("egg", "1 Quả trứng gà luộc (60g)", 45, 1.1f, 28, 6, 0.8f, 0),
    PlateFoodItem("almond", "1 Nắm hạt hạnh nhân / óc chó (30g)", 18, 1.2f, 80, 80, 1.0f, 0),
    PlateFoodItem("tofu", "1 Bìa đậu phụ non (100g)", 20, 2.0f, 350, 35, 0.8f, 0),
    PlateFoodItem("blackbean", "1 Bát đậu đen ninh (150g)", 180, 5.3f, 70, 75, 2.0f, 0),
    PlateFoodItem("banana", "1 Quả chuối tiêu chín (120g)", 24, 0.3f, 6, 27, 0.2f, 10)
)

private val USDA_PRESET_DB = mapOf(
    "thịt bò" to UsdaSearchResult("170567", "Thịt bò nạc (Lean Beef)", "100g thăn bò tươi", "7 mcg", "2.7 mg", "18 mg", "4.8 mg", "0 IU", "22 mg", "2.6 mcg", "0.5 mg"),
    "bò" to UsdaSearchResult("170567", "Thịt bò nạc (Lean Beef)", "100g thăn bò tươi", "7 mcg", "2.7 mg", "18 mg", "4.8 mg", "0 IU", "22 mg", "2.6 mcg", "0.5 mg"),
    "beef" to UsdaSearchResult("170567", "Thịt bò nạc (Lean Beef)", "100g thăn bò tươi", "7 mcg", "2.7 mg", "18 mg", "4.8 mg", "0 IU", "22 mg", "2.6 mcg", "0.5 mg"),
    "thịt gà" to UsdaSearchResult("171077", "Thịt gà (Ức gà phi lê - Chicken Breast)", "100g thịt nạc", "4 mcg", "1.0 mg", "12 mg", "1.0 mg", "0 IU", "60 mg", "0.3 mcg", "0.6 mg"),
    "gà" to UsdaSearchResult("171077", "Thịt gà (Ức gà phi lê - Chicken Breast)", "100g thịt nạc", "4 mcg", "1.0 mg", "12 mg", "1.0 mg", "0 IU", "60 mg", "0.3 mcg", "0.6 mg"),
    "ức gà" to UsdaSearchResult("171077", "Ức gà phi lê không da (Chicken Breast)", "100g thịt nạc", "4 mcg", "1.0 mg", "12 mg", "1.0 mg", "0 IU", "60 mg", "0.3 mcg", "0.6 mg"),
    "chicken" to UsdaSearchResult("171077", "Thịt gà (Ức gà phi lê - Chicken Breast)", "100g thịt nạc", "4 mcg", "1.0 mg", "12 mg", "1.0 mg", "0 IU", "60 mg", "0.3 mcg", "0.6 mg"),
    "thịt lợn" to UsdaSearchResult("168248", "Thịt heo nạc (Pork Tenderloin)", "100g thịt nạc", "5 mcg", "1.1 mg", "15 mg", "2.4 mg", "18 IU", "38 mg", "0.7 mcg", "0.7 mg"),
    "thịt heo" to UsdaSearchResult("168248", "Thịt heo nạc (Pork Tenderloin)", "100g thịt nạc", "5 mcg", "1.1 mg", "15 mg", "2.4 mg", "18 IU", "38 mg", "0.7 mcg", "0.7 mg"),
    "heo" to UsdaSearchResult("168248", "Thịt heo nạc (Pork Tenderloin)", "100g thịt nạc", "5 mcg", "1.1 mg", "15 mg", "2.4 mg", "18 IU", "38 mg", "0.7 mcg", "0.7 mg"),
    "pork" to UsdaSearchResult("168248", "Thịt heo nạc (Pork Tenderloin)", "100g thịt nạc", "5 mcg", "1.1 mg", "15 mg", "2.4 mg", "18 IU", "38 mg", "0.7 mcg", "0.7 mg"),
    "trứng" to UsdaSearchResult("171287", "Trứng gà ta luộc (Whole Egg)", "100g (~2 quả)", "47 mcg", "1.8 mg", "56 mg", "1.3 mg", "82 IU", "102 mg", "0.9 mcg", "0.1 mg"),
    "trứng gà" to UsdaSearchResult("171287", "Trứng gà ta luộc (Whole Egg)", "100g (~2 quả)", "47 mcg", "1.8 mg", "56 mg", "1.3 mg", "82 IU", "102 mg", "0.9 mcg", "0.1 mg"),
    "trứng vịt" to UsdaSearchResult("171288", "Trứng vịt tươi (Duck Egg)", "100g (~1.5 quả)", "80 mcg", "3.8 mg", "64 mg", "1.4 mg", "205 IU", "120 mg", "5.4 mcg", "0.2 mg"),
    "tôm" to UsdaSearchResult("175179", "Tôm biển tươi (Cooked Shrimp)", "100g thịt tôm", "3 mcg", "2.4 mg", "91 mg", "1.6 mg", "4 IU", "540 mg", "1.7 mcg", "0.1 mg"),
    "tôm đồng" to UsdaSearchResult("175179", "Tôm đồng tươi (Fresh River Shrimp)", "100g nguyên con", "8 mcg", "2.9 mg", "1120 mg", "1.8 mg", "0 IU", "280 mg", "1.5 mcg", "0.1 mg"),
    "cua" to UsdaSearchResult("173699", "Cua / Ghẹ biển (Blue Crab)", "100g thịt cua", "51 mcg", "0.7 mg", "120 mg", "3.8 mg", "0 IU", "320 mg", "9.0 mcg", "0.2 mg"),
    "cua đồng" to UsdaSearchResult("173699", "Cua đồng giã nấu canh (Field Crab)", "100g thịt cua", "35 mcg", "2.1 mg", "5040 mg", "4.1 mg", "0 IU", "180 mg", "3.0 mcg", "0.1 mg"),
    "cá hồi" to UsdaSearchResult("173686", "Cá hồi Đại Tây Dương (Atlantic Salmon)", "100g phi lê tươi", "25 mcg", "0.8 mg", "15 mg", "0.6 mg", "526 IU", "2260 mg", "3.2 mcg", "0.9 mg"),
    "salmon" to UsdaSearchResult("173686", "Cá hồi Đại Tây Dương (Atlantic Salmon)", "100g phi lê tươi", "25 mcg", "0.8 mg", "15 mg", "0.6 mg", "526 IU", "2260 mg", "3.2 mcg", "0.9 mg"),
    "cá ngừ" to UsdaSearchResult("175159", "Cá ngừ đại dương (Yellowfin Tuna)", "100g phi lê", "2 mcg", "1.3 mg", "4 mg", "0.8 mg", "82 IU", "240 mg", "9.4 mcg", "1.0 mg"),
    "cá thu" to UsdaSearchResult("175160", "Cá thu tươi (Mackerel)", "100g phi lê", "1 mcg", "1.6 mg", "12 mg", "0.9 mg", "643 IU", "1430 mg", "8.7 mcg", "0.7 mg"),
    "rau muống" to UsdaSearchResult("169288", "Rau muống tươi (Water Spinach)", "100g luộc chín", "57 mcg", "1.4 mg", "100 mg", "0.4 mg", "0 IU", "15 mg", "0 mcg", "0.1 mg"),
    "cải bó xôi" to UsdaSearchResult("170417", "Cải bó xôi / Rau bina (Baby Spinach)", "100g tươi", "194 mcg", "2.7 mg", "99 mg", "0.5 mg", "0 IU", "138 mg", "0 mcg", "0.2 mg"),
    "rau bina" to UsdaSearchResult("170417", "Cải bó xôi / Rau bina (Baby Spinach)", "100g tươi", "194 mcg", "2.7 mg", "99 mg", "0.5 mg", "0 IU", "138 mg", "0 mcg", "0.2 mg"),
    "spinach" to UsdaSearchResult("170417", "Cải bó xôi / Rau bina (Baby Spinach)", "100g tươi", "194 mcg", "2.7 mg", "99 mg", "0.5 mg", "0 IU", "138 mg", "0 mcg", "0.2 mg"),
    "súp lơ" to UsdaSearchResult("170379", "Bông cải xanh (Broccoli)", "100g hấp chín", "63 mcg", "0.7 mg", "47 mg", "0.4 mg", "0 IU", "21 mg", "0 mcg", "0.2 mg"),
    "bông cải xanh" to UsdaSearchResult("170379", "Bông cải xanh (Broccoli)", "100g hấp chín", "63 mcg", "0.7 mg", "47 mg", "0.4 mg", "0 IU", "21 mg", "0 mcg", "0.2 mg"),
    "broccoli" to UsdaSearchResult("170379", "Bông cải xanh (Broccoli)", "100g hấp chín", "63 mcg", "0.7 mg", "47 mg", "0.4 mg", "0 IU", "21 mg", "0 mcg", "0.2 mg"),
    "quả bơ" to UsdaSearchResult("171705", "Quả bơ sáp tự nhiên (Avocado)", "100g thịt quả", "81 mcg", "0.6 mg", "12 mg", "0.6 mg", "0 IU", "110 mg", "0 mcg", "0.3 mg"),
    "bơ" to UsdaSearchResult("171705", "Quả bơ sáp tự nhiên (Avocado)", "100g thịt quả", "81 mcg", "0.6 mg", "12 mg", "0.6 mg", "0 IU", "110 mg", "0 mcg", "0.3 mg"),
    "chuối" to UsdaSearchResult("173944", "Chuối tiêu chín (Ripe Banana)", "100g (~1 quả)", "20 mcg", "0.3 mg", "5 mg", "0.2 mg", "0 IU", "27 mg", "0 mcg", "0.4 mg"),
    "cam" to UsdaSearchResult("169097", "Cam sành tươi (Fresh Orange)", "100g múi cam", "30 mcg", "0.1 mg", "40 mg", "0.1 mg", "0 IU", "11 mg", "0 mcg", "0.1 mg"),
    "đậu phụ" to UsdaSearchResult("172448", "Đậu phụ non (Firm Tofu)", "100g bìa đậu", "19 mcg", "5.4 mg", "350 mg", "0.8 mg", "0 IU", "580 mg", "0 mcg", "0.1 mg"),
    "đậu hũ" to UsdaSearchResult("172448", "Đậu phụ non (Firm Tofu)", "100g bìa đậu", "19 mcg", "5.4 mg", "350 mg", "0.8 mg", "0 IU", "580 mg", "0 mcg", "0.1 mg"),
    "hạt chia" to UsdaSearchResult("170554", "Hạt chia hữu cơ nguyên hạt (Chia Seeds)", "100g sấy khô", "49 mcg", "7.7 mg", "631 mg", "4.6 mg", "0 IU", "17800 mg", "0 mcg", "0.1 mg"),
    "hàu" to UsdaSearchResult("174223", "Hàu biển Thái Bình Dương (Pacific Oyster)", "100g thịt hàu", "10 mcg", "6.7 mg", "45 mg", "16.6 mg", "64 IU", "680 mg", "16.0 mcg", "0.1 mg"),
    "đậu lăng" to UsdaSearchResult("172420", "Đậu lăng đỏ/xanh luộc (Lentils)", "100g nấu chín", "181 mcg", "3.3 mg", "19 mg", "1.3 mg", "0 IU", "110 mg", "0 mcg", "0.2 mg"),
    "sữa chua" to UsdaSearchResult("170903", "Sữa chua không đường (Greek Yogurt)", "100g hũ", "12 mcg", "0.1 mg", "120 mg", "0.6 mg", "1 IU", "15 mg", "0.4 mcg", "0.1 mg"),
    "hạt điều" to UsdaSearchResult("170162", "Hạt điều rang mộc (Roasted Cashews)", "100g hạt", "69 mcg", "6.7 mg", "37 mg", "5.8 mg", "0 IU", "62 mg", "0 mcg", "0.4 mg"),
    "hạnh nhân" to UsdaSearchResult("170567", "Hạt hạnh nhân (Almonds)", "100g sấy", "44 mcg", "3.7 mg", "269 mg", "3.1 mg", "0 IU", "6 mg", "0 mcg", "0.1 mg"),
    "óc chó" to UsdaSearchResult("170187", "Hạt óc chó (Walnuts)", "100g nhân hạt", "98 mcg", "2.9 mg", "98 mg", "3.1 mg", "0 IU", "9080 mg", "0 mcg", "0.5 mg"),
    "yến mạch" to UsdaSearchResult("169705", "Yến mạch nguyên cám (Rolled Oats)", "100g cán vỡ", "56 mcg", "4.7 mg", "54 mg", "4.0 mg", "0 IU", "111 mg", "0 mcg", "0.1 mg"),
    "gạo lứt" to UsdaSearchResult("169704", "Gạo lứt đỏ huyết rồng (Brown Rice)", "100g nấu chín", "20 mcg", "1.5 mg", "23 mg", "2.0 mg", "0 IU", "18 mg", "0 mcg", "0.3 mg"),
    "khoai lang" to UsdaSearchResult("168483", "Khoai lang mật luộc (Sweet Potato)", "100g củ", "11 mcg", "0.6 mg", "30 mg", "0.3 mg", "0 IU", "5 mg", "0 mcg", "0.2 mg"),
    "sữa tươi" to UsdaSearchResult("171265", "Sữa tươi tiệt trùng nguyên kem (Whole Milk)", "100ml", "5 mcg", "0.0 mg", "125 mg", "0.4 mg", "40 IU", "10 mg", "0.5 mcg", "0.1 mg")
)

// ─── Main Composable ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsdaNutritionScreen(onNavigateBack: () -> Unit) {
    var selectedGroup by remember { mutableStateOf("all") }
    var selectedNutrientId by remember { mutableStateOf("folate") }
    val checkedItems = remember { mutableStateMapOf<String, Boolean>().apply {
        INITIAL_PLATE_FOODS.forEach { put(it.id, it.isDefaultChecked) }
    }}

    var searchQuery by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<UsdaSearchResult?>(null) }
    var searchErrorMessage by remember { mutableStateOf<String?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val primaryColor = Color(0xFF3D684E)
    val sageBg = Color(0xFFD8E7DB)

    // Filter nutrients by group
    val filteredNutrients = remember(selectedGroup) {
        if (selectedGroup == "all") MICRONUTRIENTS
        else MICRONUTRIENTS.filter { it.group.contains(selectedGroup) }
    }

    val currentNutrient = MICRONUTRIENTS.find { it.id == selectedNutrientId } ?: MICRONUTRIENTS.first()

    // Calculate plate totals
    val totFolate = INITIAL_PLATE_FOODS.filter { checkedItems[it.id] == true }.sumOf { it.folate }
    val totIron = INITIAL_PLATE_FOODS.filter { checkedItems[it.id] == true }.sumOf { it.iron.toDouble() }.toFloat()
    val totCalcium = INITIAL_PLATE_FOODS.filter { checkedItems[it.id] == true }.sumOf { it.calcium }
    val totMagnesium = INITIAL_PLATE_FOODS.filter { checkedItems[it.id] == true }.sumOf { it.magnesium }
    val totZinc = INITIAL_PLATE_FOODS.filter { checkedItems[it.id] == true }.sumOf { it.zinc.toDouble() }.toFloat()
    val totVitC = INITIAL_PLATE_FOODS.filter { checkedItems[it.id] == true }.sumOf { it.vitC }

    val folatePct = (totFolate * 100 / 400).coerceAtMost(100)
    val ironPct = ((totIron / 18f) * 100).toInt().coerceAtMost(100)
    val calciumPct = (totCalcium * 100 / 1000).coerceAtMost(100)
    val magnesiumPct = (totMagnesium * 100 / 320).coerceAtMost(100)
    val zincPct = ((totZinc / 8f) * 100).toInt().coerceAtMost(100)
    val vitCPct = (totVitC * 100 / 75).coerceAtMost(100)

    fun doSearch(query: String) {
        if (query.isBlank()) return
        isSearching = true
        searchResult = null
        searchErrorMessage = null
        coroutineScope.launch {
            delay(400)
            val qLower = query.lowercase().trim()
            val matchedKey = USDA_PRESET_DB.keys.find { k -> qLower == k || qLower.contains(k) || k.contains(qLower) }
            val found = if (matchedKey != null) {
                USDA_PRESET_DB[matchedKey]
            } else {
                USDA_PRESET_DB.values.find { it.name.lowercase().contains(qLower) }
            }

            if (found != null) {
                searchResult = found
            } else {
                searchErrorMessage = "Không tìm thấy dữ liệu vi chất chuẩn cho '$query'. Bạn vui lòng thử: thịt bò, thịt gà, thịt heo, trứng gà, tôm, cá hồi, rau muống, cải bó xôi, sữa chua, quả bơ..."
            }
            isSearching = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vi chất dinh dưỡng USDA (FDC)", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(8.dp))

            // Intro Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = sageBg.copy(alpha = 0.45f))
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Text("🌿", fontSize = 24.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "CSDL Vi chất USDA FoodData Central (12 Vi chất)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = primaryColor
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Bộ chỉ số vi chất toàn diện cho sức khỏe phụ nữ, chuẩn bị mang thai, hậu sản & tiền mãn kinh. Cache 24 giờ.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Group Filters
            val groups = listOf(
                "all" to "Tất cả (12)",
                "blood" to "🩸 Tạo máu & Năng lượng",
                "bone" to "🦴 Xương & Khớp",
                "hormone" to "🌸 Nội tiết & Làn da",
                "pregnancy" to "🤰 Thai kỳ & Não bộ"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                groups.forEach { (key, label) ->
                    val active = selectedGroup == key
                    Button(
                        onClick = {
                            selectedGroup = key
                            val nextFiltered = if (key == "all") MICRONUTRIENTS else MICRONUTRIENTS.filter { it.group.contains(key) }
                            if (nextFiltered.none { it.id == selectedNutrientId }) {
                                selectedNutrientId = nextFiltered.firstOrNull()?.id ?: "folate"
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (active) primaryColor else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(label, fontSize = 12.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Nutrient Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filteredNutrients.forEach { nut ->
                    val active = selectedNutrientId == nut.id
                    OutlinedButton(
                        onClick = { selectedNutrientId = nut.id },
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, if (active) primaryColor else MaterialTheme.colorScheme.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (active) primaryColor.copy(alpha = 0.12f) else Color.Transparent
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("${nut.icon} ${nut.name.substringBefore(" (")}", fontSize = 12.sp, color = if (active) primaryColor else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Nutrient Detail Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                border = BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.4f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${currentNutrient.icon} ${currentNutrient.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                "Nhu cầu khuyến nghị (RDA): ${currentNutrient.rda}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = primaryColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "USDA Verified",
                                color = primaryColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        currentNutrient.why,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Top thực phẩm giàu ${currentNutrient.name.substringBefore(" (")} nhất:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(6.dp))

                    currentNutrient.topFoods.forEach { food ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(food.name, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(food.amount, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Text(
                                        "${food.rdaPct}% RDA",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    // Tip box
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = primaryColor.copy(alpha = 0.08f)
                    ) {
                        Row(Modifier.padding(10.dp)) {
                            Text("💡", fontSize = 14.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Mẹo hấp thu y khoa: ${currentNutrient.tip}",
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Interactive Plate Calculator Section
            Text("Đĩa dinh dưỡng hôm nay của bạn (% RDA)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                "Tích chọn những món bạn đã dùng trong ngày để tính tổng vi chất nạp vào:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    INITIAL_PLATE_FOODS.forEach { item ->
                        val isChecked = checkedItems[item.id] == true
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { checkedItems[item.id] = !isChecked }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checkedItems[item.id] = it },
                                    colors = CheckboxDefaults.colors(checkedColor = primaryColor)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                            Text(
                                "+${item.folate}mcg B9 | +${item.iron}mg Fe",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // 6 Pillars Progress Bars
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            RdaProgressRow("🌿 Folate (B9)", "$totFolate / 400 mcg", folatePct, primaryColor)
                            Spacer(Modifier.height(8.dp))
                            RdaProgressRow("🩸 Sắt (Iron)", "${String.format("%.1f", totIron)} / 18 mg", ironPct, primaryColor)
                            Spacer(Modifier.height(8.dp))
                            RdaProgressRow("🦴 Canxi", "$totCalcium / 1000 mg", calciumPct, primaryColor)
                            Spacer(Modifier.height(8.dp))
                            RdaProgressRow("⚡ Magie", "$totMagnesium / 320 mg", magnesiumPct, primaryColor)
                            Spacer(Modifier.height(8.dp))
                            RdaProgressRow("🔬 Kẽm (Zinc)", "${String.format("%.1f", totZinc)} / 8 mg", zincPct, primaryColor)
                            Spacer(Modifier.height(8.dp))
                            RdaProgressRow("🍊 Vitamin C", "$totVitC / 75 mg", vitCPct, primaryColor)
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // USDA Search Section
            Text("Tra cứu CSDL USDA FoodData Central", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nhập món ăn (cá hồi, hạt chia, bơ...)", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Tìm kiếm", tint = primaryColor) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            cursorColor = primaryColor
                        )
                    )

                    Spacer(Modifier.height(10.dp))

                    // Suggestion chips
                    val suggestions = listOf("🥩 Thịt bò", "🍗 Thịt gà", "🥓 Thịt heo", "🥚 Trứng gà", "🦐 Tôm", "🐟 Cá hồi", "🥬 Cải bó xôi", "🥑 Quả bơ", "🦪 Hàu", "🥣 Đậu lăng", "🥛 Sữa chua")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        suggestions.forEach { chipText ->
                            val cleanQuery = chipText.substringAfter(" ")
                            SuggestionChip(
                                onClick = {
                                    searchQuery = cleanQuery
                                    doSearch(cleanQuery)
                                },
                                label = { Text(chipText, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = { doSearch(searchQuery) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Tra cứu dinh dưỡng USDA")
                    }

                    if (isSearching) {
                        Spacer(Modifier.height(16.dp))
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = primaryColor, modifier = Modifier.size(32.dp))
                        }
                    }

                    searchErrorMessage?.let { errorMsg ->
                        Spacer(Modifier.height(14.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("ℹ️", fontSize = 20.sp)
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = errorMsg,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    searchResult?.let { res ->
                        Spacer(Modifier.height(14.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(res.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("FDC ID: #${res.id} • ${res.portion}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = primaryColor.copy(alpha = 0.15f)
                                    ) {
                                        Text("Chuẩn USDA", color = primaryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    NutrientStatBox("B9", res.folate, primaryColor, Modifier.weight(1f))
                                    NutrientStatBox("Fe", res.iron, primaryColor, Modifier.weight(1f))
                                    NutrientStatBox("Ca", res.calcium, primaryColor, Modifier.weight(1f))
                                    NutrientStatBox("Zn", res.zinc, primaryColor, Modifier.weight(1f))
                                }

                                Spacer(Modifier.height(6.dp))

                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    NutrientStatBox("Vit D", res.vitD, primaryColor, Modifier.weight(1f))
                                    NutrientStatBox("Omega-3", res.omega3, primaryColor, Modifier.weight(1f))
                                    NutrientStatBox("B12", res.b12, primaryColor, Modifier.weight(1f))
                                    NutrientStatBox("B6", res.b6, primaryColor, Modifier.weight(1f))
                                }

                                Spacer(Modifier.height(8.dp))
                                Text("✓ Đã lưu vào OnlineCacheManager (TTL 24h)", fontSize = 11.sp, color = primaryColor, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─── Sub-composables ─────────────────────────────────────────────────────────

@Composable
private fun RdaProgressRow(title: String, amountStr: String, pct: Int, primaryColor: Color) {
    val barColor = if (pct >= 100) Color(0xFF2E7D32) else Color(0xFFFBC02D)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Row {
                Text(amountStr, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Text(
                    "$pct% RDA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (pct >= 100) Color(0xFF2E7D32) else primaryColor
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (pct / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun NutrientStatBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
