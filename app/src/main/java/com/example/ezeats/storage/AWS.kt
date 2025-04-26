package com.example.ezeats.storage

import android.content.ContentValues.TAG
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest




data class AWSUserData(
    val email: String,
    val password: String,
    val bookmarkedUrls: List<String>
)

class DynamoDBHelper {
    val credentials = AwsBasicCredentials.create(
        "AKIAQ3EGSOQJHU23DYSB",
        "vPbUjwyU+OB9pm212Q1snqT7hCN0Bl/eou5HZnrt"
    )

    val sdkHttpClient: SdkHttpClient = UrlConnectionHttpClient.builder().build()

    val client =  DynamoDbClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .httpClient(sdkHttpClient)
        .build()

    fun userDataToItem(user: AWSUserData): Map<String, AttributeValue> {
        return mapOf(
            "email" to AttributeValue.fromS(user.email),
            "password" to AttributeValue.fromS(user.password),
            "bookmarkedUrls" to AttributeValue.fromL(
                user.bookmarkedUrls.map { AttributeValue.fromS(it) }
            )
        )
    }

    suspend fun saveUserData(user: AWSUserData) {
        withContext(Dispatchers.IO) {
            try {
                val request = PutItemRequest.builder()
                    .tableName("EzEatsUsers")
                    .item(userDataToItem(user))
                    .build()

                client.putItem(request)
                Log.i(TAG, "Item saved successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving item: ${e.message}", e)
            }
        }
    }

    suspend fun  getUserDataById(id: String, password: String): AWSUserData? {
        return withContext(Dispatchers.IO) {
            try {
                val key = mapOf("email" to AttributeValue.fromS(id))

                val request = GetItemRequest.builder()
                    .tableName("EzEatsUsers")
                    .key(key)
                    .build()

                val response = client.getItem(request)
                val item = response.item()

                if (item != null && item.isNotEmpty()) {
                    val storedPassword = item["password"]?.s() ?: ""

                    if (storedPassword == password) {
                        val isLoggedIn = item["isLoggedIn"]?.bool() ?: false
                        val bookmarkedUrls =
                            item["bookmarkedUrls"]?.l()?.mapNotNull { it.s() } ?: emptyList()

                        AWSUserData(
                            email = id,
                            password = storedPassword,
                            bookmarkedUrls = bookmarkedUrls
                        )
                    } else {
                        Log.w("DynamoDBHelper", "Password does not match for $id")
                        null
                    }
                } else {
                    Log.w("DynamoDBHelper", "No user found with email $id")
                    null
                }
            } catch (e: Exception) {
                Log.e("DynamoDBHelper", "Error authenticating user: ${e.message}", e)
                null
            }
        }
    }

    suspend fun updateBookmarkedUrls(
        email: String,
        newUrls: List<String>,
        tableName: String = "EzEatsUsers"
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Step 1: Fetch existing user data
                val existingUser = getUserDataById(email, password = "") // you might need to adjust password handling if needed

                val existingUrls = existingUser?.bookmarkedUrls ?: emptyList()

                // Step 2: Merge existing + new URLs
                val mergedUrls = (existingUrls + newUrls).distinct()

                // Step 3: Convert merged list to AttributeValue
                val urlsAttribute = AttributeValue.fromL(
                    mergedUrls.map { AttributeValue.fromS(it) }
                )

                val key = mapOf("email" to AttributeValue.fromS(email))
                val expressionValues = mapOf(":urls" to urlsAttribute)

                val request = UpdateItemRequest.builder()
                    .tableName(tableName)
                    .key(key)
                    .updateExpression("SET bookmarkedUrls = :urls")
                    .expressionAttributeValues(expressionValues)
                    .build()

                client.updateItem(request)

                Log.i("DynamoDBHelper", "Bookmarked URLs updated for $email")
                true
            } catch (e: Exception) {
                Log.e("DynamoDBHelper", "Failed to update bookmarked URLs: ${e.message}", e)
                false
            }
        }
    }
}

