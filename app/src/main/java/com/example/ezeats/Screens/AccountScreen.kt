package com.example.ezeats.Screens

import android.provider.ContactsContract
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ezeats.storage.AWSUserData
import com.example.ezeats.storage.DatabaseProvider
import com.example.ezeats.storage.DynamoDBHelper
import kotlinx.coroutines.launch


@Composable
fun AccountScreen() {
    var createEmail by remember { mutableStateOf("") }
    var createPassword by remember { mutableStateOf("") }
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var isLoggedIn = remember { DatabaseProvider.isLoggedIn }
    val scope = rememberCoroutineScope()

    BackHandler(enabled = true) {
        // Do nothing, back is disabled
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()), // In case the content gets too long
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoggedIn) {
            Text(
                text = "Welcome, ${DatabaseProvider.email}",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(32.dp))


            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { scope.launch {
                    onLogout()
                } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text("Log Out")
            }
        } else {
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = createEmail,
                onValueChange = { createEmail = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = createPassword,
                onValueChange = { createPassword = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {scope.launch {
                    onCreateAccount(createEmail, createPassword)
                } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text("Create Account")
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = loginEmail,
                onValueChange = { loginEmail = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = loginPassword,
                onValueChange = { loginPassword = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { scope.launch {
                    onLogin(loginEmail, loginPassword)
                } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text("Login")
            }
        }
    }
}

suspend fun onCreateAccount(email: String, password: String){

    if(DatabaseProvider.dynamoDBHelper.getUserDataById(email,password) == null){
        println("Account Doesnt Exist")
        DatabaseProvider.db.userDataDao().updateLoginStatus(true)
        DatabaseProvider.db.userDataDao().updateEmailAndPassword(email,password)
        DatabaseProvider.isLoggedIn = true
        DatabaseProvider.email = email
        DatabaseProvider.password = password

        val user = AWSUserData(
            email,
            password,
            DatabaseProvider.getBookmarkedUrls()
        )

        DatabaseProvider.dynamoDBHelper.saveUserData(user)
    }else{
        println("Account Exists")
    }
}

suspend fun onLogin(email: String, password: String){
    val userData = DatabaseProvider.dynamoDBHelper.getUserDataById(email,password)
    if(userData != null){
        println("Account Exists")
        DatabaseProvider.db.userDataDao().updateLoginStatus(true)
        DatabaseProvider.db.userDataDao().updateEmailAndPassword(email,password)
        DatabaseProvider.db.userDataDao().updateBookmarkedUrls(userData.bookmarkedUrls)

        DatabaseProvider.isLoggedIn = true
        DatabaseProvider.email = email
        DatabaseProvider.password = password
        DatabaseProvider.bookmarkedUrlsList = userData.bookmarkedUrls.toMutableList()

    }else{
        println("Account Doesnt Exist")
    }
}

suspend fun onLogout(){
    DatabaseProvider.db.userDataDao().updateLoginStatus(false)
    DatabaseProvider.db.userDataDao().updateEmailAndPassword("","")
    DatabaseProvider.db.userDataDao().updateBookmarkedUrls(emptyList())

    DatabaseProvider.isLoggedIn = false
    DatabaseProvider.email = ""
    DatabaseProvider.password = ""
    DatabaseProvider.bookmarkedUrlsList = emptyList<String>().toMutableList()

    println("Logged Out")
}
