package com.example.filmo.uii

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.filmo.R
import com.example.filmo.ui.theme.BackGround
import com.example.filmo.ui.theme.LightGray
import com.example.filmo.ui.theme.Maroon
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(
    navController: NavController,
    moviesViewModel: MoviesViewModel = viewModel()
) {

    val auth = FirebaseAuth.getInstance()

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val colors = remember {
        Brush.linearGradient(listOf(Color.Magenta, Color.Green, Color.Blue))
    }
    val gold = Color(0xFFD3C0AF)
    val darkRed = Color(0xFF6E0A01)
    val darkBackground = Color(0xFF1A0907)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())   // أهم جزء
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // logo
            Image(
                painter = painterResource(id = R.drawable.filmo_logo),
                contentDescription = "Filmo logo",
                modifier = Modifier
                    .padding(top = 50.dp)
                    .size(size = 200.dp)

            )

            Text(
                text = "Welcome Back!",
                fontSize = 36.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(bottom = 5.dp)
            )
            Text(
                text = "Login to your account",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
            )

            // Email
            EmailField(email) { email = it }

            // Password
            PasswordField(password) { password = it }

            // Forget Password
            Row (modifier = Modifier
                .padding(end = 24.dp),
                horizontalArrangement = Arrangement.End){
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
                Text(
                    text = "Forget Password?",
                    color = Color(0xFF0A84FF),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    textDecoration = TextDecoration.Underline,

                ) }
            }
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
                    .width(120.dp)
                    .height(40.dp)
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

            DontHaveAcc(navController)
        }
    }
}
@Composable
fun EmailField(value: String, onValueChange: (String) -> Unit) {
    val colors = remember {
        Brush.linearGradient(listOf(Color.Magenta, Color.Green, Color.Blue))
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Email", color = MaterialTheme.colorScheme.secondary) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Email icon",
                tint = Color(110, 10, 1)
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        textStyle = TextStyle(MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .padding(top = 20.dp)
    )
}
@Composable
fun PasswordField(value: String, onValueChange: (String) -> Unit) {
    var showPassword by rememberSaveable { mutableStateOf(false) }
    val colors = remember {
        Brush.linearGradient(listOf(Color.Magenta, Color.Green, Color.Blue))
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Password", color = MaterialTheme.colorScheme.secondary) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Password icon",
                tint = Color(110, 10, 1)
            )
        },
        trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
                Icon(
                    imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = "Toggle password",
                    tint = Color(0xFF696868)
                )
            }
        },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        textStyle = TextStyle(MaterialTheme.colorScheme.primary),
        singleLine = true,
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .padding(top = 20.dp)
    )
}
@Composable
fun DontHaveAcc(navController: NavController) {
    val annotatedText = buildAnnotatedString {
        withStyle(
            style = SpanStyle
                ( color =  MaterialTheme.colorScheme.secondary)){
            append("Don't have an account? ")
        }



        pushStringAnnotation(tag = "SIGNUP", annotation = "signup")
        withStyle(
            style = SpanStyle(
                color = Color(0xFF0A84FF),
                fontWeight = FontWeight.SemiBold
            )
        ) {
            append("Sign up here")
        }
        pop()
    }

    ClickableText(
        text = annotatedText,
        modifier = Modifier.padding(top = 18.dp),
        onClick = {offset->annotatedText.getStringAnnotations("SIGNUP",offset,offset)
            .firstOrNull()?.let {
                navController.navigate("signup") }}
    )
}


