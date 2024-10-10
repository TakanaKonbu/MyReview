package com.takanakonbu.myreview.review.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.takanakonbu.myreview.category.data.Category
import java.util.Date

@Entity(
    tableName = "reviews",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val favorite: Boolean = false,
    val image: String? = null,
    val categoryId: Int,
    val genre: String? = null,
    val review: String,
    val itemScore1: Double,
    val itemScore2: Double? = null,
    val itemScore3: Double? = null,
    val itemScore4: Double? = null,
    val itemScore5: Double? = null,
    val createdDate: Date = Date()
)