package org.sammomanyi.mediaccess.core.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.core.data.preferences.ThemeRepository

class ThemeViewModel(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = themeRepository.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    fun toggleTheme() {
        viewModelScope.launch {
            themeRepository.setDarkMode(!isDarkMode.value)
        }
    }

    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            themeRepository.setDarkMode(isDark)
        }
    }
}