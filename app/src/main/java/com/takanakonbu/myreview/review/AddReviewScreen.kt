package com.takanakonbu.myreview.review

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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.takanakonbu.myreview.category.data.AppDatabase
import com.takanakonbu.myreview.category.data.CategoryRepository
import com.takanakonbu.myreview.review.data.ReviewRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddReviewViewModel = viewModel(
        factory = AddReviewViewModelFactory(
            CategoryRepository(AppDatabase.getDatabase(LocalContext.current).categoryDao()),
            ReviewRepository(AppDatabase.getDatabase(LocalContext.current).reviewDao())
        )
    )
) {
    val title by viewModel.title.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val genre by viewModel.genre.collectAsState()
    val review by viewModel.review.collectAsState()
    val itemScores by viewModel.itemScores.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val imageUri by viewModel.imageUri.collectAsState()

    var expanded by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setImageUri(it.toString()) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                    tint = if (isFavorite) Color.Black else Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 画像選択ボタンと選択された画像の表示
        Button(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
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
                value = selectedCategory?.name ?: "",
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
            listOfNotNull(
                category.item1,
                category.item2,
                category.item3,
                category.item4,
                category.item5
            ).forEach { item ->
                Text(text = "$item: ${itemScores[item]?.toInt() ?: 1}")
                Slider(
                    value = itemScores[item] ?: 1f,
                    onValueChange = { viewModel.setItemScore(item, it) },
                    valueRange = 1f..5f,
                    steps = 3,
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
            Text("保存")
        }
    }
}