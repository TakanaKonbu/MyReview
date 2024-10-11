package com.takanakonbu.myreview.review

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.takanakonbu.myreview.category.data.AppDatabase
import com.takanakonbu.myreview.category.data.CategoryRepository
import com.takanakonbu.myreview.review.data.ReviewRepository
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReviewDetailScreen(
    reviewId: Int,
    onNavigateBack: () -> Unit,
    viewModel: ReviewViewModel = viewModel(
        factory = ReviewViewModelFactory(
            ReviewRepository(AppDatabase.getDatabase(LocalContext.current).reviewDao()),
            CategoryRepository(AppDatabase.getDatabase(LocalContext.current).categoryDao())
        )
    )
) {
    val review by viewModel.selectedReview.collectAsState()
    val category by viewModel.selectedCategory.collectAsState()

    LaunchedEffect(reviewId) {
        viewModel.loadReviewById(reviewId)
    }

    review?.let { reviewData ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reviewData.name,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f)
                )
                if (reviewData.favorite) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "Favorite",
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            reviewData.image?.let { imageUri ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Review Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = "ジャンル: ${reviewData.genre ?: "未設定"}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            Text(
                text = "作成日: ${dateFormat.format(reviewData.createdDate)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "評価:",
                style = MaterialTheme.typography.titleMedium
            )

            category?.let { cat ->
                listOfNotNull(
                    cat.item1 to reviewData.itemScore1,
                    cat.item2?.let { it to reviewData.itemScore2 },
                    cat.item3?.let { it to reviewData.itemScore3 },
                    cat.item4?.let { it to reviewData.itemScore4 },
                    cat.item5?.let { it to reviewData.itemScore5 }
                ).forEach { (item, score) ->
                    score?.let {
                        Text(
                            text = "$item: $it",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "レビュー:",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = reviewData.review,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}