package com.takanakonbu.myreview.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.takanakonbu.myreview.category.data.AppDatabase
import com.takanakonbu.myreview.review.data.Review
import com.takanakonbu.myreview.review.data.ReviewRepository

@Composable
fun ReviewScreen(
    categoryId: Int,
    categoryName: String,
    onNavigateBack: () -> Unit,
    viewModel: ReviewViewModel = viewModel(
        factory = ReviewViewModelFactory(
            ReviewRepository(
                AppDatabase.getDatabase(LocalContext.current).reviewDao()
            )
        )
    )
) {
    val reviews by viewModel.reviews.collectAsState()

    LaunchedEffect(categoryId) {
        viewModel.loadReviewsForCategory(categoryId)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* クリック処理はここに追加 */ },
                containerColor = Color(0xFF6D6DF6),
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Review")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (reviews.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("このカテゴリーにはまだレビューがありません。")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(reviews) { review ->
                        ReviewItem(review)
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = review.name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "評価: ${review.itemScore1}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = review.review, style = MaterialTheme.typography.bodyMedium)
        }
    }
}