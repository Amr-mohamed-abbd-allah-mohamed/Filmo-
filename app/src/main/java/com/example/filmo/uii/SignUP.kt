package com.example.filmo.uii

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.filmo.R
import com.example.filmo.ui.theme.BackGround
import com.example.filmo.ui.theme.LightGray
import com.example.filmo.ui.theme.Maroon
import com.example.filmo.utils.UserPrefsManager
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()

    val context = LocalContext.current
    val userPrefsManager = remember { UserPrefsManager.getInstance(context) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val gold = Color(0xFFD3C0AF)
    val darkRed = Color(0xFF6E0A01)
    val darkBackground = Color(0xFF1A0907)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.filmo_logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(140.dp)
                .padding(bottom = 20.dp)
        )

        Text(
            text = "Welcome!",
            fontSize = 36.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(bottom = 5.dp)
        )
        Text(
            text = "Creat your account",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
        )

        // Email
        EmailField(email) { email = it }

        // Name field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name", color = MaterialTheme.colorScheme.secondary) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Email icon",
                    tint = Color(110, 10, 1)
                )
            },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .padding(top = 20.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Password
        PasswordField(password) { password = it }
        //confirm password
        ConfirmPasswordField(confirmPassword) {confirmPassword = it }
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
                .padding(top = 20.dp)
                .width(200.dp)
                .height(40.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = gold, strokeWidth = 2.dp)
            } else {
                Text("Create Account")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        AlreadyHaveAccountText(navController)


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
@Composable
fun ConfirmPasswordField(value: String, onValueChange: (String) -> Unit) {
    var showPassword by rememberSaveable { mutableStateOf(false) }
    val colors = remember {
        Brush.linearGradient(listOf(Color.Magenta, Color.Green, Color.Blue))
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Confirm Password", color = MaterialTheme.colorScheme.secondary) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.LockOpen,
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
fun AlreadyHaveAccountText(navController: NavController) {
    val annotatedText = buildAnnotatedString {
        withStyle(
            style = SpanStyle
                ( color =  MaterialTheme.colorScheme.secondary)){
            append("Already have an account? ")
        }



        // نضيف كلمة "Login in" ككلمة قابلة للنقر
        pushStringAnnotation(tag = "LOGIN", annotation = "login")
        withStyle(
            style = SpanStyle(
                color = Color(0xFF0A84FF),
                fontWeight = FontWeight.SemiBold
            )
        ) {
            append("Login")
        }
        pop()
    }

    ClickableText(
        text = annotatedText,
        modifier = Modifier.padding(top = 18.dp),
        onClick = {offset->annotatedText.getStringAnnotations("LOGIN",offset,offset)
            .firstOrNull()?.let {
                navController.navigate("login") }}
    )
}