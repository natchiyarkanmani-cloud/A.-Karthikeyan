package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Alex Sharma",
    val gender: String = "Male",
    val age: Int = 28,
    val height: Double = 178.0, // in cm
    val weight: Double = 74.0, // in kg
    val healthIssues: String = "Gluten Intolerance, Mild Hypertension"
)
