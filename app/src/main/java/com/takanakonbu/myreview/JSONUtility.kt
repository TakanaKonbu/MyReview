package com.takanakonbu.myreview

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.takanakonbu.myreview.category.data.Category
import com.takanakonbu.myreview.review.data.Review
import java.util.Date

object JSONUtility {
    val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        .create()

    fun categoriesToJson(categories: List<Category>): String {
        return gson.toJson(categories)
    }

    fun reviewsToJson(reviews: List<Review>): String {
        return gson.toJson(reviews)
    }

    fun jsonToCategories(json: String): List<Category> {
        val type = object : TypeToken<List<Category>>() {}.type
        return gson.fromJson(json, type)
    }

    fun jsonToReviews(json: String): List<Review> {
        val type = object : TypeToken<List<Review>>() {}.type
        return gson.fromJson(json, type)
    }

    fun createBackupJson(categories: List<Category>, reviews: List<Review>): String {
        val backupData = mapOf(
            "categories" to categories,
            "reviews" to reviews
        )
        return gson.toJson(backupData)
    }

    fun parseBackupJson(json: String): Pair<List<Category>, List<Review>> {
        val type = object : TypeToken<Map<String, Any>>() {}.type
        val backupData: Map<String, Any> = gson.fromJson(json, type)

        val categoriesJson = gson.toJson(backupData["categories"])
        val reviewsJson = gson.toJson(backupData["reviews"])

        val categories = jsonToCategories(categoriesJson)
        val reviews = jsonToReviews(reviewsJson)

        return Pair(categories, reviews)
    }
}