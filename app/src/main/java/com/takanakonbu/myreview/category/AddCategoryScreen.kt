package com.takanakonbu.myreview.category

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.takanakonbu.myreview.category.data.AppDatabase
import com.takanakonbu.myreview.category.data.CategoryRepository
import com.takanakonbu.myreview.review.data.ReviewRepository
import com.takanakonbu.myreview.ui.theme.MainColor

@Composable
fun AddReviewScreen(
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

    val name by viewModel.newCategoryName.collectAsState()
    val items by viewModel.newCategoryItems.collectAsState()
    val visibleItemCount by viewModel.visibleItemCount.collectAsState()

    fun deleteItem(index: Int) {
        // 最後の入力済み項目でない場合は削除可能
        if (items.count { it.isNotBlank() } > 1 || items[index].isBlank()) {
            val currentItems = items.toMutableList()

            // 対象の項目を削除し、それ以降の項目を前に詰める
            for (i in index until currentItems.lastIndex) {
                currentItems[i] = currentItems[i + 1]
            }
            currentItems[currentItems.lastIndex] = ""

            viewModel.updateNewCategoryItem(index, "")
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
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.updateNewCategoryName(it) },
                placeholder = { Text("カテゴリーを追加") },
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "評価項目の追加(最大5個)\n最低1個は必須",
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 表示するフォームの数を制御
            repeat(visibleItemCount) { index ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = items[index],
                        onValueChange = { viewModel.updateNewCategoryItem(index, it) },
                        placeholder = { Text("評価項目の追加") },
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

            // 追加ボタン
            if (visibleItemCount < 5) {
                TextButton(
                    onClick = { viewModel.addNewItem() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ 評価項目を追加", color = MainColor.value)
                }
            }

            val hasEmptyItems = items.take(visibleItemCount).any { it.isBlank() }

            if (hasEmptyItems) {
                Text(
                    text = "空白の項目があります",
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (name.isNotEmpty() && !hasEmptyItems) {
                        viewModel.insertCategory()
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotEmpty() && !hasEmptyItems,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MainColor.value,
                    disabledContainerColor = MainColor.value.copy(alpha = 0.5f)
                )
            ) {
                Text("保存", color = Color.White, fontSize = 20.sp)
            }
        }
    }
}