package com.takanakonbu.myreview.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.takanakonbu.myreview.category.data.AppDatabase
import com.takanakonbu.myreview.category.data.Category
import com.takanakonbu.myreview.category.data.CategoryRepository
import com.takanakonbu.myreview.review.data.ReviewRepository
import com.takanakonbu.myreview.ui.theme.MainColor

@Composable
fun EditCategoryScreen(
    categoryId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModelFactory(
            CategoryRepository(AppDatabase.getDatabase(context).categoryDao()),
            ReviewRepository(AppDatabase.getDatabase(context).reviewDao()),
            context
        )
    )

    var category by remember { mutableStateOf<Category?>(null) }
    var name by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(List(5) { "" }) }
    var visibleItemCount by remember { mutableStateOf(1) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    fun updateItem(index: Int, newValue: String) {
        val currentItems = items.toMutableList().apply {
            this[index] = newValue
        }
        val (reorganizedItems, newVisibleCount) = viewModel.reorganizeAndGetItemCount(currentItems)
        items = reorganizedItems
        visibleItemCount = newVisibleCount
    }

    LaunchedEffect(categoryId) {
        category = viewModel.getCategoryById(categoryId)
        category?.let {
            name = it.name
            val initialItems = listOf(
                it.item1,
                it.item2 ?: "",
                it.item3 ?: "",
                it.item4 ?: "",
                it.item5 ?: ""
            )
            val (reorganizedItems, initialVisibleCount) = viewModel.reorganizeAndGetItemCount(initialItems)
            items = reorganizedItems
            visibleItemCount = initialVisibleCount
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDeleteConfirmDialog = true },
                containerColor = MainColor.value,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "削除")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("カテゴリー名") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MainColor.value,
                    unfocusedBorderColor = Color.Gray
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "評価項目(最大5個)",
                modifier = Modifier.padding(bottom = 8.dp)
            )

            repeat(visibleItemCount) { index ->
                OutlinedTextField(
                    value = items[index],
                    onValueChange = { newValue -> updateItem(index, newValue) },
                    label = { Text("評価項目 ${index + 1}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MainColor.value,
                        unfocusedBorderColor = Color.Gray
                    ),
                    singleLine = true
                )
            }

            if (visibleItemCount < 5) {
                TextButton(
                    onClick = {
                        if (visibleItemCount < 5) {
                            visibleItemCount++
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ 評価項目を追加", color = MainColor.value)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    category?.let { currentCategory ->
                        val updatedCategory = currentCategory.copy(
                            name = name,
                            item1 = items[0],
                            item2 = items[1].takeIf { input -> input.isNotBlank() },
                            item3 = items[2].takeIf { input -> input.isNotBlank() },
                            item4 = items[3].takeIf { input -> input.isNotBlank() },
                            item5 = items[4].takeIf { input -> input.isNotBlank() }
                        )
                        viewModel.updateCategory(updatedCategory)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MainColor.value
                )
            ) {
                Text("更新")
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("カテゴリーの削除") },
            text = { Text("このカテゴリーを削除してもよろしいですか？") },
            confirmButton = {
                Button(
                    onClick = {
                        category?.let {
                            viewModel.deleteCategory(it)
                        }
                        showDeleteConfirmDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MainColor.value),
                ) {
                    Text("削除", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                ) {
                    Text("キャンセル", color = Color.White)
                }
            }
        )
    }
}