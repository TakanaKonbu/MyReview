package com.takanakonbu.myreview.category

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
    context: Context
) : ViewModel() {
    private val applicationContext = context.applicationContext

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

    private val _visibleItemCount = MutableStateFlow(1)
    val visibleItemCount: StateFlow<Int> = _visibleItemCount.asStateFlow()

    private fun reorganizeItems(items: List<String>): List<String> {
        // 空の文字列も保持したまま返す
        return items.take(5).toMutableList().apply {
            // リストのサイズが5未満の場合、空文字列で埋める
            while (size < 5) {
                add("")
            }
        }
    }

    fun insertCategory() {
        val name = _newCategoryName.value
        val items = _newCategoryItems.value

        viewModelScope.launch {
            val currentCategoryCount = allCategoriesWithReviewCount.value.size
            if (currentCategoryCount < _maxCategories.value && name.isNotBlank() && items.any { it.isNotBlank() }) {
                val newCategory = Category(
                    name = name,
                    item1 = items[0],
                    item2 = items[1].ifBlank { null },
                    item3 = items[2].ifBlank { null },
                    item4 = items[3].ifBlank { null },
                    item5 = items[4].ifBlank { null },
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
        val currentItems = _newCategoryItems.value.toMutableList().apply {
            this[index] = value
        }
        _newCategoryItems.value = reorganizeItems(currentItems)

        // 表示する項目数を更新
        // 最低1つは表示し、その後は最後に入力された項目まで表示する
        _visibleItemCount.value = maxOf(1, currentItems.indexOfLast { it.isNotBlank() } + 1)
    }

    fun addNewItem() {
        if (_visibleItemCount.value < 5) {
            _visibleItemCount.value += 1
        }
    }

    fun reorganizeAndGetItemCount(items: List<String>): Pair<List<String>, Int> {
        val reorganizedItems = reorganizeItems(items)
        // 少なくとも1つは表示し、その後は最後に入力された項目まで表示
        val visibleCount = maxOf(1, items.indexOfLast { it.isNotBlank() } + 1)
        return Pair(reorganizedItems, visibleCount)
    }

    private fun clearInputs() {
        _newCategoryName.value = ""
        _newCategoryItems.value = List(5) { "" }
        _visibleItemCount.value = 1  // リセット時に1つに戻す
    }

    suspend fun getCategoryById(id: Int): Category? {
        return categoryRepository.getCategoryById(id)
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