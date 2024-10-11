import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.takanakonbu.myreview.category.data.Category
import com.takanakonbu.myreview.category.data.CategoryRepository
import com.takanakonbu.myreview.review.data.Review
import com.takanakonbu.myreview.review.data.ReviewRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.UUID

class AddReviewViewModel(
    private val context: Context,
    private val categoryRepository: CategoryRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _genre = MutableStateFlow("")
    val genre: StateFlow<String> = _genre.asStateFlow()

    private val _review = MutableStateFlow("")
    val review: StateFlow<String> = _review.asStateFlow()

    private val _itemScores = MutableStateFlow<Map<String, Float>>(emptyMap())
    val itemScores: StateFlow<Map<String, Float>> = _itemScores.asStateFlow()

    private val _imageUri = MutableStateFlow<String?>(null)
    val imageUri: StateFlow<String?> = _imageUri.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private var editingReviewId: Int? = null

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.allCategories.collect { categories ->
                _categories.value = categories
                if (categories.isNotEmpty() && _selectedCategoryId.value == null) {
                    setSelectedCategoryId(categories.first().id)
                }
            }
        }
    }

    fun setTitle(title: String) {
        _title.value = title
    }

    fun toggleFavorite() {
        _isFavorite.value = !_isFavorite.value
    }

    fun setSelectedCategoryId(id: Int) {
        _selectedCategoryId.value = id
        viewModelScope.launch {
            _selectedCategory.value = categoryRepository.getCategoryById(id)
            updateItemScores(_selectedCategory.value ?: return@launch)
        }
    }

    fun setGenre(genre: String) {
        _genre.value = genre
    }

    fun setReview(review: String) {
        _review.value = review
    }

    fun setItemScore(item: String, score: Float) {
        _itemScores.value = _itemScores.value.toMutableMap().apply { this[item] = score }
    }

    fun setImageUri(uri: String?) {
        _imageUri.value = uri
    }

    private fun updateItemScores(category: Category) {
        _itemScores.value = listOfNotNull(
            category.item1 to 3f,
            category.item2?.let { it to 3f },
            category.item3?.let { it to 3f },
            category.item4?.let { it to 3f },
            category.item5?.let { it to 3f }
        ).toMap()
    }

    fun loadReviewForEditing(reviewId: Int) {
        viewModelScope.launch {
            val review = reviewRepository.getReviewById(reviewId)
            review?.let {
                _isEditMode.value = true
                editingReviewId = it.id
                _title.value = it.name
                _isFavorite.value = it.favorite
                _selectedCategoryId.value = it.categoryId
                _genre.value = it.genre ?: ""
                _review.value = it.review
                _imageUri.value = it.image

                // Load category and update item scores
                _selectedCategory.value = categoryRepository.getCategoryById(it.categoryId)
                updateItemScores(_selectedCategory.value ?: return@launch, review)
            }
        }
    }

    private fun updateItemScores(category: Category, review: Review) {
        _itemScores.value = buildMap {
            put(category.item1, review.itemScore1.toFloat())
            category.item2?.let { put(it, review.itemScore2?.toFloat() ?: 3f) }
            category.item3?.let { put(it, review.itemScore3?.toFloat() ?: 3f) }
            category.item4?.let { put(it, review.itemScore4?.toFloat() ?: 3f) }
            category.item5?.let { put(it, review.itemScore5?.toFloat() ?: 3f) }
        }
    }

    private suspend fun saveImageToInternalStorage(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileName = "review_image_${UUID.randomUUID()}.jpg"
                val outputFile = File(context.filesDir, fileName)
                inputStream?.use { input ->
                    FileOutputStream(outputFile).use { output ->
                        input.copyTo(output)
                    }
                }
                outputFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun saveReview() {
        viewModelScope.launch {
            val imagePath = imageUri.value?.let { uri ->
                saveImageToInternalStorage(Uri.parse(uri))
            }

            val selectedCategory = _selectedCategory.value ?: return@launch
            val review = Review(
                id = editingReviewId ?: 0,
                name = title.value,
                favorite = isFavorite.value,
                image = imagePath,
                categoryId = selectedCategory.id,
                genre = genre.value,
                review = review.value,
                itemScore1 = itemScores.value[selectedCategory.item1]?.toDouble() ?: 0.0,
                itemScore2 = selectedCategory.item2?.let { itemScores.value[it]?.toDouble() },
                itemScore3 = selectedCategory.item3?.let { itemScores.value[it]?.toDouble() },
                itemScore4 = selectedCategory.item4?.let { itemScores.value[it]?.toDouble() },
                itemScore5 = selectedCategory.item5?.let { itemScores.value[it]?.toDouble() },
                createdDate = Date()
            )

            if (_isEditMode.value) {
                reviewRepository.updateReview(review)
            } else {
                reviewRepository.insertReview(review)
            }
        }
    }
}

class AddReviewViewModelFactory(
    private val context: Context,
    private val categoryRepository: CategoryRepository,
    private val reviewRepository: ReviewRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddReviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddReviewViewModel(context, categoryRepository, reviewRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}