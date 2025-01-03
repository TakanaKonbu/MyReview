package com.takanakonbu.myreview.review

import android.app.Activity
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.takanakonbu.myreview.category.data.AppDatabase
import com.takanakonbu.myreview.category.data.CategoryRepository
import com.takanakonbu.myreview.review.data.Review
import com.takanakonbu.myreview.review.data.ReviewRepository
import com.takanakonbu.myreview.ui.theme.MainColor
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReviewListScreen(
    categoryId: Int,
    categoryName: String,
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: ReviewViewModel = viewModel(
        factory = ReviewViewModelFactory(
            ReviewRepository(AppDatabase.getDatabase(context).reviewDao()),
            CategoryRepository(AppDatabase.getDatabase(context).categoryDao()),
            context
        )
    )

    val reviews by viewModel.reviews.collectAsState()
    val maxReviews by viewModel.maxReviews.collectAsState()
    var showSortDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var showAdDialog by remember { mutableStateOf(false) }
    var showRewardEarnedDialog by remember { mutableStateOf(false) }
    val sortOrder by viewModel.sortOrder.collectAsState()
    val showOnlyFavorites by viewModel.showOnlyFavorites.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(categoryId) {
        viewModel.loadReviewsForCategory(categoryId)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = { showSearchDialog = true },
                    containerColor = MainColor.value,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Search, contentDescription = "検索")
                }
                Spacer(modifier = Modifier.height(16.dp))
                FloatingActionButton(
                    onClick = { showSortDialog = true },
                    containerColor = MainColor.value,
                    contentColor = Color.White
                ) {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "並び替え")
                }
                Spacer(modifier = Modifier.height(16.dp))
                FloatingActionButton(
                    onClick = {
                        if (reviews.size >= maxReviews) {
                            showAdDialog = true
                        } else {
                            navController.navigate("add_review/$categoryId/${Uri.encode(categoryName)}")
                        }
                    },
                    containerColor = MainColor.value,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "レビュー追加")
                }
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
                        ReviewListItem(
                            review = review,
                            onClick = {
                                navController.navigate("review_detail/${review.id}")
                            }
                        )
                    }
                }
            }
        }
    }

    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text("レビューの並び替え") },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = sortOrder == SortOrder.NEWEST_FIRST,
                            onClick = {
                                viewModel.setSortOrder(SortOrder.NEWEST_FIRST)
                                showSortDialog = false
                            }
                        )
                        Text("新しい順")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = sortOrder == SortOrder.OLDEST_FIRST,
                            onClick = {
                                viewModel.setSortOrder(SortOrder.OLDEST_FIRST)
                                showSortDialog = false
                            }
                        )
                        Text("古い順")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = sortOrder == SortOrder.HIGHEST_RATED,
                            onClick = {
                                viewModel.setSortOrder(SortOrder.HIGHEST_RATED)
                                showSortDialog = false
                            }
                        )
                        Text("評価の高い順")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = sortOrder == SortOrder.LOWEST_RATED,
                            onClick = {
                                viewModel.setSortOrder(SortOrder.LOWEST_RATED)
                                showSortDialog = false
                            }
                        )
                        Text("評価の低い順")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = showOnlyFavorites,
                            onCheckedChange = {
                                viewModel.setShowOnlyFavorites(it)
                                showSortDialog = false
                            }
                        )
                        Text("お気に入りのみ表示")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortDialog = false }) {
                    Text("閉じる")
                }
            }
        )
    }

    if (showSearchDialog) {
        AlertDialog(
            onDismissRequest = { showSearchDialog = false },
            title = { Text("レビュー検索") },
            text = {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("レビュー名") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.searchReviewsInCategory(categoryId, searchQuery)
                        showSearchDialog = false
                    }
                ) {
                    Text("検索")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSearchDialog = false }
                ) {
                    Text("キャンセル")
                }
            }
        )
    }

    if (showAdDialog) {
        AlertDialog(
            onDismissRequest = { showAdDialog = false },
            title = { Text("レビュー枠の追加") },
            text = { Text("広告を見て2枠増加しますか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val activity = context as? Activity
                        activity?.let {
                            viewModel.showRewardedAd(
                                activity = it,
                                onRewardEarned = {
                                    showRewardEarnedDialog = true
                                },
                                onAdDismissed = {
                                    showAdDialog = false
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MainColor.value)
                ) {
                    Text("視聴する")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAdDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                ) {
                    Text("キャンセル")
                }
            }
        )
    }

    if (showRewardEarnedDialog) {
        AlertDialog(
            onDismissRequest = { showRewardEarnedDialog = false },
            title = { Text("レビュー枠が増加しました") },
            text = { Text("レビュー枠が3つ増加しました。新しいレビューを追加できます。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRewardEarnedDialog = false
                        navController.navigate("add_review/$categoryId/${Uri.encode(categoryName)}")
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MainColor.value)
                ) {
                    Text("レビューを追加")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRewardEarnedDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MainColor.value)
                ) {
                    Text("閉じる")
                }
            }
        )
    }
}

@Composable
fun ReviewListItem(review: Review, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            review.image?.let { imagePath ->
                val imageFile = File(imagePath)
                if (imageFile.exists()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageFile)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Review Thumbnail",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Review details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = review.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    if (review.favorite) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = "お気に入り",
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                val averageScore = listOfNotNull(
                    review.itemScore1,
                    review.itemScore2,
                    review.itemScore3,
                    review.itemScore4,
                    review.itemScore5
                ).average()

                Text(
                    text = "総評: ${String.format(Locale.US, "%.1f", averageScore)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MainColor.value
                )

                Spacer(modifier = Modifier.height(4.dp))
                val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                Text(
                    text = dateFormat.format(review.createdDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}