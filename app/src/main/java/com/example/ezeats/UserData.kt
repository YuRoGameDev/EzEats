package com.example.ezeats

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "user_data")
data class UserData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "is_logged_in") val isLoggedIn: Boolean,
    @TypeConverters(StringListConverter::class)@ColumnInfo(name = "bookmarked_urls") val bookmarkedUrls: List<String>
)
