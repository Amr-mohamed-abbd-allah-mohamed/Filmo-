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
import androidx.navigation.NavController
import com.example.filmo.R
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    val gold = Color(0xFFD3C0AF)
    val darkRed = Color(0xFF6E0A01)
    val darkBackground = Color(0xFF1A0907)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(140.dp)
                .padding(bottom = 20.dp)
        )

        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium.copy(color = gold)
        )
        Spacer(modifier = Modifier.height(20.dp))

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
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password", color = gold) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            textStyle = LocalTextStyle.current.copy(color = gold),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
        }

        Button(
            onClick = {
                when {
                    email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ->
                        errorMessage = "Please fill all fields"
                    password != confirmPassword ->
                        errorMessage = "Passwords do not match"
                    else -> {
                        errorMessage = ""
                        isLoading = true
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    // إرسال ايميل التحقق
                                    auth.currentUser?.sendEmailVerification()
                                        ?.addOnCompleteListener { verifyTask ->
                                            if (verifyTask.isSuccessful) {
                                                showSnackbar = true
                                            } else {
                                                errorMessage = verifyTask.exception?.message ?: "Failed to send verification email"
                                            }
                                        }
                                } else {
                                    errorMessage = task.exception?.message ?: "Registration failed"
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
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = gold, strokeWidth = 2.dp)
            } else {
                Text("Create Account")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text("Already have an account? Login", color = gold)
        }


        if (showSnackbar) {
            LaunchedEffect(Unit) {
                snackbarHostState.showSnackbar("Verification email sent! Check your inbox.")
                showSnackbar = false
                navController.navigate("login") {
                    popUpTo("signup") { inclusive = true }
                }
            }
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}