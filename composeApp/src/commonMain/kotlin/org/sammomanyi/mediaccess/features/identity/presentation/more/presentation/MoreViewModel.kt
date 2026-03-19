package org.sammomanyi.mediaccess.features.more.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GetProfileUseCase
import org.sammomanyi.mediaccess.core.data.ThemePreferences
import org.sammomanyi.mediaccess.features.identity.presentation.more.presentation.MoreAction
import org.sammomanyi.mediaccess.features.identity.presentation.more.presentation.MoreState

class MoreViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val themePreferences: ThemePreferences,
    private val onLogoutSuccess: () -> Unit
) : ViewModel() {

    private val _state = MutableStateFlow(MoreState())
    val state = _state.asStateFlow()

    init {
        loadUserProfile()
        observeTheme()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            getProfileUseCase().collectLatest { user ->
                _state.update { it.copy(user = user) }
            }
        }
    }

    private fun observeTheme() {
        viewModelScope.launch {
            themePreferences.isDarkMode.collectLatest { isDark ->
                _state.update { it.copy(isDarkMode = isDark) }
            }
        }
    }

    fun onAction(action: MoreAction) {
        when (action) {
            MoreAction.OnToggleTheme -> {
                viewModelScope.launch {
                    themePreferences.toggleTheme()
                }
            }
            MoreAction.OnHelpCenter -> {
                println("✅ Opening Help Center")
                // TODO: Navigate to help
            }
            MoreAction.OnAbout -> {
                println("✅ Opening About")
                // TODO: Navigate to about
            }
            MoreAction.OnFeedback -> {
                println("✅ Opening Feedback")
                // TODO: Navigate to feedback
            }
            MoreAction.OnSettings -> {
                println("✅ Opening Settings")
                // TODO: Navigate to settings
            }
            MoreAction.OnNotifications -> {
                println("✅ Opening Notifications Settings")
                // TODO: Navigate to notification settings
            }
            MoreAction.OnLanguage -> {
                println("✅ Opening Language Selection")
                // TODO: Navigate to language selection
            }
            MoreAction.OnPrivacy -> {
                println("✅ Opening Privacy Policy")
                // TODO: Navigate to privacy policy
            }
            MoreAction.OnTerms -> {
                println("✅ Opening Terms of Service")
                // TODO: Navigate to terms
            }
            MoreAction.OnLicenses -> {
                println("✅ Opening Licenses")
                // TODO: Navigate to licenses
            }
            MoreAction.OnLogout -> {
                viewModelScope.launch {
                    println("✅ Logging out...")
                    onLogoutSuccess()
                }
            }
        }
    }
}
