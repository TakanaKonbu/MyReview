package com.takanakonbu.myreview.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.takanakonbu.myreview.category.data.AppDatabase
import com.takanakonbu.myreview.category.data.CategoryRepository
import com.takanakonbu.myreview.category.ui.CategoryViewModel
import com.takanakonbu.myreview.category.ui.CategoryViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewScreen(
    viewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModelFactory(
            CategoryRepository(
                AppDatabase.getDatabase(LocalContext.current).categoryDao()
            )
        )
    ),
    onNavigateBack: () -> Unit
) {
    val name by viewModel.newCategoryName.collectAsState()
    val items by viewModel.newCategoryItems.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ジャンル入力フィールド
            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.updateNewCategoryName(it) },
                placeholder = { Text("ジャンルを追加") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF6D6DF6),
                    unfocusedBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 評価項目の追加セクション
            Text(
                "評価項目の追加(最大5個)\n最低1個は必須(1個のみの例：総合)",
                modifier = Modifier.padding(bottom = 8.dp)
            )

            items.forEachIndexed { index, item ->
                OutlinedTextField(
                    value = item,
                    onValueChange = { viewModel.updateNewCategoryItem(index, it) },
                    placeholder = { Text("評価項目の追加") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF6D6DF6),
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 保存ボタン
            Button(
                onClick = {
                    viewModel.insertCategory()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D6DF6))
            ) {
                Text("保存", color = Color.White, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))

            // キャンセルボタン
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD27778))
            ) {
                Text("キャンセル", fontSize = 20.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddReviewScreenPreview() {
    AddReviewScreen(onNavigateBack = {})
}