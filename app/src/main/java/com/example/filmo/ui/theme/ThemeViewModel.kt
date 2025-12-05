package com.example.filmo.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ThemeViewModel(private val themeManager: ThemeManager) : ViewModel() {

    private val _isDark = MutableStateFlow(false)
    val isDark: StateFlow<Boolean> = _isDark

    init {
        // استمع للـ flow من DataStore وحدّث الـ state
        themeManager.isDarkMode
            .onEach { saved ->
                _isDark.value = saved
            }
            .launchIn(viewModelScope)
    }

    // يغير ويحفظ
    fun toggleTheme() {
        val newVal = !_isDark.value
        viewModelScope.launch {
            themeManager.setDarkMode(newVal)
        }
    }

    // أو لو عايز تمرّر قيمة صريحة
    fun setTheme(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setDarkMode(enabled)
        }
    }
}
