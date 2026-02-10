package org.sammomanyi.mediaccess.features.identity.presentation.profile

import org.sammomanyi.mediaccess.core.presentation.UiText
import org.sammomanyi.mediaccess.features.identity.domain.model.User

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val errorMessage: UiText? = null
)