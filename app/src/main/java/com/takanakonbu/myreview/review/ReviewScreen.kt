package com.takanakonbu.myreview.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.takanakonbu.myreview.category.data.AppDatabase
import com.takanakonbu.myreview.review.data.Review
import com.takanakonbu.myreview.review.data.ReviewRepository

@OptIn(ExperimentalMaterial3Api::class)
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
        topBar = {
            TopAppBar(
                title = { Text(text = "$categoryName のレビュー") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (reviews.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("このカテゴリーにはまだレビューがありません。")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(reviews) { review ->
                    ReviewItem(review)
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