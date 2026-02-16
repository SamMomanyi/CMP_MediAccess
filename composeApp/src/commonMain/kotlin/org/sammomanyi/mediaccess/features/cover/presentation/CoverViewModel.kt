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
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            getProfileUseCase().collectLatest { user ->
                user?.let {
                    _state.update { s ->
                        s.copy(
                            userEmail = user.email ?: "",
                            userId = user.id ?: "",
                            isLoading = false
                        )
                    }

                    // Pull latest status from Firestore into Room first
                    coverRepository.syncFromFirestore(user.id ?: "")

                    coverRepository.getUserRequests(user.id ?: "")
                        .collectLatest { requests ->
                            _state.update { s ->
                                s.copy(requests = requests, isLoading = false)
                            }
                        }
                }
            }
        }
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