package com.example.filmo


import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

sealed class LoginState {
    object Idle: LoginState()
    object Loading: LoginState()
    object Success: LoginState()
    data class Error(val msg: String): LoginState()
}

class LoginViewModel : ViewModel() {
    var loginState: LoginState by mutableStateOf(LoginState.Idle)
        private set

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            loginState = LoginState.Error("Please fill all fields")
            return
        }
        // fake logic for demo
        loginState = LoginState.Loading
        if (email == "a@a.com" && password == "123") {
            loginState = LoginState.Success
        } else {
            loginState = LoginState.Error("Login failed")
        }
    }
}