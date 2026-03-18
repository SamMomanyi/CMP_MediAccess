package org.sammomanyi.mediaccess.features.cover.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.features.cover.data.CoverRepository
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GetProfileUseCase

class CoverViewModel(
    private val coverRepository: CoverRepository,
    private val getProfileUseCase: GetProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CoverState())
    val state = _state.asStateFlow()

    init {
        loadCoverRequests()
    }

    private fun loadCoverRequests() {
        viewModelScope.launch {
            // ✅ Get current user once
            val user = getProfileUseCase().firstOrNull()

            if (user == null) {
                _state.update { it.copy(isLoading = false) }
                return@launch
            }

            _state.update { it.copy(
                userEmail = user.email ?: "",
                userId = user.id ?: "",
                isLoading = true
            ) }

            // ✅ Listen to real-time updates
            coverRepository.getUserRequests(user.id ?: "")
                .catch { e ->
                    println("🔴 Error loading cover requests: ${e.message}")
                    _state.update { it.copy(isLoading = false) }
                }
                .collect { requests ->
                    _state.update { it.copy(
                        requests = requests,
                        isLoading = false
                    ) }
                }
        }
    }

    // ✅ Manual refresh (optional but nice to have)
    fun refresh() {
        loadCoverRequests()
    }

    fun onAction(action: CoverAction) {
        when (action) {
            is CoverAction.OnApproveRequest -> {
                viewModelScope.launch {
                    coverRepository.approveRequest(action.id)
                }
            }
            is CoverAction.OnRejectRequest -> {
                viewModelScope.launch {
                    coverRepository.rejectRequest(action.id, action.note)
                }
            }
        }
    }
}