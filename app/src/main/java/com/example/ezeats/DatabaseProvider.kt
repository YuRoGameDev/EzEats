package com.example.ezeats

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DatabaseProvider {

    private var bookmarkedUrlsList: MutableList<String> = mutableListOf()

    val db: AppDatabase by lazy {
        Room.databaseBuilder(
            ezeats.context,  // Use your Application context
            AppDatabase::class.java,
            "user_database"
        ).addCallback(object: RoomDatabase.Callback(){
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                // Run in a coroutine since this is a background thread
                CoroutineScope(Dispatchers.IO).launch {
                    val userDao = DatabaseProvider.db.userDataDao()

                    val defaultUser = UserData(
                        email = "",
                        password = "",
                        isLoggedIn = false,
                        bookmarkedUrls = listOf()
                    )

                    userDao.insert(defaultUser)
                }


            }
        })
            .fallbackToDestructiveMigration(false)  // Optional, specify migration strategy
            .build()

    }

    fun setList(){
        CoroutineScope(Dispatchers.IO).launch {
            val urls = db.userDataDao().getBookmarkedUrls()
            bookmarkedUrlsList = urls.toMutableList()
        }
    }

    fun getBookmarkedUrls(): List<String> {
        return bookmarkedUrlsList
    }

    fun addBookmark(url: String) {
        if (!bookmarkedUrlsList.contains(url)) {
            bookmarkedUrlsList.add(url)
            println("Added")
            println(bookmarkedUrlsList)
            CoroutineScope(Dispatchers.IO).launch {
                db.userDataDao().updateBookmarkedUrls(bookmarkedUrlsList)
            }
        }
    }

    // Function to remove a URL from the list
    fun removeBookmark(url: String) {
        if (bookmarkedUrlsList.contains(url)) {
            bookmarkedUrlsList.remove(url)
            println("Removed")
            println(bookmarkedUrlsList)
            CoroutineScope(Dispatchers.IO).launch {
                db.userDataDao().updateBookmarkedUrls(bookmarkedUrlsList)
            }
        }
    }

}