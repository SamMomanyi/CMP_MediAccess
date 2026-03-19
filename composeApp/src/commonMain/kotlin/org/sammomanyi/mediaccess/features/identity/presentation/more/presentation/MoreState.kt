package org.sammomanyi.mediaccess.features.identity.presentation.more.presentation

import org.sammomanyi.mediaccess.features.identity.domain.model.User

data class MoreState(
    val user: User? = null,
    val isDarkMode: Boolean = false,
    val appVersion: String = "1.0.0",
    val isLoading: Boolean = false
)
