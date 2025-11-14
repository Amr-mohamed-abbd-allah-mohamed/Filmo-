package com.example.filmo.uii

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.filmo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    moviesViewModel: MoviesViewModel = viewModel()
) {

    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    val gold = Color(0xFFD3C0AF)
    val darkRed = Color(0xFF6E0A01)
    val darkBackground = Color(0xFF1A0907)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = darkBackground
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(140.dp)
                    .padding(bottom = 20.dp)
            )

            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineMedium.copy(color = gold)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = gold) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                textStyle = LocalTextStyle.current.copy(color = gold),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = gold) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                textStyle = LocalTextStyle.current.copy(color = gold),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Forget Password
            TextButton(onClick = {
                if (email.isEmpty()) {
                    scope.launch { snackbarHostState.showSnackbar("Please enter your email first") }
                } else {
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            scope.launch {
                                if (task.isSuccessful) {
                                    snackbarHostState.showSnackbar("Password reset email sent!")
                                } else {
                                    snackbarHostState.showSnackbar(
                                        task.exception?.message ?: "Failed to send reset email"
                                    )
                                }
                            }
                        }
                }
            }) {
                Text("Forgot Password?", color = gold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Button
            Button(
                onClick = {
                    when {
                        email.isEmpty() || password.isEmpty() -> {
                            scope.launch { snackbarHostState.showSnackbar("Please fill all fields") }
                        }
                        else -> {
                            isLoading = true
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        val user = auth.currentUser
                                        if (user != null) {
                                            user.reload().addOnSuccessListener {
                                                if (user.isEmailVerified) {
                                                    moviesViewModel.loadFavourites()
                                                    navController.navigate("home") {
                                                        popUpTo("login") { inclusive = true }
                                                    }
                                                } else {
                                                    auth.signOut()
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Please verify your email first")
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        val exception = task.exception
                                        val message = if (exception is FirebaseAuthException) {
                                            when (exception.errorCode) {
                                                "ERROR_WRONG_PASSWORD" -> "Wrong password"
                                                "ERROR_USER_NOT_FOUND" -> "User not found"
                                                else -> "Login failed"
                                            }
                                        } else {
                                            "Login failed"
                                        }
                                        scope.launch { snackbarHostState.showSnackbar(message) }
                                    }
                                }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = darkRed, contentColor = gold),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = gold,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Login")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(onClick = { navController.navigate("signup") }) {
                Text("Don't have an account? Sign Up", color = gold)
            }
        }
    }
}