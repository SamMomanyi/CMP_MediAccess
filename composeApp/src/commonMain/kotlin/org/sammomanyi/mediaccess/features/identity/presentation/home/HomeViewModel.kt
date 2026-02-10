package org.sammomanyi.mediaccess.features.identity.presentation.home

import GenerateVisitCodeUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.core.presentation.UiText
import org.sammomanyi.mediaccess.features.identity.domain.model.VisitPurpose
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GetProfileUseCase

class HomeViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val generateVisitCodeUseCase: GenerateVisitCodeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = combine(_state, getProfileUseCase()) { currentState, user ->
        currentState.copy(user = user)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeState())

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.OnGenerateVisitCode -> generateVisitCode()
            HomeAction.OnRefreshVisitCode -> generateVisitCode()
        }
    }

    private fun generateVisitCode() {
        viewModelScope.launch {
            val currentUserId = state.value.user?.id

            if (currentUserId.isNullOrEmpty()) {
                _state.update {
                    it.copy(errorMessage = UiText.DynamicString("Please log in to generate a visit code"))
                }
                return@launch
            }

            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val result = generateVisitCodeUseCase(
                userId = currentUserId,
                purpose = VisitPurpose.GENERAL_VISIT
            )

            when (result) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            activeVisitCode = result.data,
                            errorMessage = null
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = UiText.from(result.error)
                        )
                    }
                }
            }
        }
    }
}