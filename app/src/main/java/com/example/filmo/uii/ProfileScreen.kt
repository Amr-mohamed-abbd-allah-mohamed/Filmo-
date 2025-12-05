package com.example.filmo.uii


import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.filmo.R
import com.example.filmo.uii.BottomNavigationBar
import com.example.filmo.utils.UserPrefsManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val userPrefsManager = remember { UserPrefsManager.getInstance(context) }

    // State variables
    var userName by remember {
        mutableStateOf(userPrefsManager.getUserName())
    }

    var userEmail by remember {
        mutableStateOf(currentUser?.email ?: "")
    }

    var originalEmail by remember {
        mutableStateOf(currentUser?.email ?: "")
    }

    var userLocation by remember {
        mutableStateOf(userPrefsManager.getUserLocation())
    }

    // Store selected avatar ID
    var selectedAvatarId by remember {
        mutableStateOf(userPrefsManager.getAvatarId())
    }

    // Edit mode
    var isEditing by remember { mutableStateOf(false) }
    var darkMode by remember { mutableStateOf(userPrefsManager.isDarkMode()) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showChangeEmailDialog by remember { mutableStateOf(false) }

    // New: Dialog for reauthentication when changing name
    var showReauthDialog by remember { mutableStateOf(false) }
    var reauthPassword by remember { mutableStateOf("") }
    var reauthError by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Password change states
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    // Email change states
    var newEmail by remember { mutableStateOf("") }
    var emailPassword by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var emailVerificationSent by remember { mutableStateOf(false) }

    // Avatar resources
    val avatarMap = mapOf(
        1 to R.drawable.avatar1,
        2 to R.drawable.avatar2,
        3 to R.drawable.avatar3,
        4 to R.drawable.avatar4
    )

    val avatarList = avatarMap.filter { it.value != 0 }.entries.toList()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Sync Firebase data with local cache
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            val firebaseEmail = user.email ?: ""
            if (firebaseEmail.isNotEmpty() && firebaseEmail != userEmail) {
                userEmail = firebaseEmail
                originalEmail = firebaseEmail
            }

            val firebaseName = user.displayName
            if (!firebaseName.isNullOrEmpty() && firebaseName != userName) {
                userName = firebaseName
                userPrefsManager.saveUserName(firebaseName)
            }
        }
    }

    // Function to update user profile in Firebase with reauthentication
    suspend fun updateFirebaseProfile(name: String, password: String): Boolean {
        return try {
            val user = currentUser ?: throw Exception("No user logged in")
            val userEmail = user.email ?: throw Exception("User email not found")

            // Reauthentication before any sensitive changes
            val credential = EmailAuthProvider.getCredential(userEmail, password)
            user.reauthenticate(credential).await()

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()

            user.updateProfile(profileUpdates).await()
            userPrefsManager.saveUserName(name)

            errorMessage = null
            snackbarHostState.showSnackbar("Profile updated successfully!")
            true
        } catch (e: Exception) {
            reauthError = when {
                e.message?.contains("wrong password", ignoreCase = true) == true ||
                        e.message?.contains("invalid credential", ignoreCase = true) == true ->
                    "Incorrect password. Please try again."
                else -> "Error: ${e.message}"
            }
            false
        }
    }

    // Function to change password
    suspend fun changePassword(oldPassword: String, newPassword: String): Boolean {
        return try {
            val user = currentUser ?: return false
            val email = user.email ?: return false

            val credential = EmailAuthProvider.getCredential(email, oldPassword)
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()

            true
        } catch (e: Exception) {
            passwordError = when {
                e.message?.contains("wrong password", ignoreCase = true) == true ||
                        e.message?.contains("invalid credential", ignoreCase = true) == true ->
                    "Current password is incorrect"
                e.message?.contains("weak", ignoreCase = true) == true ->
                    "Password is too weak. Use at least 6 characters"
                else -> "Error: ${e.message}"
            }
            false
        }
    }

    // Function to change email (with verification)
    suspend fun changeEmailWithVerification(oldEmail: String, newEmail: String, password: String): Boolean {
        return try {
            val user = currentUser ?: return false

            val credential = EmailAuthProvider.getCredential(oldEmail, password)
            user.reauthenticate(credential).await()
            user.verifyBeforeUpdateEmail(newEmail).await()

            true
        } catch (e: Exception) {
            emailError = when {
                e.message?.contains("wrong password", ignoreCase = true) == true ||
                        e.message?.contains("invalid credential", ignoreCase = true) == true ->
                    "Password is incorrect"
                e.message?.contains("already in use", ignoreCase = true) == true ->
                    "This email is already registered"
                e.message?.contains("invalid email", ignoreCase = true) == true ->
                    "Please enter a valid email address"
                else -> "Error: ${e.message}"
            }
            false
        }
    }

    // Main save function for profile
    fun saveProfile() {
        // Show reauthentication dialog instead of directly saving
        if (isEditing) {
            showReauthDialog = true
        } else {
            isEditing = true
        }
    }

    // Reauthentication Dialog
    if (showReauthDialog) {
        AlertDialog(
            onDismissRequest = {
                showReauthDialog = false
                reauthPassword = ""
                reauthError = ""
            },
            title = {
                Text(
                    "Security Verification",
                    color = Color(0xFF6E0A01),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "For security reasons, please enter your current password to save changes:",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = reauthPassword,
                        onValueChange = {
                            reauthPassword = it
                            reauthError = ""
                        },
                        label = { Text("Current Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (reauthError.isNotEmpty()) {
                        Text(
                            text = reauthError,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when {
                            reauthPassword.isEmpty() -> {
                                reauthError = "Please enter your password"
                            }
                            else -> {
                                scope.launch {
                                    isLoading = true
                                    val success = updateFirebaseProfile(userName, reauthPassword)
                                    if (success) {
                                        if (selectedAvatarId != -1) {
                                            userPrefsManager.saveAvatarId(selectedAvatarId)
                                        }
                                        userPrefsManager.saveUserLocation(userLocation)

                                        snackbarHostState.showSnackbar("Profile updated successfully!")
                                        showReauthDialog = false
                                        isEditing = false
                                        reauthPassword = ""
                                        reauthError = ""
                                    }
                                    isLoading = false
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6E0A01))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text("Verify & Save")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showReauthDialog = false
                        reauthPassword = ""
                        reauthError = ""
                        isEditing = true // Stay in edit mode
                    }
                ) {
                    Text("Cancel")
                }
            },
            containerColor = Color.White
        )
    }

    // Change Password Dialog
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showChangePasswordDialog = false
                currentPassword = ""
                newPassword = ""
                confirmPassword = ""
                passwordError = ""
            },
            title = {
                Text(
                    "Change Password",
                    color = Color(0xFF6E0A01),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = {
                            currentPassword = it
                            passwordError = ""
                        },
                        label = { Text("Current Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            passwordError = ""
                        },
                        label = { Text("New Password (min 6 characters)") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            passwordError = ""
                        },
                        label = { Text("Confirm New Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (passwordError.isNotEmpty()) {
                        Text(
                            text = passwordError,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = "Password must be at least 6 characters long",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when {
                            currentPassword.isEmpty() -> {
                                passwordError = "Please enter current password"
                            }
                            newPassword.length < 6 -> {
                                passwordError = "New password must be at least 6 characters"
                            }
                            newPassword != confirmPassword -> {
                                passwordError = "Passwords don't match"
                            }
                            else -> {
                                scope.launch {
                                    isLoading = true
                                    val success = changePassword(currentPassword, newPassword)
                                    if (success) {
                                        snackbarHostState.showSnackbar("Password changed successfully!")
                                        showChangePasswordDialog = false
                                        currentPassword = ""
                                        newPassword = ""
                                        confirmPassword = ""
                                    }
                                    isLoading = false
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6E0A01))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text("Change Password")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showChangePasswordDialog = false
                        currentPassword = ""
                        newPassword = ""
                        confirmPassword = ""
                        passwordError = ""
                    }
                ) {
                    Text("Cancel")
                }
            },
            containerColor = Color.White
        )
    }

    // Change Email Dialog
    if (showChangeEmailDialog) {
        AlertDialog(
            onDismissRequest = {
                showChangeEmailDialog = false
                newEmail = ""
                emailPassword = ""
                emailError = ""
                emailVerificationSent = false
            },
            title = {
                Text(
                    if (!emailVerificationSent) "Change Email" else "Verify New Email",
                    color = Color(0xFF6E0A01),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    if (!emailVerificationSent) {
                        Text(
                            "Current email:",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Text(
                            originalEmail,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = newEmail,
                            onValueChange = {
                                newEmail = it
                                emailError = ""
                            },
                            label = { Text("New Email Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = emailPassword,
                            onValueChange = {
                                emailPassword = it
                                emailError = ""
                            },
                            label = { Text("Current Password (for verification)") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (emailError.isNotEmpty()) {
                            Text(
                                text = emailError,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Text(
                            text = "A verification email will be sent to your new email address.",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        Text(
                            "✓ Verification email sent!",
                            color = Color.Green,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            "We've sent a verification email to:",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Text(
                            newEmail,
                            color = Color(0xFF6E0A01),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            "Please check your inbox and click the verification link to complete the email change.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            "Your email will remain:",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Text(
                            originalEmail,
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            },
            confirmButton = {
                if (!emailVerificationSent) {
                    Button(
                        onClick = {
                            when {
                                newEmail.isEmpty() -> {
                                    emailError = "Please enter new email"
                                }
                                newEmail == originalEmail -> {
                                    emailError = "New email cannot be the same as current email"
                                }
                                !newEmail.contains("@") || !newEmail.contains(".") -> {
                                    emailError = "Please enter a valid email address"
                                }
                                emailPassword.isEmpty() -> {
                                    emailError = "Please enter your password for verification"
                                }
                                else -> {
                                    scope.launch {
                                        isLoading = true
                                        val success = changeEmailWithVerification(
                                            originalEmail,
                                            newEmail,
                                            emailPassword
                                        )
                                        if (success) {
                                            emailVerificationSent = true
                                            snackbarHostState.showSnackbar("Verification email sent to $newEmail")
                                        }
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6E0A01))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text("Send Verification")
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            showChangeEmailDialog = false
                            newEmail = ""
                            emailPassword = ""
                            emailVerificationSent = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6E0A01))
                    ) {
                        Text("Done")
                    }
                }
            },
            dismissButton = {
                if (!emailVerificationSent) {
                    TextButton(
                        onClick = {
                            showChangeEmailDialog = false
                            newEmail = ""
                            emailPassword = ""
                            emailError = ""
                        }
                    ) {
                        Text("Cancel")
                    }
                } else {
                    null
                }
            },
            containerColor = Color.White
        )
    }

    // Avatar Dialog
    if (showAvatarDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarDialog = false },
            title = {
                Text(
                    "Choose Profile Picture",
                    color = MaterialTheme.colorScheme.primary
                )
            },
            confirmButton = {
                Button(
                    onClick = { showAvatarDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6E0A01))
                ) {
                    Text("Close")
                }
            },
            containerColor = if (darkMode) Color(0xFF1E1E1E) else Color.White,
            text = {
                if (avatarList.isEmpty()) {
                    Text("No avatars available", color = Color.Gray)
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.height(250.dp)
                    ) {
                        items(avatarList) { (id, resId) ->
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = "Avatar $id",
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(
                                        2.dp,
                                        if (selectedAvatarId == id) Color(0xFF6E0A01) else
                                            if (darkMode) Color.White else Color.Gray,
                                        CircleShape
                                    )
                                    .clickable {
                                        selectedAvatarId = id
                                        showAvatarDialog = false
                                    },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        )
    }


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(60.dp))

                    // Profile Image Section
                    Box {
                        selectedAvatarId.takeIf { it != -1 }?.let { avatarId ->
                            avatarMap[avatarId]?.let { avatarResId ->
                                Image(
                                    painter = painterResource(id = avatarResId),
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(140.dp)
                                        .clip(CircleShape)
                                        .border(3.dp, if (darkMode) Color.White else Color.Gray, CircleShape)
                                        .clickable {
                                            if (isEditing) showAvatarDialog = true
                                        }
                                )
                            }
                        } ?: run {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Default User",
                                tint = if (darkMode) Color.White else Color.Gray,
                                modifier = Modifier
                                    .size(140.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        if (isEditing) showAvatarDialog = true
                                    }
                            )
                        }

                        // Edit Avatar Button
                        if (isEditing) {
                            IconButton(
                                onClick = { showAvatarDialog = true },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .background(if (darkMode) Color(0xFF6E0A01) else Color.White, CircleShape)
                                    .border(1.dp, if (darkMode) Color.White else Color.Gray, CircleShape)
                                    .size(42.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Change Photo",
                                    tint =MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Error message
                    errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Name
                    if (isEditing) {
                        OutlinedTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            label = { Text("Name") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        Text(
                            text = userName,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Email
                    Text(
                        text = userEmail,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Location
                    if (isEditing) {
                        OutlinedTextField(
                            value = userLocation,
                            onValueChange = { userLocation = it },
                            label = { Text("Location") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    } else {
                        Text(
                            text = userLocation,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Edit/Save Button
                    Button(
                        onClick = {
                            saveProfile()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(horizontal = 20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isEditing) "Save Changes" else "Edit Profile",
                            fontSize = 16.sp
                        )
                    }


                    Spacer(modifier = Modifier.height(12.dp))

                    // Change Password Button - بيظهر بس في Edit Mode
                    if (isEditing) {
                        OutlinedButton(
                            onClick = {
                                showChangePasswordDialog = true
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF6E0A01)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .padding(horizontal = 20.dp)
                                .border(2.dp, Color(0xFF6E0A01), MaterialTheme.shapes.medium)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Change Password",
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Change Password", fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Change Email Button - بيظهر بس في Edit Mode
                        OutlinedButton(
                            onClick = {
                                showChangeEmailDialog = true
                                newEmail = ""
                                emailPassword = ""
                                emailError = ""
                                emailVerificationSent = false
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF2196F3)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .padding(horizontal = 20.dp)
                                .border(2.dp, Color(0xFF2196F3), MaterialTheme.shapes.medium)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Change Email",
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Change Email", fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(if (isEditing) 12.dp else 24.dp))

                    // Favorites Button
                    if (!isEditing) {
                        OutlinedButton(
                            onClick = {
                                navController.navigate("favourites")
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .padding(horizontal = 20.dp)

                        ) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = "Favorites",
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("My Favorites", fontSize = 16.sp)
                        }
                    }
                    ThemeSwitch(
                        isDark = isDark,
                        onToggle = onToggleTheme // خليها تجي من MainActivity
                    )
                    TextButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    })
                    {
                        Text(text = "Logout", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }

                }
            }
        }
    }
}