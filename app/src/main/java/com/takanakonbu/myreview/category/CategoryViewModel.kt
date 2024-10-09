package com.takanakonbu.myreview.category.ui

import androidx.lifecycle.*
import androidx.lifecycle.asLiveData
import com.takanakonbu.myreview.category.data.Category
import com.takanakonbu.myreview.category.data.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class CategoryViewModel(private val repository: CategoryRepository) : ViewModel() {

    // すべてのカテゴリーを取得
    val allCategories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // 新しいカテゴリーの入力状態を管理
    private val _newCategoryName = MutableStateFlow("")
    val newCategoryName: StateFlow<String> = _newCategoryName.asStateFlow()



    private val _newCategoryItems = MutableStateFlow(List(5) { "" })
    val newCategoryItems: StateFlow<List<String>> = _newCategoryItems.asStateFlow()

    // カテゴリーを追加
    fun insertCategory() {
        val name = _newCategoryName.value
        val items = _newCategoryItems.value

        if (name.isNotBlank() && items.any { it.isNotBlank() }) {
            val newCategory = Category(
                name = name,
                item1 = items[0],
                item2 = items[1].takeIf { it.isNotBlank() },
                item3 = items[2].takeIf { it.isNotBlank() },
                item4 = items[3].takeIf { it.isNotBlank() },
                item5 = items[4].takeIf { it.isNotBlank() },
                createdDate = Date()
            )
            viewModelScope.launch {
                repository.insertCategory(newCategory)
            }
            // 入力フィールドをクリア
            clearInputs()
        }
    }

    // カテゴリーを更新
    fun updateCategory(category: Category) = viewModelScope.launch {
        repository.updateCategory(category)
    }

    // カテゴリーを削除
    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.deleteCategory(category)
    }

    // 入力フィールドの値を更新
    fun updateNewCategoryName(name: String) {
        _newCategoryName.value = name
    }



    fun updateNewCategoryItem(index: Int, value: String) {
        _newCategoryItems.value = _newCategoryItems.value.toMutableList().apply {
            this[index] = value
        }
    }

    // 入力フィールドをクリア
    private fun clearInputs() {
        _newCategoryName.value = ""
        _newCategoryItems.value = List(5) { "" }
    }

    suspend fun getCategoryById(id: Int): Category? {
        return repository.getCategoryById(id)
    }
}

class CategoryViewModelFactory(private val repository: CategoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}