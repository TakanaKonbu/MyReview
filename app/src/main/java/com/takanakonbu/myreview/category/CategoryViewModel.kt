package com.takanakonbu.myreview.category.ui

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
import com.takanakonbu.myreview.review.data.ReviewRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

class CategoryViewModel(
    private val categoryRepository: CategoryRepository,
    private val reviewRepository: ReviewRepository,
    private val context: Context
) : ViewModel() {

    private var _maxCategories = MutableStateFlow(3)
    val maxCategories: StateFlow<Int> = _maxCategories.asStateFlow()

    private var rewardedAd: RewardedAd? = null
//    本番広告
//    private val adUnitId = "ca-app-pub-2836653067032260/7608512459"
    private val adUnitId = "ca-app-pub-3940256099942544/5224354917"

    val allCategoriesWithReviewCount: StateFlow<List<CategoryWithReviewCount>> = categoryRepository.allCategories
        .map { categories ->
            categories.map { category ->
                CategoryWithReviewCount(
                    category = category,
                    reviewCount = reviewRepository.getReviewCountForCategory(category.id)
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _newCategoryName = MutableStateFlow("")
    val newCategoryName: StateFlow<String> = _newCategoryName.asStateFlow()

    private val _newCategoryItems = MutableStateFlow(List(5) { "" })
    val newCategoryItems: StateFlow<List<String>> = _newCategoryItems.asStateFlow()

    fun insertCategory() {
        val name = _newCategoryName.value
        val items = _newCategoryItems.value

        viewModelScope.launch {
            val currentCategoryCount = allCategoriesWithReviewCount.value.size
            if (currentCategoryCount < _maxCategories.value && name.isNotBlank() && items.any { it.isNotBlank() }) {
                val newCategory = Category(
                    name = name,
                    item1 = items[0],
                    item2 = items[1].takeIf { it.isNotBlank() },
                    item3 = items[2].takeIf { it.isNotBlank() },
                    item4 = items[3].takeIf { it.isNotBlank() },
                    item5 = items[4].takeIf { it.isNotBlank() },
                    createdDate = Date()
                )
                categoryRepository.insertCategory(newCategory)
                clearInputs()
            }
        }
    }

    fun updateCategory(category: Category) = viewModelScope.launch {
        categoryRepository.updateCategory(category)
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        categoryRepository.deleteCategory(category)
    }

    fun updateNewCategoryName(name: String) {
        _newCategoryName.value = name
    }

    fun updateNewCategoryItem(index: Int, value: String) {
        _newCategoryItems.value = _newCategoryItems.value.toMutableList().apply {
            this[index] = value
        }
    }

    private fun clearInputs() {
        _newCategoryName.value = ""
        _newCategoryItems.value = List(5) { "" }
    }

    suspend fun getCategoryById(id: Int): Category? {
        return categoryRepository.getCategoryById(id)
    }

    fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
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
            ad.show(activity) { rewardItem ->
                _maxCategories.value += 1
                onRewardEarned()
            }
        } ?: run {
            loadRewardedAd()
            onAdDismissed()
        }
    }
}

data class CategoryWithReviewCount(
    val category: Category,
    val reviewCount: Int
)

class CategoryViewModelFactory(
    private val categoryRepository: CategoryRepository,
    private val reviewRepository: ReviewRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryViewModel(categoryRepository, reviewRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}