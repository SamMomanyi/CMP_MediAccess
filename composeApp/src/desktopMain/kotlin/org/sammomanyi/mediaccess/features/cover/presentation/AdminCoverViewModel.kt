package org.sammomanyi.mediaccess.features.cover.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.features.cover.data.desktop.DesktopCoverRepository
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverLinkRequest
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverStatus

enum class CoverRequestFilter { ALL, PENDING, APPROVED, REJECTED }

data class AdminCoverUiState(
    val requests: List<CoverLinkRequest> = emptyList(),
    val isLoading: Boolean = false,
    val selectedRequest: CoverLinkRequest? = null,
    val activeFilter: CoverRequestFilter = CoverRequestFilter.PENDING,
    val actionInProgress: Boolean = false,
    val actionError: String? = null,
    val actionSuccess: String? = null,
    val lastRefreshedAt: Long? = null,
    val loadError: String? = null
) {
    val filteredRequests: List<CoverLinkRequest>
        get() = when (activeFilter) {
            CoverRequestFilter.ALL -> requests
            CoverRequestFilter.PENDING -> requests.filter { it.status == CoverStatus.PENDING }
            CoverRequestFilter.APPROVED -> requests.filter { it.status == CoverStatus.APPROVED }
            CoverRequestFilter.REJECTED -> requests.filter { it.status == CoverStatus.REJECTED }
        }

    val pendingCount: Int get() = requests.count { it.status == CoverStatus.PENDING }
}

class AdminCoverViewModel(
    private val repository: DesktopCoverRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminCoverUiState())
    val state: StateFlow<AdminCoverUiState> = _state.asStateFlow()

    init {
        // Mirror repository state into UI state
        repository.state.onEach { repoState ->
            val currentSelected = _state.value.selectedRequest
            _state.value = _state.value.copy(
                requests = repoState.requests,
                isLoading = repoState.isLoading,
                loadError = repoState.error,
                lastRefreshedAt = repoState.lastRefreshedAt,
                // Keep selected request fresh after refresh
                selectedRequest = if (currentSelected != null)
                    repoState.requests.find { it.id == currentSelected.id }
                else null
            )
        }.launchIn(viewModelScope)

        // Load on startup
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refresh()
        }
    }

    fun selectRequest(request: CoverLinkRequest) {
        _state.value = _state.value.copy(
            selectedRequest = request,
            actionError = null,
            actionSuccess = null
        )
    }

    fun setFilter(filter: CoverRequestFilter) {
        _state.value = _state.value.copy(activeFilter = filter, selectedRequest = null)
    }

    fun approveRequest(id: String, note: String = "Approved") {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionInProgress = true, actionError = null)
            val result = repository.approveRequest(id, note)
            _state.value = _state.value.copy(
                actionInProgress = false,
                actionSuccess = if (result.isSuccess) "Request approved successfully" else null,
                actionError = if (result.isFailure) result.exceptionOrNull()?.message ?: "Approval failed" else null,
                selectedRequest = null
            )
        }
    }

    fun rejectRequest(id: String, note: String) {
        if (note.isBlank()) {
            _state.value = _state.value.copy(actionError = "A rejection reason is required")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(actionInProgress = true, actionError = null)
            val result = repository.rejectRequest(id, note)
            _state.value = _state.value.copy(
                actionInProgress = false,
                actionSuccess = if (result.isSuccess) "Request rejected" else null,
                actionError = if (result.isFailure) result.exceptionOrNull()?.message ?: "Rejection failed" else null,
                selectedRequest = null
            )
        }
    }

    fun dismissFeedback() {
        _state.value = _state.value.copy(actionError = null, actionSuccess = null)
    }
}