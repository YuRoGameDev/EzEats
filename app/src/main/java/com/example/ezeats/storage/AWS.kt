package com.example.ezeats.storage

import android.content.ContentValues.TAG
import android.util.Log
import com.example.ezeats.Credentials
import kotlinx.coroutines.*

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*

//User class only for AWS.
data class AWSUserData(
    val email: String,
    val password: String,
    val bookmarkedUrls: List<String>
)

//This is the entire Online component, AWS Dynamo which stores user data online
//Only thing that should be done is to encrpyt user password. Rn its only in plaintext
class DynamoDBHelper {
    val credentials = AwsBasicCredentials.create(
        Credentials.AWS_API_KEY,
        Credentials.AWS_SECRET_KEY
    )

    val sdkHttpClient: SdkHttpClient = UrlConnectionHttpClient.builder().build()

    //Builds the dynamo table
    val client = DynamoDbClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .httpClient(sdkHttpClient)
        .build()

    //Convert the UserData class to something AWS can read
    fun userDataToItem(user: AWSUserData): Map<String, AttributeValue> {
        return mapOf(
            "email" to AttributeValue.fromS(user.email),
            "password" to AttributeValue.fromS(user.password),
            "bookmarkedUrls" to AttributeValue.fromL(
                user.bookmarkedUrls.map { AttributeValue.fromS(it) }
            )
        )
    }

    //Saves the user data
    suspend fun saveUserData(user: AWSUserData) {
        withContext(Dispatchers.IO) {
            try {
                val request = PutItemRequest.builder()
                    .tableName(Credentials.AWS_DYNAMO_DB_TABLE)
                    .item(userDataToItem(user))
                    .build()

                client.putItem(request)
                Log.i(TAG, "Item saved successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving item: ${e.message}", e)
            }
        }
    }

    //Gets user data. It checks password and email
    suspend fun getUserDataById(id: String, password: String): AWSUserData? {
        return withContext(Dispatchers.IO) {
            try {
                val key = mapOf("email" to AttributeValue.fromS(id))

                val request = GetItemRequest.builder()
                    .tableName(Credentials.AWS_DYNAMO_DB_TABLE)
                    .key(key)
                    .build()

                val response = client.getItem(request)
                val item = response.item()

                if (item != null && item.isNotEmpty()) {
                    val storedPassword = item["password"]?.s() ?: ""

                    if (storedPassword == password) {
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

    //Only for updating a users bookmarked Urls. Password isn't needed
    suspend fun updateBookmarkedUrls(email: String, newUrls: List<String>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val existingUser = getUserDataById(
                    email,
                    password = ""
                )

                val existingUrls = existingUser?.bookmarkedUrls ?: emptyList()

                val mergedUrls = (existingUrls + newUrls).distinct()

                val urlsAttribute = AttributeValue.fromL(
                    mergedUrls.map { AttributeValue.fromS(it) }
                )

                val key = mapOf("email" to AttributeValue.fromS(email))
                val expressionValues = mapOf(":urls" to urlsAttribute)

                val request = UpdateItemRequest.builder()
                    .tableName(Credentials.AWS_DYNAMO_DB_TABLE)
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

