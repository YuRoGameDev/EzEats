package com.example.ezeats.storage

import androidx.room.*

@Entity(tableName = "user_data")
data class UserData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "is_logged_in") val isLoggedIn: Boolean,
    @TypeConverters(StringListConverter::class) @ColumnInfo(name = "bookmarked_urls") val bookmarkedUrls: List<String>
)
