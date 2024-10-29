package com.takanakonbu.myreview

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.takanakonbu.myreview.category.data.CategoryRepository
import com.takanakonbu.myreview.review.data.ReviewRepository
import com.takanakonbu.myreview.ui.theme.ColorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SettingsViewModel(
    context: Context,  // contextをプロパティとして保持しない
    private val categoryRepository: CategoryRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {
    // applicationContextを使用してメモリリークを防ぐ
    private val applicationContext = context.applicationContext

    private val _backupProgress = MutableStateFlow(0f)
    val backupProgress: StateFlow<Float> = _backupProgress

    private val _restoreProgress = MutableStateFlow(0f)
    val restoreProgress: StateFlow<Float> = _restoreProgress

    fun backupData(uri: Uri, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                _backupProgress.value = 0.1f
                val categories = categoryRepository.allCategories.first()
                _backupProgress.value = 0.3f
                val reviews = reviewRepository.getAllReviews().first()
                _backupProgress.value = 0.5f

                // 画像をBase64エンコード
                val reviewsWithEncodedImages = reviews.map { review ->
                    review.apply {
                        image?.let { imagePath ->
                            val file = File(imagePath)
                            if (file.exists()) {
                                val bytes = file.readBytes()
                                imageBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                            }
                        }
                    }
                }

                val backupJson = JSONUtility.createBackupJson(categories, reviewsWithEncodedImages)
                _backupProgress.value = 0.7f

                withContext(Dispatchers.IO) {
                    applicationContext.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(backupJson.toByteArray())
                    }
                }
                _backupProgress.value = 1f
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                _backupProgress.value = 0f
                onComplete(false)
            }
        }
    }

    fun restoreData(uri: Uri, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                _restoreProgress.value = 0.1f
                val jsonString = withContext(Dispatchers.IO) {
                    applicationContext.contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() }
                } ?: throw Exception("バックアップファイルの読み込みに失敗しました")
                _restoreProgress.value = 0.3f

                val (categories, reviews) = JSONUtility.parseBackupJson(jsonString)
                _restoreProgress.value = 0.5f

                withContext(Dispatchers.IO) {
                    // 既存のデータを削除
                    categoryRepository.deleteAllCategories()
                    reviewRepository.deleteAllReviews()

                    // 新しいデータを挿入
                    categories.forEach { categoryRepository.insertCategory(it) }
                    reviews.forEach { review ->
                        review.imageBase64?.let { base64 ->
                            val bytes = Base64.decode(base64, Base64.DEFAULT)
                            val file = File(applicationContext.filesDir, "review_image_${review.id}.jpg")
                            file.writeBytes(bytes)
                            review.image = file.absolutePath
                        }
                        reviewRepository.insertReview(review)
                    }
                }

                _restoreProgress.value = 1f
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                _restoreProgress.value = 0f
                onComplete(false)
            }
        }
    }

    fun updateMainColor(color: Color) {
        viewModelScope.launch {
            ColorManager.setMainColor(applicationContext, color)
        }
    }
}

class SettingsViewModelFactory(
    private val context: Context,
    private val categoryRepository: CategoryRepository,
    private val reviewRepository: ReviewRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(context, categoryRepository, reviewRepository) as T
        }
        throw IllegalArgumentException("不明なViewModelクラスです")
    }
}