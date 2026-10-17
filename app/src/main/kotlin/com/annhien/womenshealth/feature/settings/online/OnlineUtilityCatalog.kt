package com.annhien.womenshealth.feature.settings.online

data class OnlineUtilityCatalogItem(
    val sourceId: String,
    val titleVi: String,
    val descriptionVi: String
)

object OnlineUtilityCatalog {
    val utilities = listOf(
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
            sourceId = "openaq",
            titleVi = "Chất lượng không khí",
            descriptionVi = "Xem khu vực quan sát không khí gần vị trí của bạn từ OpenAQ."
        )
    )
}
