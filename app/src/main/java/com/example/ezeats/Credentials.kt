package com.example.ezeats
import com.example.ezeats.BuildConfig
//API Credentials. You need to update the local.properties files with your keys
object Credentials {
    val AWS_API_KEY: String get() = BuildConfig.AWS_API_KEY
    val AWS_SECRET_KEY: String get() = BuildConfig.AWS_SECRET_KEY
    val AWS_DYNAMO_DB_TABLE: String get() = BuildConfig.AWS_DYNAMO_DB_TABLE
    val GOOGLE_CSE_ID: String get() = BuildConfig.GOOGLE_CSE_ID
    val GOOGLE_SEARCH_API_KEY: String get() = BuildConfig.GOOGLE_SEARCH_API_KEY
}
