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
}