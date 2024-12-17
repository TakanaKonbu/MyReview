package com.takanakonbu.myreview.review

import android.app.Activity
import android.content.Context
import androidx.lifecycle.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.FullScreenContentCallback
import com.takanakonbu.myreview.category.data.Category
import com.takanakonbu.myreview.category.data.CategoryRepository
import com.takanakonbu.myreview.review.data.Review
import com.takanakonbu.myreview.review.data.ReviewRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOrder {
    NEWEST_FIRST,
    OLDEST_FIRST,
    HIGHEST_RATED,
    LOWEST_RATED
}

class ReviewViewModel(
    private val reviewRepository: ReviewRepository,
    private val categoryRepository: CategoryRepository,
    context: Context
) : ViewModel() {
    private val applicationContext = context.applicationContext

    private var _maxReviews = MutableStateFlow(5)
    val maxReviews: StateFlow<Int> = _maxReviews.asStateFlow()

    private var rewardedAd: RewardedAd? = null
    //    本番広告
//    private val adUnitId = "ca-app-pub-2836653067032260/7608512459"
    private val adUnitId = "ca-app-pub-3940256099942544/5224354917"

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST_FIRST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _showOnlyFavorites = MutableStateFlow(false)
    val showOnlyFavorites: StateFlow<Boolean> = _showOnlyFavorites.asStateFlow()

    private val _currentCategoryId = MutableStateFlow<Int?>(null)

    private val _selectedReview = MutableStateFlow<Review?>(null)
    val selectedReview: StateFlow<Review?> = _selectedReview.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    init {
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
        loadRewardedAd()
    }

    fun loadReviewsForCategory(categoryId: Int) {
        _currentCategoryId.value = categoryId
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun setShowOnlyFavorites(show: Boolean) {
        _showOnlyFavorites.value = show
    }

    fun searchReviewsInCategory(categoryId: Int, query: String) {
        viewModelScope.launch {
            _reviews.value = reviewRepository.searchReviewsInCategory(categoryId, query)
        }
    }

    fun deleteReview(review: Review) = viewModelScope.launch {
        reviewRepository.deleteReview(review)
    }

    fun loadReviewById(id: Int) {
        viewModelScope.launch {
            val review = reviewRepository.getReviewById(id)
            _selectedReview.value = review
            review?.let {
                _selectedCategory.value = categoryRepository.getCategoryById(it.categoryId)
            }
        }
    }

    private fun Review.calculateAverageScore(): Float {
        val scores = listOfNotNull(itemScore1, itemScore2, itemScore3, itemScore4, itemScore5)
        return if (scores.isNotEmpty()) scores.average().toFloat() else 0f
    }

    fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(applicationContext, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd = null
            }

            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
            }
        })
    }

    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onAdDismissed: () -> Unit) {
        rewardedAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd()
                    onAdDismissed()
                }
            }
            ad.show(activity) { _ ->
                _maxReviews.value += 2
                onRewardEarned()
            }
        } ?: run {
            loadRewardedAd()
            onAdDismissed()
        }
    }
}

class ReviewViewModelFactory(
    private val reviewRepository: ReviewRepository,
    private val categoryRepository: CategoryRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReviewViewModel(reviewRepository, categoryRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}