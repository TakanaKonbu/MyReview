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

    fun searchReviews(query: String): Flow<List<Review>> = reviewDao.searchReviews(query)

    fun getReviewsSortedByRating(ascending: Boolean): Flow<List<Review>> =
        if (ascending) reviewDao.getReviewsSortedByRatingAsc()
        else reviewDao.getReviewsSortedByRatingDesc()

    suspend fun searchReviewsInCategory(categoryId: Int, query: String): List<Review> {
        return reviewDao.searchReviewsInCategory(categoryId, query)
    }
}