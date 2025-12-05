package com.example.filmo


import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.example.filmo.uii.LoginScreen
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun checkEmailFieldExists() {
        rule.setContent {
            LoginScreen(navController = rememberNavController())
        }

        rule.onNodeWithText("Email").assertExists()
    }

    @Test
    fun checkPasswordFieldExists() {
        rule.setContent {
            LoginScreen(navController = rememberNavController())
        }

        rule.onNodeWithText("Password").assertExists()
    }

    @Test
    fun typingEmailWorks() {
        rule.setContent {
            LoginScreen(navController = rememberNavController())
        }

        rule.onNodeWithText("Email")
            .performTextInput("test@gmail.com")

        rule.onNodeWithText("test@gmail.com")
            .assertExists()
    }
}