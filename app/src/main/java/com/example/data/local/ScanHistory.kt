package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val barcode: String,
    val productName: String,
    val fssaiRating: String,
    val preservatives: String,
    val addedIngredients: String,
    val timestamp: Long = System.currentTimeMillis()
)
