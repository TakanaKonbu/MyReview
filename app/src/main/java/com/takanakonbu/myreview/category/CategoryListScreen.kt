package com.takanakonbu.myreview.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.takanakonbu.myreview.category.data.AppDatabase
import com.takanakonbu.myreview.category.data.Category
import com.takanakonbu.myreview.category.data.CategoryRepository
import com.takanakonbu.myreview.category.ui.CategoryViewModel
import com.takanakonbu.myreview.category.ui.CategoryViewModelFactory

@Composable
fun CategoryList(
    onAddCategory: () -> Unit,
    onEditCategory: (Int) -> Unit,
    onCategorySelected: (Int, String) -> Unit,
    viewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModelFactory(
            CategoryRepository(
                AppDatabase.getDatabase(LocalContext.current).categoryDao()
            )
        )
    )
) {
    val categories by viewModel.allCategories.collectAsState(initial = emptyList())

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(categories) { category ->
                CategoryItem(
                    category = category,
                    onEditCategory = { onEditCategory(category.id) },
                    onCategorySelected = { onCategorySelected(category.id, category.name) }
                )
            }
        }

        FloatingActionButton(
            onClick = onAddCategory,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFF6D6DF6)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Category", tint = Color.White)
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
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
                text = category.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onEditCategory) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit Category")
            }
        }

        // 評価項目の表示
        category.item1.let { Text(text = "- $it", fontSize = 14.sp) }
        category.item2?.let { Text(text = "- $it", fontSize = 14.sp) }
        category.item3?.let { Text(text = "- $it", fontSize = 14.sp) }
        category.item4?.let { Text(text = "- $it", fontSize = 14.sp) }
        category.item5?.let { Text(text = "- $it", fontSize = 14.sp) }
    }
    Divider(color = Color.LightGray, thickness = 1.dp)
}

@Preview(showBackground = true)
@Composable
fun CategoryListPreview() {
    CategoryList(
        onAddCategory = {},
        onEditCategory = {},
        onCategorySelected = { _, _ -> }
    )
}