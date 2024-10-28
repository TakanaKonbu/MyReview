package com.takanakonbu.myreview.review

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.takanakonbu.myreview.category.data.AppDatabase
import com.takanakonbu.myreview.category.data.CategoryRepository
import com.takanakonbu.myreview.review.data.ReviewRepository
import com.takanakonbu.myreview.ui.theme.MainColor
import java.util.Locale
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
            context = context,
            categoryRepository = CategoryRepository(AppDatabase.getDatabase(context).categoryDao()),
            reviewRepository = ReviewRepository(AppDatabase.getDatabase(context).reviewDao())
        )
    )

    val title by viewModel.title.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
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

    LaunchedEffect(Unit) {
        if (reviewId != null) {
            viewModel.loadReviewForEditing(reviewId)
        } else if (categoryId != null) {
            viewModel.prepareNewReview(categoryId)
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
                    modifier = Modifier.weight(1f),
                    singleLine = true
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
                    containerColor = MainColor.value
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

            Box {
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
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = genre,
                onValueChange = { viewModel.setGenre(it) },
                label = { Text("ジャンル") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
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

                val averageScore = if (itemScores.isNotEmpty()) {
                    itemScores.values.average().toFloat()
                } else {
                    0f
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    // Localeを明示的に指定
                    text = "総評: ${String.format(Locale.US, "%.1f", averageScore)}",
                    color = MainColor.value,
                    fontSize = 32.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                items.forEach { item ->
                    val score = itemScores[item] ?: 1f
                    val roundedScore = (score * 2).roundToInt() / 2f

                    Text(text = "$item: ${String.format(Locale.US, "%.1f", roundedScore)}")
                    Slider(
                        value = score,
                        onValueChange = { newValue ->
                            val steppedValue = (newValue * 2).roundToInt() / 2f
                            viewModel.setItemScore(item, steppedValue)
                        },
                        valueRange = 1f..5f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = MainColor.value,
                            activeTrackColor = MainColor.value,
                            inactiveTrackColor = MainColor.value.copy(alpha = 0.3f)
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
                    containerColor = MainColor.value
                )
            ) {
                Text(if (isEditMode) "更新" else "保存")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddReviewScreenPreview() {
    AddReviewScreen(
        onNavigateBack = {},
        categoryId = 1,
        categoryName = "Sample Category",
        reviewId = null
    )
}