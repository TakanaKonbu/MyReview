package com.takanakonbu.myreview.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
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
import com.takanakonbu.myreview.category.ui.CategoryViewModel
import com.takanakonbu.myreview.category.ui.CategoryViewModelFactory
import com.takanakonbu.myreview.category.ui.CategoryWithReviewCount
import com.takanakonbu.myreview.review.data.ReviewRepository

@Composable
fun CategoryList(
    onAddCategory: () -> Unit,
    onEditCategory: (Int) -> Unit,
    onCategorySelected: (Int, String) -> Unit,
    viewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModelFactory(
            CategoryRepository(AppDatabase.getDatabase(LocalContext.current).categoryDao()),
            ReviewRepository(AppDatabase.getDatabase(LocalContext.current).reviewDao())
        )
    )
) {
    val categoriesWithReviewCount by viewModel.allCategoriesWithReviewCount.collectAsState(initial = emptyList())
    var showAdDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(categoriesWithReviewCount) { categoryWithCount ->
                CategoryItem(
                    categoryWithCount = categoryWithCount,
                    onEditCategory = { onEditCategory(categoryWithCount.category.id) },
                    onCategorySelected = { onCategorySelected(categoryWithCount.category.id, categoryWithCount.category.name) }
                )
            }
        }

        FloatingActionButton(
            onClick = {
                if (categoriesWithReviewCount.size >= 3) {
                    showAdDialog = true
                } else {
                    onAddCategory()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFF6D6DF6)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "カテゴリーを追加", tint = Color.White)
        }
    }

    if (showAdDialog) {
        AlertDialog(
            onDismissRequest = { showAdDialog = false },
            title = { Text("カテゴリー枠の追加") },
            text = { Text("広告を見て1枠増加しますか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 広告視聴の処理をここに追加（今回は何もしない）
                        showAdDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD27778))
                ) {
                    Text("視聴する")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAdDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF6D6DF6))
                ) {
                    Text("キャンセル")
                }
            }
        )
    }
}

@Composable
fun CategoryItem(
    categoryWithCount: CategoryWithReviewCount,
    onEditCategory: () -> Unit,
    onCategorySelected: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCategorySelected)
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = categoryWithCount.category.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "(${categoryWithCount.reviewCount})",
                fontSize = 18.sp,
                color = Color.Gray,
                modifier = Modifier.padding(end = 8.dp)
            )
            IconButton(onClick = onEditCategory) {
                Icon(Icons.Filled.Edit, contentDescription = "カテゴリーを編集")
            }
        }
        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
    }
}
