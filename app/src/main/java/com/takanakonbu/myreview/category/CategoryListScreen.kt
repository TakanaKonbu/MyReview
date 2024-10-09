package com.takanakonbu.myreview.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.takanakonbu.myreview.category.data.Category
import com.takanakonbu.myreview.category.data.CategoryDatabase
import com.takanakonbu.myreview.category.data.CategoryRepository
import com.takanakonbu.myreview.category.ui.CategoryViewModel
import com.takanakonbu.myreview.category.ui.CategoryViewModelFactory

@Composable
fun CategoryList(
    onAddCategory: () -> Unit,
    onEditCategory: (Int) -> Unit,
    viewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModelFactory(
            CategoryRepository(
                CategoryDatabase.getDatabase(LocalContext.current).categoryDao()
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
                CategoryItem(category, onEditCategory)
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
fun CategoryItem(category: Category, onEditCategory: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onEditCategory(category.id) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${category.name}",
            fontSize = 24.sp,
        )
    }
    Divider(color = Color.LightGray, thickness = 1.dp)
}