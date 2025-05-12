package com.example.ezeats.storage

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.*
import com.example.ezeats.ezeats

object DatabaseProvider {
    var bookmarkedUrlsList: MutableList<String> = mutableListOf()
    var email: String = ""
    var password: String = ""
    var isLoggedIn: Boolean = false
    val dynamoDBHelper = DynamoDBHelper()

    //Creates a singleton used in every file
    //On first time the app is opened, a single Room for the app will be created
    //That room will be used for the entire app duration till its uninstalled
    val db: AppDatabase by lazy {
        Room.databaseBuilder(
            ezeats.Companion.context,
            AppDatabase::class.java,
            "user_database"
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

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
            .fallbackToDestructiveMigration(false)
            .build()

    }

    //This sets the user room data at the start.
    //If the user is logged in then it gets the data from AWS instead
    fun setList() {
        CoroutineScope(Dispatchers.IO).launch {
            val user = db.userDataDao().getBookmarkedUrls()
            val urls = user?.bookmarkedUrls ?: emptyList()
            bookmarkedUrlsList = urls.toMutableList()

            email = db.userDataDao().getEmail()
            password = db.userDataDao().getPassword()
            isLoggedIn = db.userDataDao().isUserLoggedIn()

            if (isLoggedIn) {
                val userAws: AWSUserData? = dynamoDBHelper.getUserDataById(email, password)
                val bookmarkedUrls: List<String> = userAws?.bookmarkedUrls ?: emptyList()
                bookmarkedUrlsList = bookmarkedUrls.toMutableList()
                db.userDataDao().updateBookmarkedUrls(bookmarkedUrlsList)
            }
        }
    }

    //Gets a list of the book
    fun getBookmarkedUrls(): List<String> {
        return bookmarkedUrlsList
    }

    //Self check a url to see if its bookmarked
    fun isBookmarked(url: String): Boolean {
        return bookmarkedUrlsList.contains(url)
    }

    //Adds a bookmark. Updates AWS if logged in
    fun addBookmark(url: String) {
        if (!bookmarkedUrlsList.contains(url)) {
            bookmarkedUrlsList.add(url)
            println("Added")
            println(bookmarkedUrlsList)
            CoroutineScope(Dispatchers.IO).launch {
                db.userDataDao().updateBookmarkedUrls(bookmarkedUrlsList)

                if (isLoggedIn) {
                    dynamoDBHelper.updateBookmarkedUrls(email, bookmarkedUrlsList)
                }
            }
        }
    }

    //Removes a bookmark. Updates AWS if logged in
    fun removeBookmark(url: String) {
        if (bookmarkedUrlsList.contains(url)) {
            bookmarkedUrlsList.remove(url)
            println("Removed")
            println(bookmarkedUrlsList)
            CoroutineScope(Dispatchers.IO).launch {
                db.userDataDao().updateBookmarkedUrls(bookmarkedUrlsList)

                if (isLoggedIn) {
                    dynamoDBHelper.updateBookmarkedUrls(email, bookmarkedUrlsList)
                }
            }
        }
    }

}