import androidx.lifecycle.*
import com.takanakonbu.myreview.category.data.Category
import com.takanakonbu.myreview.category.data.CategoryRepository
import com.takanakonbu.myreview.review.data.Review
import com.takanakonbu.myreview.review.data.ReviewRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// レビューのソート順を定義する列挙型
enum class SortOrder {
    NEWEST_FIRST,
    OLDEST_FIRST,
    HIGHEST_RATED,
    LOWEST_RATED
}

// レビュー関連の操作と状態を管理するViewModel
class ReviewViewModel(
    private val reviewRepository: ReviewRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    // レビューリストの状態
    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    // 現在のソート順の状態
    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST_FIRST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    // お気に入りのみ表示するかどうかの状態
    private val _showOnlyFavorites = MutableStateFlow(false)
    val showOnlyFavorites: StateFlow<Boolean> = _showOnlyFavorites.asStateFlow()

    // 現在選択されているカテゴリーIDの状態
    private val _currentCategoryId = MutableStateFlow<Int?>(null)

    // 選択されているレビューの状態
    private val _selectedReview = MutableStateFlow<Review?>(null)
    val selectedReview: StateFlow<Review?> = _selectedReview.asStateFlow()

    // 選択されているカテゴリーの状態
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    init {
        // 初期化時にレビューリストを取得し、フィルタリングとソートを適用
        viewModelScope.launch {
            combine(
                reviewRepository.getAllReviews(),
                _sortOrder,
                _showOnlyFavorites,
                _currentCategoryId
            ) { reviews, sortOrder, showOnlyFavorites, categoryId ->
                reviews.filter { review ->
                    (categoryId == null || review.categoryId == categoryId) &&
                            (!showOnlyFavorites || review.favorite)
                }.let { filteredReviews ->
                    when (sortOrder) {
                        SortOrder.NEWEST_FIRST -> filteredReviews.sortedByDescending { it.createdDate }
                        SortOrder.OLDEST_FIRST -> filteredReviews.sortedBy { it.createdDate }
                        SortOrder.HIGHEST_RATED -> filteredReviews.sortedByDescending { it.calculateAverageScore() }
                        SortOrder.LOWEST_RATED -> filteredReviews.sortedBy { it.calculateAverageScore() }
                    }
                }
            }.collect { sortedAndFilteredReviews ->
                _reviews.value = sortedAndFilteredReviews
            }
        }
    }

    // 指定されたカテゴリーIDのレビューを読み込む
    fun loadReviewsForCategory(categoryId: Int) {
        _currentCategoryId.value = categoryId
    }

    // ソート順を設定する
    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    // お気に入りのみ表示するかどうかを設定する
    fun setShowOnlyFavorites(show: Boolean) {
        _showOnlyFavorites.value = show
    }

    // レビューを検索する
    fun searchReviews(query: String) {
        viewModelScope.launch {
            _reviews.value = reviewRepository.searchReviews(query).first()
        }
    }

    // 指定されたカテゴリー内でレビューを検索する
    fun searchReviewsInCategory(categoryId: Int, query: String) {
        viewModelScope.launch {
            _reviews.value = reviewRepository.searchReviewsInCategory(categoryId, query)
        }
    }

    // 新しいレビューを挿入する
    fun insertReview(review: Review) = viewModelScope.launch {
        reviewRepository.insertReview(review)
    }

    // レビューを更新する
    fun updateReview(review: Review) = viewModelScope.launch {
        reviewRepository.updateReview(review)
    }

    // レビューを削除する
    fun deleteReview(review: Review) = viewModelScope.launch {
        reviewRepository.deleteReview(review)
    }

    // 指定されたIDのレビューを読み込む
    fun loadReviewById(id: Int) {
        viewModelScope.launch {
            val review = reviewRepository.getReviewById(id)
            _selectedReview.value = review
            review?.let {
                _selectedCategory.value = categoryRepository.getCategoryById(it.categoryId)
            }
        }
    }

    // レビューの平均スコアを計算する
    private fun Review.calculateAverageScore(): Float {
        val scores = listOfNotNull(itemScore1, itemScore2, itemScore3, itemScore4, itemScore5)
        return if (scores.isNotEmpty()) scores.average().toFloat() else 0f
    }
}

// ReviewViewModelのファクトリークラス
class ReviewViewModelFactory(
    private val reviewRepository: ReviewRepository,
    private val categoryRepository: CategoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReviewViewModel(reviewRepository, categoryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}