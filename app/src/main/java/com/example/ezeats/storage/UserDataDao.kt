package com.example.ezeats.storage

import androidx.room.*

@Database(entities = [UserData::class], version = 1)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDataDao(): UserDataDao
}

//interface for extracting Room Data
@Dao
interface UserDataDao {
    @Insert
    suspend fun insert(user: UserData)

    @Query("UPDATE user_data SET is_logged_in = :status WHERE id = 1")
    suspend fun updateLoginStatus(status: Boolean)

    @Query("SELECT is_logged_in FROM user_data WHERE id = 1 LIMIT 1")
    suspend fun isUserLoggedIn(): Boolean


    @Query("UPDATE user_data SET email = :email, password = :password WHERE id = 1")
    suspend fun updateEmailAndPassword(email: String, password: String)

    @Query("SELECT email FROM user_data WHERE id = 1 LIMIT 1")
    suspend fun getEmail(): String

    @Query("SELECT password FROM user_data WHERE id = 1 LIMIT 1")
    suspend fun getPassword(): String

    //So there isn't a crash with getting a list, I extract the UserData object,
    //then reference the bookmarked urls
    @Query("SELECT * FROM user_data WHERE id = 1 LIMIT 1")
    suspend fun getBookmarkedUrls(): UserData?

    @Query("UPDATE user_data SET bookmarked_urls = :urls WHERE id = 1")
    suspend fun updateBookmarkedUrls(urls: List<String>)


    @Query("DELETE FROM user_data WHERE id = 1")
    suspend fun deleteUser()
}