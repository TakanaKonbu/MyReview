package com.takanakonbu.myreview.review.data

import kotlinx.coroutines.flow.Flow

// ReviewRepositoryは、アプリケーションのレビューデータに関する操作を管理するクラスです。
// このクラスは、データアクセスオブジェクト（DAO）を使用してデータベース操作を抽象化します。
class ReviewRepository(private val reviewDao: ReviewDao) {

    // すべてのレビューを取得します（デフォルトの順序）
    fun getAllReviews(): Flow<List<Review>> = reviewDao.getAllReviews()

    // お気に入りのレビューのみを取得します
    fun getFavoriteReviews(): Flow<List<Review>> = reviewDao.getFavoriteReviews()

    // すべてのレビューを古い順（作成日時の昇順）で取得します
    fun getAllReviewsOldestFirst(): Flow<List<Review>> = reviewDao.getAllReviewsOldestFirst()

    // 新しいレビューをデータベースに挿入します
    suspend fun insertReview(review: Review) {
        reviewDao.insertReview(review)
    }

    // 既存のレビューを更新します
    suspend fun updateReview(review: Review) {
        reviewDao.updateReview(review)
    }

    // 指定されたレビューを削除します
    suspend fun deleteReview(review: Review) {
        reviewDao.deleteReview(review)
    }

    // 指定されたIDのレビューを取得します。存在しない場合はnullを返します。
    suspend fun getReviewById(id: Int): Review? {
        return reviewDao.getReviewById(id)
    }

    // クエリに基づいてレビューを検索します
    fun searchReviews(query: String): Flow<List<Review>> = reviewDao.searchReviews(query)

    // レビューを評価順にソートして取得します
    // ascending: trueの場合は昇順、falseの場合は降順
    fun getReviewsSortedByRating(ascending: Boolean): Flow<List<Review>> =
        if (ascending) reviewDao.getReviewsSortedByRatingAsc()
        else reviewDao.getReviewsSortedByRatingDesc()

    // 特定のカテゴリー内でクエリに基づいてレビューを検索します
    suspend fun searchReviewsInCategory(categoryId: Int, query: String): List<Review> {
        return reviewDao.searchReviewsInCategory(categoryId, query)
    }
}