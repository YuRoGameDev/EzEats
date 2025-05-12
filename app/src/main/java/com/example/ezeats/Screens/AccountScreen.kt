package com.example.ezeats.Screens

import android.content.res.Configuration

import androidx.activity.compose.BackHandler

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.ezeats.storage.AWSUserData
import com.example.ezeats.storage.DatabaseProvider
import com.example.ezeats.ui.theme.darkGreen

import kotlinx.coroutines.launch

//This handles the entire account page
@Composable
fun AccountScreen() {
    var isWrong by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }
    val viewModel: AccountView = viewModel()
    var isLoading by remember { mutableStateOf(false) }
    BackHandler(enabled = true) {}
    //This reloads the entire page
    key(refreshTrigger) {
        var createEmail by remember { mutableStateOf("") }
        var createPassword by remember { mutableStateOf("") }
        var loginEmail by remember { mutableStateOf("") }
        var loginPassword by remember { mutableStateOf("") }
        var isLoggedIn = remember { DatabaseProvider.isLoggedIn }

        //While the backend processes, a loading screen would be showed
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //Only shown when the user is logged in
                if (isLoggedIn) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        //A simple welcome message
                        Text(
                            text = "Welcome\n ${DatabaseProvider.email}",
                            style = MaterialTheme.typography.headlineLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 16.dp),
                            fontSize = 40.sp
                        )
                        //Logout button
                        Button(
                            onClick = {
                                viewModel.logoutAccount(
                                    onLoading = { isLoading = it },
                                    onFinished = { refreshTrigger++ })
                            },
                            modifier = Modifier
                                .width(250.dp) // fixed width!
                                .height(60.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = darkGreen,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Log Out",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                fontSize = 32.sp
                            )

                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Just a fun thing for the user
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            Text(
                                text = "Fun Fact: You've got ${DatabaseProvider.bookmarkedUrlsList.size} bookmarks!",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    val configuration = LocalConfiguration.current
                    val isLandscape =
                        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

                    //This shows the Account and login section when user is not logged in
                    //It is custom designed to account for orientation
                    if (isLandscape) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(32.dp)
                        ) {
                            CreateAccountSection(
                                createEmail,
                                createPassword,
                                onCreateEmailChange = { createEmail = it },
                                onCreatePasswordChange = { createPassword = it },
                                modifier = Modifier.weight(1f),
                                true,
                                viewModel = viewModel,
                                loadingChange = { isLoading = it },
                                refreshChange = {
                                    if (it) {
                                        refreshTrigger++
                                    } else {
                                        isWrong = true
                                    }
                                }
                            )
                            LoginSection(
                                loginEmail,
                                loginPassword,
                                onLoginEmailChange = { loginEmail = it },
                                onLoginPasswordChange = { loginPassword = it },
                                modifier = Modifier.weight(1f),
                                true,
                                viewModel = viewModel,
                                loadingChange = { isLoading = it },
                                refreshChange = {
                                    if (it) {
                                        refreshTrigger++
                                    } else {
                                        isWrong = true
                                    }
                                }
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(48.dp) // Bigger space between sections
                        ) {
                            CreateAccountSection(
                                createEmail,
                                createPassword,
                                onCreateEmailChange = { createEmail = it },
                                onCreatePasswordChange = { createPassword = it },
                                viewModel = viewModel,
                                loadingChange = { isLoading = it },
                                refreshChange = {
                                    if (it) {
                                        refreshTrigger++
                                    } else {
                                        isWrong = true
                                    }
                                }
                            )
                            LoginSection(
                                loginEmail,
                                loginPassword,
                                onLoginEmailChange = { loginEmail = it },
                                onLoginPasswordChange = { loginPassword = it },
                                viewModel = viewModel,
                                loadingChange = { isLoading = it },
                                refreshChange = {
                                    if (it) {
                                        refreshTrigger++
                                    } else {
                                        isWrong = true
                                    }
                                }
                            )
                        }
                    }
                    //If the user puts in an invalid email/password
                    if (isWrong) {
                        Text(
                            text = "Invalid Email/Password",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            fontSize = 32.sp
                        )
                    }
                }
            }
        }
    }
}

//This is for the creating account section
@Composable
private fun CreateAccountSection(
    createEmail: String,
    createPassword: String,
    onCreateEmailChange: (String) -> Unit,
    onCreatePasswordChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false,
    viewModel: AccountView,
    refreshChange: (Boolean) -> Unit,
    loadingChange: (Boolean) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        //When in landscaped, the button is changed to be next to the fields instead of below
        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(2f)
                ) {
                    //Email
                    OutlinedTextField(
                        value = createEmail,
                        onValueChange = onCreateEmailChange,
                        label = { Text("Email/Username") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    //Password
                    OutlinedTextField(
                        value = createPassword,
                        onValueChange = onCreatePasswordChange,
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )
                }

                //Create account
                Button(
                    onClick = {
                        viewModel.createAccount(
                            createEmail, createPassword,
                            onLoading = { loadingChange(it) },
                            onFinished = { refreshChange(it) })
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = darkGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Create\nAccount",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp
                    )
                }
            }
        } else {
            //Email
            OutlinedTextField(
                value = createEmail,
                onValueChange = onCreateEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            //Password
            OutlinedTextField(
                value = createPassword,
                onValueChange = onCreatePasswordChange,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            //Create account
            Button(
                onClick = {
                    viewModel.createAccount(
                        createEmail, createPassword,
                        onLoading = { loadingChange(it) },
                        onFinished = { refreshChange(it) })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = darkGreen,
                    contentColor = Color.White
                )
            ) {
                Text(
                    "Create Account",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp
                )
            }
        }
    }

}

