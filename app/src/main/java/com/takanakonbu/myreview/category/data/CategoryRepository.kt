package com.takanakonbu.myreview.category.data

import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    // すべてのカテゴリーを取得
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    // カテゴリーを追加
    suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category)
    }

    // カテゴリーを更新
    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }

    // カテゴリーを削除
    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    // ID指定でカテゴリーを取得
    suspend fun getCategoryById(id: Int): Category? {
        return categoryDao.getCategoryById(id)
    }
}