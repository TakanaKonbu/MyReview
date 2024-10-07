package com.takanakonbu.myreview.category.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val icon: String,
    val item1: String,
    val item2: String?,
    val item3: String?,
    val item4: String?,
    val item5: String?,
    val createdDate: Date
)