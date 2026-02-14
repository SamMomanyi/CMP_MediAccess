package org.sammomanyi.mediaccess.features.cover.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.features.cover.data.CoverRepository
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverLinkRequest
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverStatus

// ── Filter tabs ───────────────────────────────────────────────
enum class CoverRequestFilter { ALL, PENDING, APPROVED, REJECTED }

// ── UI State ──────────────────────────────────────────────────
data class AdminCoverUiState(
    val requests: List<CoverLinkRequest> = emptyList(),
    val isLoading: Boolean = true,
    val selectedRequest: CoverLinkRequest? = null,
    val activeFilter: CoverRequestFilter = CoverRequestFilter.PENDING,
    val actionInProgress: Boolean = false,
    val actionError: String? = null,
    val actionSuccess: String? = null
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
    private val coverRepository: CoverRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminCoverUiState())
    val state: StateFlow<AdminCoverUiState> = _state.asStateFlow()

    init {
        observeAllRequests()
    }

    private fun observeAllRequests() {
        viewModelScope.launch {
            coverRepository.getAllRequests()
                .catch { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        actionError = "Failed to load requests: ${e.message}"
                    )
                }
                .collect { requests ->
                    val currentSelected = _state.value.selectedRequest
                    _state.value = _state.value.copy(
                        requests = requests.sortedByDescending { it.submittedAt },
                        isLoading = false,
                        // Keep selected request fresh if it's still in the list
                        selectedRequest = if (currentSelected != null)
                            requests.find { it.id == currentSelected.id }
                        else null
                    )
                }
        }
    }

    fun selectRequest(request: CoverLinkRequest) {
        _state.value = _state.value.copy(selectedRequest = request, actionError = null, actionSuccess = null)
    }

    fun setFilter(filter: CoverRequestFilter) {
        _state.value = _state.value.copy(activeFilter = filter, selectedRequest = null)
    }

    fun approveRequest(id: String, note: String = "Approved") {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionInProgress = true, actionError = null)
            val result = coverRepository.approveRequest(id, note)
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
            val result = coverRepository.rejectRequest(id, note)
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