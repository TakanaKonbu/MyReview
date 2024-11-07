package com.takanakonbu.myreview.review

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun ReviewDetailScreen(
    reviewId: Int,
    onNavigateBack: () -> Unit,
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

    val review by viewModel.selectedReview.collectAsState()
    val category by viewModel.selectedCategory.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(reviewId) {
        viewModel.loadReviewById(reviewId)
    }

    fun shareReview(review: Review) {
        // レビュー内容をTwitter向けにフォーマット
        val shareText = buildString {
            append("『${review.name}』\n")
            append("総評: ${String.format(Locale.US, "%.1f", listOfNotNull(
                review.itemScore1,
                review.itemScore2,
                review.itemScore3,
                review.itemScore4,
                review.itemScore5
            ).average())}⭐️\n\n")
            if (!review.genre.isNullOrEmpty()) {
                append("ジャンル: ${review.genre}\n")
            }
            append(review.review)        }

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        context.startActivity(Intent.createChooser(intent, "レビューをシェア"))
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                FloatingActionButton(
                    onClick = { review?.let { shareReview(it) } },
                    containerColor = MainColor.value,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Share, contentDescription = "Share")
                }
                Spacer(modifier = Modifier.height(16.dp))
                FloatingActionButton(
                    onClick = { navController.navigate("edit_review/${review?.id}") },
                    containerColor = MainColor.value,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                }
                Spacer(modifier = Modifier.height(16.dp))
                FloatingActionButton(
                    onClick = { showDeleteDialog = true },
                    containerColor = MainColor.value,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                }
            }
        }
    ) { paddingValues ->
        review?.let { reviewData ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 140.dp) // FABのための余白
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
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

                    // Calculate and display average score
                    val averageScore = listOfNotNull(
                        reviewData.itemScore1,
                        reviewData.itemScore2,
                        reviewData.itemScore3,
                        reviewData.itemScore4,
                        reviewData.itemScore5
                    ).average()

                    // Display image if available
                    reviewData.image?.let { imagePath ->
                        val imageFile = File(imagePath)
                        if (imageFile.exists()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageFile)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Review Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "総評",
                            fontSize = 32.sp,
                            color = MainColor.value,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = String.format(Locale.US, "%.1f", averageScore),
                            fontSize = 32.sp,
                            color = MainColor.value,
                            textAlign = TextAlign.End
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ジャンル",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (reviewData.genre.isNullOrEmpty()) "未設定" else reviewData.genre,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.End
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "作成日",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = dateFormat.format(reviewData.createdDate),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.End
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "評価",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    category?.let { cat ->
                        listOfNotNull(
                            cat.item1 to reviewData.itemScore1,
                            cat.item2?.let { it to reviewData.itemScore2 },
                            cat.item3?.let { it to reviewData.itemScore3 },
                            cat.item4?.let { it to reviewData.itemScore4 },
                            cat.item5?.let { it to reviewData.itemScore5 }
                        ).forEach { (item, score) ->
                            score?.let {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "⭐️$it",
                                        fontSize = 20.sp,
                                        textAlign = TextAlign.End
                                    )
                                }
                                HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "レビュー",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = reviewData.review,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Delete confirmation dialog
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("レビューの削除") },
                    text = { Text("このレビューを削除してもよろしいですか？") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteReview(reviewData)
                                showDeleteDialog = false
                                onNavigateBack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MainColor.value)
                        ) {
                            Text("削除")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showDeleteDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                        ) {
                            Text("キャンセル")
                        }
                    }
                )
            }
        }
    }
}