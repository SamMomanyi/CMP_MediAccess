package org.sammomanyi.mediaccess.features.identity.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.features.identity.data.remote.NewsService
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GetProfileUseCase

class HomeViewModel(
    private val newsService: NewsService,
    private val getProfileUseCase: GetProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            // Load user profile
            getProfileUseCase().collectLatest { user ->
                _state.update { it.copy(user = user) }
            }
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoadingNews = true) }

            val fetchedArticles = newsService.fetchHealthNews()

            _state.update {
                it.copy(
                    articles = fetchedArticles,
                    isLoadingNews = false
                )
            }
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.OnRefresh -> {
                viewModelScope.launch {
                    _state.update { it.copy(isRefreshing = true) }
                    val articles = newsService.fetchHealthNews()
                    _state.update {
                        it.copy(
                            articles = articles,
                            isRefreshing = false
                        )
                    }
                }
            }

            HomeAction.OnGenerateVisitCode -> TODO()
            HomeAction.OnRefreshVisitCode -> TODO()
        }
    }
}