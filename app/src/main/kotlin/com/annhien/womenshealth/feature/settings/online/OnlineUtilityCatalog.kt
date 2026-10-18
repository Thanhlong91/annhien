package com.annhien.womenshealth.feature.settings.online

data class OnlineUtilityCatalogItem(
    val sourceId: String,
    val titleVi: String,
    val descriptionVi: String
)

object OnlineUtilityCatalog {
    val utilities = listOf(
        OnlineUtilityCatalogItem(
            sourceId = "openmeteo_weather",
            titleVi = "Thời tiết & Cảnh báo UV",
            descriptionVi = "Chỉ số UV theo giờ, nhiệt độ và khuyến nghị kem chống nắng từ Open-Meteo."
        ),
        OnlineUtilityCatalogItem(
            sourceId = "wger_workout",
            titleVi = "Bài tập thể dục phụ nữ",
            descriptionVi = "Bài tập Kegel, thai kỳ và giảm đau từ nguồn mở Wger Workout Manager."
        ),
        OnlineUtilityCatalogItem(
            sourceId = "quotable",
            titleVi = "Lời khích lệ mỗi ngày",
            descriptionVi = "Trích dẫn truyền cảm hứng sức khỏe và bình an nội tâm từ Quotable.io."
        ),
        OnlineUtilityCatalogItem(
            sourceId = "openfoodfacts",
            titleVi = "Tra cứu thực phẩm",
            descriptionVi = "Tìm thông tin dinh dưỡng theo tên hoặc mã vạch từ Open Food Facts."
        ),
        OnlineUtilityCatalogItem(
            sourceId = "myhealthfinder",
            titleVi = "Kiến thức sức khỏe",
            descriptionVi = "Xem chủ đề sức khỏe được cập nhật từ MyHealthfinder."
        ),
        OnlineUtilityCatalogItem(
            sourceId = "usda_fdc",
            titleVi = "Vi chất dinh dưỡng phụ nữ",
            descriptionVi = "Axit Folic, Sắt, Canxi và bộ đĩa vi chất từ cơ sở dữ liệu USDA FoodData Central."
        ),
        OnlineUtilityCatalogItem(
            sourceId = "openmeteo_pollen",
            titleVi = "Phấn hoa & Dị ứng",
            descriptionVi = "Chỉ số phấn hoa và cảnh báo dị ứng hô hấp từ Open-Meteo Air Quality."
        ),
        OnlineUtilityCatalogItem(
            sourceId = "openaq",
            titleVi = "Chất lượng không khí",
            descriptionVi = "Xem khu vực quan sát không khí gần vị trí của bạn từ OpenAQ."
        )
    )
}
