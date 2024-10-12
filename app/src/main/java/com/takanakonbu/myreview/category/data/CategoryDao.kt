package com.takanakonbu.myreview.category.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    // 一覧表示
    @Query("SELECT * FROM categories ORDER BY createdDate ASC")
    fun getAllCategories(): Flow<List<Category>>

    // 追加
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    // 更新
    @Update
    suspend fun updateCategory(category: Category)

    // 削除
    @Delete
    suspend fun deleteCategory(category: Category)

    // ID指定で単一カテゴリー取得（必要に応じて）
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Int): Category?

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
}