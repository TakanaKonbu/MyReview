package com.takanakonbu.myreview.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.takanakonbu.myreview.category.data.AppDatabase
import com.takanakonbu.myreview.review.data.Review
import com.takanakonbu.myreview.review.data.ReviewRepository

@Composable
fun ReviewScreen(
    categoryId: Int,
    categoryName: String,
    onNavigateBack: () -> Unit,
    navController: NavController,
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
                onClick = { navController.navigate("add_review") },
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                if (review.favorite) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "Favorite",
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 総評の計算と表示
            val averageScore = listOfNotNull(
                review.itemScore1,
                review.itemScore2,
                review.itemScore3,
                review.itemScore4,
                review.itemScore5
            ).average()
            Text(
                text = "総評: ${String.format("%.1f", averageScore)}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF6D6DF6)
            )

            Spacer(modifier = Modifier.height(4.dp))

        }
    }
}