@Composable
private fun LoginSection(
    loginEmail: String,
    loginPassword: String,
    onLoginEmailChange: (String) -> Unit,
    onLoginPasswordChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false,
    viewModel: AccountView,
    refreshChange: (Boolean) -> Unit,
    loadingChange: (Boolean) -> Unit
) {

    Column(
        modifier = modifier
    ) {
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        //When in landscaped, the button is changed to be next to the fields instead of below
        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(2f)
                ) {
                    //Email
                    OutlinedTextField(
                        value = loginEmail,
                        onValueChange = onLoginEmailChange,
                        label = { Text("Email/Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    //Password
                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = onLoginPasswordChange,
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                }

                //Login
                Button(
                    onClick = {
                        viewModel.loginAccount(
                            loginEmail, loginPassword,
                            onLoading = { loadingChange(it) },
                            onFinished = { refreshChange(it) })
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = darkGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        "Login",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp
                    )
                }
            }
        } else {
            //Email
            OutlinedTextField(
                value = loginEmail,
                onValueChange = onLoginEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            //Password
            OutlinedTextField(
                value = loginPassword,
                onValueChange = onLoginPasswordChange,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            //Login
            Button(
                onClick = {
                    viewModel.loginAccount(
                        loginEmail, loginPassword,
                        onLoading = { loadingChange(it) },
                        onFinished = { refreshChange(it) })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = darkGreen,
                    contentColor = Color.White
                )
            ) {
                Text(
                    "Login",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp
                )
            }
        }
    }

}

//This is a view model to handle the backend so that it runs regardless of the composable state
class AccountView : ViewModel() {
    fun createAccount(
        email: String,
        password: String,
        onLoading: (Boolean) -> Unit,
        onFinished: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            var success = false
            onLoading(true)
            try {
                success = onCreateAccount(email, password)
            } finally {
                onLoading(false)
                onFinished(success)
            }
        }
    }

    fun loginAccount(
        email: String,
        password: String,
        onLoading: (Boolean) -> Unit,
        onFinished: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            var success = false
            onLoading(true)
            try {
                success = onLogin(email, password)
            } finally {
                onLoading(false)
                onFinished(success)
            }
        }
    }

    fun logoutAccount(onLoading: (Boolean) -> Unit, onFinished: () -> Unit) {
        viewModelScope.launch {
            onLoading(true)
            try {
                onLogout()
            } finally {
                onLoading(false)
                onFinished()
            }
        }
    }
}

//Creates the account
//Will return as false if the account already exists
//Any url that is bookmarked will automatically be added to the account
suspend fun onCreateAccount(email: String, password: String): Boolean {
    var LoggedIn = false

    if (email.isEmpty() || password.isEmpty()) {
        println("Invalid email or password")
        return LoggedIn
    }

    if (DatabaseProvider.dynamoDBHelper.getUserDataById(email, password) == null) {
        println("Account Doesnt Exist")
        DatabaseProvider.db.userDataDao().updateLoginStatus(true)
        DatabaseProvider.db.userDataDao().updateEmailAndPassword(email, password)
        DatabaseProvider.isLoggedIn = true
        DatabaseProvider.email = email
        DatabaseProvider.password = password

        val user = AWSUserData(
            email,
            password,
            DatabaseProvider.getBookmarkedUrls()
        )

        DatabaseProvider.dynamoDBHelper.saveUserData(user)
        LoggedIn = true
    } else {
        println("Account Exists")
    }
    return LoggedIn
}

//Logs into the account
//Will fail if an account doesn't exist
//Any url that is bookmarked will be replaced with the user's bookmarks
suspend fun onLogin(email: String, password: String): Boolean {
    var LoggedIn = false

    if (email.isEmpty() || password.isEmpty()) {
        println("Invalid email or password")
        return LoggedIn
    }

    val userData = DatabaseProvider.dynamoDBHelper.getUserDataById(email, password)
    if (userData != null) {
        println("Account Exists")
        DatabaseProvider.db.userDataDao().updateLoginStatus(true)
        DatabaseProvider.db.userDataDao().updateEmailAndPassword(email, password)
        DatabaseProvider.db.userDataDao().updateBookmarkedUrls(userData.bookmarkedUrls)

        DatabaseProvider.isLoggedIn = true
        DatabaseProvider.email = email
        DatabaseProvider.password = password
        DatabaseProvider.bookmarkedUrlsList = userData.bookmarkedUrls.toMutableList()
        LoggedIn = true
    } else {
        println("Account Doesnt Exist")
    }
    return LoggedIn
}

//Logs out, clears whatever was bookmarked locally
suspend fun onLogout() {
    DatabaseProvider.db.userDataDao().updateLoginStatus(false)
    DatabaseProvider.db.userDataDao().updateEmailAndPassword("", "")
    DatabaseProvider.db.userDataDao().updateBookmarkedUrls(emptyList())

    DatabaseProvider.isLoggedIn = false
    DatabaseProvider.email = ""
    DatabaseProvider.password = ""
    DatabaseProvider.bookmarkedUrlsList = emptyList<String>().toMutableList()

    println("Logged Out")
}
