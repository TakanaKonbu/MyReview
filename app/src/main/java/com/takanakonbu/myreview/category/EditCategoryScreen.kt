package com.takanakonbu.myreview.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    var visibleItemCount by remember { mutableIntStateOf(1) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    fun updateItem(index: Int, newValue: String) {
        val currentItems = items.toMutableList().apply {
            this[index] = newValue
        }
        val (reorganizedItems, newVisibleCount) = viewModel.reorganizeAndGetItemCount(currentItems)
        items = reorganizedItems
        visibleItemCount = maxOf(visibleItemCount, newVisibleCount)
    }

    fun deleteItem(index: Int) {
        // 最後の入力済み項目でない場合は削除可能
        if (items.count { it.isNotBlank() } > 1 || items[index].isBlank()) {
            val currentItems = items.toMutableList()

            // 対象の項目を削除し、それ以降の項目を前に詰める
            for (i in index until currentItems.lastIndex) {
                currentItems[i] = currentItems[i + 1]
            }
            currentItems[currentItems.lastIndex] = ""

            items = currentItems
            // 表示数を更新（少なくとも1つは表示）
            visibleItemCount = maxOf(1, items.indexOfLast { it.isNotBlank() } + 1)
        }
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
            visibleItemCount = maxOf(1, initialVisibleCount)
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
                    unfocusedBorderColor = Color.Gray,
                    errorBorderColor = Color.Red,
                    errorSupportingTextColor = Color.Red
                ),
                singleLine = true,
                isError = name.isEmpty(),
                supportingText = {
                    if (name.isEmpty()) {
                        Text("カテゴリー名は必須です")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "評価項目(最大5個)",
                modifier = Modifier.padding(bottom = 8.dp)
            )

            repeat(visibleItemCount) { index ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = items[index],
                        onValueChange = { newValue -> updateItem(index, newValue) },
                        label = { Text("評価項目 ${index + 1}") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MainColor.value,
                            unfocusedBorderColor = Color.Gray
                        ),
                        singleLine = true
                    )

                    IconButton(
                        onClick = { deleteItem(index) },
                        modifier = Modifier.padding(start = 8.dp),
                        enabled = items.count { it.isNotBlank() } > 1 || items[index].isBlank()
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "項目を削除",
                            tint = if (items.count { it.isNotBlank() } > 1 || items[index].isBlank()) Color.Gray else Color.LightGray
                        )
                    }
                }
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

            val hasEmptyItems = items.take(visibleItemCount).any { it.isBlank() }

            if (hasEmptyItems) {
                Text(
                    text = "空白の項目があります",
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    if (name.isNotEmpty() && !hasEmptyItems) {
                        category?.let { currentCategory ->
                            val updatedCategory = currentCategory.copy(
                                name = name,
                                item1 = items[0],
                                item2 = items[1].ifBlank { null },
                                item3 = items[2].ifBlank { null },
                                item4 = items[3].ifBlank { null },
                                item5 = items[4].ifBlank { null }
                            )
                            viewModel.updateCategory(updatedCategory)
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotEmpty() && !hasEmptyItems,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MainColor.value,
                    disabledContainerColor = MainColor.value.copy(alpha = 0.5f)
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