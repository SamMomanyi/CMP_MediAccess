package org.sammomanyi.mediaccess.features.identity.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.features.identity.data.remote.NewsService

class HomeViewModel(
    private val newsService: NewsService,
    // private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingNews = true) }

            // Fetch articles from the API
            val fetchedArticles = newsService.fetchHealthNews()

            _state.update { it.copy(
                articles = fetchedArticles,
                isLoadingNews = false
            ) }
        }
    }

    fun onAction(action: HomeAction) {
        // Handle clicks like OnNavigateToCare, etc.
    }
}