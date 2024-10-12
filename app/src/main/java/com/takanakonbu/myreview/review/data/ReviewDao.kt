package com.takanakonbu.myreview.review.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    // 全てのレビューを作成日の降順で取得
    @Query("SELECT * FROM reviews ORDER BY createdDate DESC")
    fun getAllReviews(): Flow<List<Review>>

    // お気に入りのレビューを作成日の降順で取得
    @Query("SELECT * FROM reviews WHERE favorite = 1 ORDER BY createdDate DESC")
    fun getFavoriteReviews(): Flow<List<Review>>

    // 全てのレビューを作成日の昇順（古い順）で取得
    @Query("SELECT * FROM reviews ORDER BY createdDate ASC")
    fun getAllReviewsOldestFirst(): Flow<List<Review>>

    // 新しいレビューを挿入または既存のレビューを更新
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)

    // 既存のレビューを更新
    @Update
    suspend fun updateReview(review: Review)

    // レビューを削除
    @Delete
    suspend fun deleteReview(review: Review)

    // 特定のIDを持つレビューを取得
    @Query("SELECT * FROM reviews WHERE id = :reviewId")
    suspend fun getReviewById(reviewId: Int): Review?

    // レビュー名で検索
    @Query("SELECT * FROM reviews WHERE name LIKE '%' || :query || '%'")
    fun searchReviews(query: String): Flow<List<Review>>

    // レビューを平均評価の昇順でソート
    @Query("SELECT * FROM reviews ORDER BY (itemScore1 + itemScore2 + itemScore3 + itemScore4 + itemScore5) / 5 ASC")
    fun getReviewsSortedByRatingAsc(): Flow<List<Review>>

    // レビューを平均評価の降順でソート
    @Query("SELECT * FROM reviews ORDER BY (itemScore1 + itemScore2 + itemScore3 + itemScore4 + itemScore5) / 5 DESC")
    fun getReviewsSortedByRatingDesc(): Flow<List<Review>>

    // 特定のカテゴリー内でレビュー名を検索
    @Query("SELECT * FROM reviews WHERE categoryId = :categoryId AND name LIKE '%' || :query || '%'")
    suspend fun searchReviewsInCategory(categoryId: Int, query: String): List<Review>
}