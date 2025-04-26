package com.example.ezeats.storage

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ezeats.ezeats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DatabaseProvider {

    var bookmarkedUrlsList: MutableList<String> = mutableListOf()
    var email: String = ""
    var password: String = ""
    var isLoggedIn: Boolean = false
    val dynamoDBHelper = DynamoDBHelper()

    val db: AppDatabase by lazy {
        Room.databaseBuilder(
            ezeats.Companion.context,  // Use your Application context
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
            val user = db.userDataDao().getBookmarkedUrls()
            val urls = user?.bookmarkedUrls ?: emptyList()
            bookmarkedUrlsList = urls.toMutableList()

            email = db.userDataDao().getEmail()
            password = db.userDataDao().getPassword()
            isLoggedIn = db.userDataDao().isUserLoggedIn()

            if(isLoggedIn){
                val userAws: AWSUserData? = dynamoDBHelper.getUserDataById(email, password)
                val bookmarkedUrls: List<String> = userAws?.bookmarkedUrls ?: emptyList()
                bookmarkedUrlsList = bookmarkedUrls.toMutableList()
                db.userDataDao().updateBookmarkedUrls(bookmarkedUrlsList)
            }
        }
    }

    fun getBookmarkedUrls(): List<String> {
        println(bookmarkedUrlsList)
        return bookmarkedUrlsList
    }

    fun isBookmarked(url:String): Boolean{
        return bookmarkedUrlsList.contains(url)
    }

    fun addBookmark(url: String) {
        if (!bookmarkedUrlsList.contains(url)) {
            bookmarkedUrlsList.add(url)
            println("Added")
            println(bookmarkedUrlsList)
            CoroutineScope(Dispatchers.IO).launch {
                db.userDataDao().updateBookmarkedUrls(bookmarkedUrlsList)

                if(isLoggedIn){
                   dynamoDBHelper.updateBookmarkedUrls(email, bookmarkedUrlsList)
                }
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

                if(isLoggedIn){
                    dynamoDBHelper.updateBookmarkedUrls(email, bookmarkedUrlsList)
                }
            }
        }
    }

}