package com.example.ezeats

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update

@Database(entities = [UserData::class], version = 1)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDataDao(): UserDataDao
}

@Dao
interface UserDataDao {
    // Insert or replace the user data (email, password, login status, and bookmarked URLs)
    @Insert
    suspend fun insert(user: UserData)

    // Update the user's login status (since there's only one user, no need for email or id)
    @Query("UPDATE user_data SET is_logged_in = :status WHERE id = 1")
    suspend fun updateLoginStatus(status: Boolean)

    // Get the user's login status
    @Query("SELECT is_logged_in FROM user_data WHERE id = 1 LIMIT 1")
    suspend fun isUserLoggedIn(): Boolean

    // Update the user's email and password (since there's only one user, no need for email or id)
    @Query("UPDATE user_data SET email = :email, password = :password WHERE id = 1")
    suspend fun updateEmailAndPassword(email: String, password: String)

    // Get the user's email
    @Query("SELECT email FROM user_data WHERE id = 1 LIMIT 1")
    suspend fun getEmail(): String

    // Get the user's password
    @Query("SELECT password FROM user_data WHERE id = 1 LIMIT 1")
    suspend fun getPassword(): String

    // Get the user's bookmarked URLs
    @Query("SELECT bookmarked_urls FROM user_data WHERE id = 1 LIMIT 1")
    suspend fun getBookmarkedUrls(): List<String>

    // Update user's bookmarked URLs
    @Query("UPDATE user_data SET bookmarked_urls = :urls WHERE id = 1")
    suspend fun updateBookmarkedUrls(urls: List<String>)

    // Delete the user data (although you mentioned there's only one user)
    @Query("DELETE FROM user_data WHERE id = 1")
    suspend fun deleteUser()
}