package org.sammomanyi.mediaccess.features.identity.presentation.home

import org.sammomanyi.mediaccess.core.presentation.UiText
import org.sammomanyi.mediaccess.features.identity.domain.model.User
import org.sammomanyi.mediaccess.features.identity.domain.model.Article

data class HomeState(
    val user: User? = null,
    val articles: List<Article> = emptyList(), // Added
    val isLoading: Boolean = false,
    val isLoadingNews: Boolean = false, // Added
    val isRefreshing: Boolean = false,
    val errorMessage: UiText? = null
)