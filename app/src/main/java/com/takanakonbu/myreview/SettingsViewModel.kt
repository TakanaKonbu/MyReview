package com.takanakonbu.myreview

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.takanakonbu.myreview.category.data.CategoryRepository
import com.takanakonbu.myreview.review.data.ReviewRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val context: Context,
    private val categoryRepository: CategoryRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

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

                val backupJson = JSONUtility.createBackupJson(categories, reviews)
                _backupProgress.value = 0.7f

                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
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
                    context.contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() }
                } ?: throw Exception("Failed to read backup file")
                _restoreProgress.value = 0.3f

                val (categories, reviews) = JSONUtility.parseBackupJson(jsonString)
                _restoreProgress.value = 0.5f

                withContext(Dispatchers.IO) {
                    // Clear existing data
                    categoryRepository.deleteAllCategories()
                    reviewRepository.deleteAllReviews()

                    // Insert new data
                    categories.forEach { categoryRepository.insertCategory(it) }
                    reviews.forEach { reviewRepository.insertReview(it) }
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

    // プログレスをリセットするメソッド
    fun resetProgress() {
        _backupProgress.value = 0f
        _restoreProgress.value = 0f
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
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}