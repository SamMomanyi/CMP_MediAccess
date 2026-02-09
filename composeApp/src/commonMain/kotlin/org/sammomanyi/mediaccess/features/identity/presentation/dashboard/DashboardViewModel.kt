package org.sammomanyi.mediaccess.features.identity.presentation.dashboard

import GenerateVisitCodeUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.identity.domain.model.VisitPurpose
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GetProfileUseCase

class DashboardViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val generateVisitCodeUseCase: GenerateVisitCodeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state = combine(_state, getProfileUseCase()) { currentState, user ->
        currentState.copy(user = user)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())

    // Inside DashboardViewModel
    fun onGenerateVisitCode() {
        viewModelScope.launch {
            val currentUserId = state.value.user?.id ?: ""

            if (currentUserId.isEmpty()) {
                // Handle case where user isn't loaded yet
                return@launch
            }

            _state.update { it.copy(isLoading = true) }

            // CALL the instance, don't try to construct it here
            val result = generateVisitCodeUseCase(
                userId = currentUserId,
                purpose = VisitPurpose.CONSULTATION
            )

            when (result) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false, activeVisitCode = result.data) }
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false, errorMessage = UiText.from(result.error)) }
                }
            }
        }
    }
}