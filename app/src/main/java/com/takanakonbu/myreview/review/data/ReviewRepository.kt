package com.takanakonbu.myreview.review.data

import kotlinx.coroutines.flow.Flow

class ReviewRepository(private val reviewDao: ReviewDao) {

    fun getAllReviews(): Flow<List<Review>> = reviewDao.getAllReviews()

    fun getFavoriteReviews(): Flow<List<Review>> = reviewDao.getFavoriteReviews()

    fun getAllReviewsOldestFirst(): Flow<List<Review>> = reviewDao.getAllReviewsOldestFirst()

    suspend fun insertReview(review: Review) {
        reviewDao.insertReview(review)
    }

    suspend fun updateReview(review: Review) {
        reviewDao.updateReview(review)
    }

    suspend fun deleteReview(review: Review) {
        reviewDao.deleteReview(review)
    }

    suspend fun getReviewById(id: Int): Review? {
        return reviewDao.getReviewById(id)
    }
}