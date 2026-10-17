package com.annhien.womenshealth.feature.online.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.annhien.womenshealth.core.online.food.OpenFoodFactsProduct

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSearchScreen(
    onNavigateBack: () -> Unit,
    viewModel: FoodSearchViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tra cứu thực phẩm") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Tên thực phẩm") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { viewModel.search(query) }) {
                        Icon(Icons.Default.Search, contentDescription = "Tìm")
                    }
                }
            )

            when (val state = uiState) {
                FoodSearchUiState.Idle -> {
                    Text(
                        text = "Nhập tên thực phẩm để tra cứu thông tin dinh dưỡng từ Open Food Facts.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                FoodSearchUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Text("Đang tra cứu…")
                    }
                }

                is FoodSearchUiState.Success -> {
                    if (state.products.isEmpty()) {
                        Text("Không tìm thấy kết quả phù hợp.")
                    } else {
                        if (state.fromCache) {
                            Text(
                                text = "Kết quả từ bộ nhớ tạm",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.products, key = { it.code ?: it.productName ?: "" }) { product ->
                                FoodProductCard(product)
                            }
                        }
                    }
                }

                is FoodSearchUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun FoodProductCard(product: OpenFoodFactsProduct) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = product.productNameVi
                    ?: product.productName
                    ?: "Thực phẩm không tên",
                style = MaterialTheme.typography.titleMedium
            )

            product.brands?.let {
                Text(text = "Thương hiệu: $it", style = MaterialTheme.typography.bodySmall)
            }

            product.quantity?.let {
                Text(text = "Khối lượng: $it", style = MaterialTheme.typography.bodySmall)
            }

            product.nutriments?.let { nutrients ->
                Text(
                    text = buildString {
                        append("Mỗi 100 g: ")
                        nutrients.energyKcalPer100g?.let { append("${it} kcal; ") }
                        nutrients.proteinsPer100g?.let { append("đạm ${it} g; ") }
                        nutrients.carbohydratesPer100g?.let { append("tinh bột ${it} g; ") }
                        nutrients.fatPer100g?.let { append("chất béo ${it} g") }
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
