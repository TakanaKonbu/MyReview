package com.takanakonbu.myreview.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.takanakonbu.myreview.category.data.Category
import com.takanakonbu.myreview.category.data.CategoryDatabase
import com.takanakonbu.myreview.category.data.CategoryRepository
import com.takanakonbu.myreview.category.ui.CategoryViewModel
import com.takanakonbu.myreview.category.ui.CategoryViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoryScreen(
    categoryId: Int,
    viewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModelFactory(
            CategoryRepository(
                CategoryDatabase.getDatabase(LocalContext.current).categoryDao()
            )
        )
    ),
    onNavigateBack: () -> Unit
) {
    var category by remember { mutableStateOf<Category?>(null) }
    var name by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(List(5) { "" }) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(categoryId) {
        category = viewModel.getCategoryById(categoryId)
        category?.let {
            name = it.name
            items = listOf(it.item1, it.item2 ?: "", it.item3 ?: "", it.item4 ?: "", it.item5 ?: "")
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("ジャンル名") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF6D6DF6),
                    unfocusedBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "評価項目(最大5個)",
                modifier = Modifier.padding(bottom = 8.dp)
            )

            items.forEachIndexed { index, item ->
                OutlinedTextField(
                    value = item,
                    onValueChange = { newValue ->
                        items = items.toMutableList().apply { this[index] = newValue }
                    },
                    label = { Text("評価項目 ${index + 1}") },
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

            Button(
                onClick = {
                    category?.let {
                        val updatedCategory = it.copy(
                            name = name,
                            item1 = items[0],
                            item2 = items[1].takeIf { it.isNotBlank() },
                            item3 = items[2].takeIf { it.isNotBlank() },
                            item4 = items[3].takeIf { it.isNotBlank() },
                            item5 = items[4].takeIf { it.isNotBlank() }
                        )
                        viewModel.updateCategory(updatedCategory)
                    }
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D6DF6))
            ) {
                Text("更新", color = Color.White, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))

            // キャンセルボタン
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD27778))
            ) {
                Text("キャンセル", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 削除ボタン
            Button(
                onClick = { showDeleteConfirmDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD27778))
            ) {
                Text("削除", color = Color.White, fontSize = 20.sp)
            }
        }
    }

    // 削除確認ダイアログ
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("カテゴリーの削除") },
            text = { Text("このカテゴリーを削除してもよろしいですか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        category?.let {
                            viewModel.deleteCategory(it)
                        }
                        showDeleteConfirmDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("削除", color = Color(0xFFD27778))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
}