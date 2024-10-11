package com.takanakonbu.myreview.review.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews ORDER BY createdDate DESC")
    fun getAllReviews(): Flow<List<Review>>

    @Query("SELECT * FROM reviews WHERE favorite = 1 ORDER BY createdDate DESC")
    fun getFavoriteReviews(): Flow<List<Review>>

    @Query("SELECT * FROM reviews ORDER BY createdDate ASC")
    fun getAllReviewsOldestFirst(): Flow<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)

    @Update
    suspend fun updateReview(review: Review)

    @Delete
    suspend fun deleteReview(review: Review)

    @Query("SELECT * FROM reviews WHERE id = :reviewId")
    suspend fun getReviewById(reviewId: Int): Review?

    @Query("SELECT * FROM reviews WHERE name LIKE '%' || :query || '%'")
    fun searchReviews(query: String): Flow<List<Review>>

    @Query("SELECT * FROM reviews ORDER BY (itemScore1 + itemScore2 + itemScore3 + itemScore4 + itemScore5) / 5 ASC")
    fun getReviewsSortedByRatingAsc(): Flow<List<Review>>

    @Query("SELECT * FROM reviews ORDER BY (itemScore1 + itemScore2 + itemScore3 + itemScore4 + itemScore5) / 5 DESC")
    fun getReviewsSortedByRatingDesc(): Flow<List<Review>>

    @Query("SELECT * FROM reviews WHERE categoryId = :categoryId AND name LIKE '%' || :query || '%'")
    suspend fun searchReviewsInCategory(categoryId: Int, query: String): List<Review>
}