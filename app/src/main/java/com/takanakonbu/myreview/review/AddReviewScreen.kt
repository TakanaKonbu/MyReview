package com.takanakonbu.myreview.review

import AddReviewViewModel
import AddReviewViewModelFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.takanakonbu.myreview.category.data.AppDatabase
import com.takanakonbu.myreview.category.data.CategoryRepository
import com.takanakonbu.myreview.review.data.ReviewRepository
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewScreen(
    onNavigateBack: () -> Unit,
    categoryId: Int? = null,
    categoryName: String? = null,
    reviewId: Int? = null
) {
    val context = LocalContext.current
    val viewModel: AddReviewViewModel = viewModel(
        factory = AddReviewViewModelFactory(
            context,
            CategoryRepository(AppDatabase.getDatabase(context).categoryDao()),
            ReviewRepository(AppDatabase.getDatabase(context).reviewDao())
        )
    )

    val title by viewModel.title.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val genre by viewModel.genre.collectAsState()
    val review by viewModel.review.collectAsState()
    val itemScores by viewModel.itemScores.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val imageUri by viewModel.imageUri.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()

    var expanded by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setImageUri(it.toString()) }
    }

    LaunchedEffect(reviewId, categoryId, categoryName) {
        reviewId?.let { viewModel.loadReviewForEditing(it) }
        if (reviewId == null && categoryId != null && categoryName != null) {
            viewModel.setSelectedCategoryId(categoryId)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = title,
                    onValueChange = { viewModel.setTitle(it) },
                    label = { Text("タイトル") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { viewModel.toggleFavorite() }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6D6DF6)
                )
            ) {
                Text("画像を選択")
            }

            imageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = selectedCategory?.name ?: categoryName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("カテゴリー") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                viewModel.setSelectedCategoryId(category.id)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = genre,
                onValueChange = { viewModel.setGenre(it) },
                label = { Text("ジャンル") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = review,
                onValueChange = { viewModel.setReview(it) },
                label = { Text("評価") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            selectedCategory?.let { category ->
                val items = listOfNotNull(
                    category.item1,
                    category.item2,
                    category.item3,
                    category.item4,
                    category.item5
                )

                // 総評の計算と表示
                val averageScore = if (itemScores.isNotEmpty()) {
                    itemScores.values.average().toFloat()
                } else {
                    0f
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "総評: ${String.format("%.1f", averageScore)}",
                    color = Color(0xFF6D6DF6),
                    fontSize = 32.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                items.forEach { item ->
                    val score = itemScores[item] ?: 1f
                    val roundedScore = (score * 2).roundToInt() / 2f

                    Text(text = "$item: ${String.format("%.1f", roundedScore)}")
                    Slider(
                        value = score,
                        onValueChange = { newValue ->
                            val steppedValue = (newValue * 2).roundToInt() / 2f
                            viewModel.setItemScore(item, steppedValue)
                        },
                        valueRange = 1f..5f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF6D6DF6),
                            activeTrackColor = Color(0xFF6D6DF6),
                            inactiveTrackColor = Color(0xFF6D6DF6).copy(alpha = 0.3f)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.saveReview()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6D6DF6)
                )
            ) {
                Text(if (isEditMode) "更新" else "保存")
            }
        }
    }
}