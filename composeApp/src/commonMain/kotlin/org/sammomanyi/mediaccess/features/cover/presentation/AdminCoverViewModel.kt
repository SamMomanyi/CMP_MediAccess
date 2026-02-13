package org.sammomanyi.mediaccess.features.cover.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.features.cover.data.CoverRepository
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverLinkRequest
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverStatus

enum class AdminFilter { ALL, PENDING, APPROVED, REJECTED }

data class AdminCoverState(
    val allRequests: List<CoverLinkRequest> = emptyList(),
    val filter: AdminFilter = AdminFilter.PENDING,
    val isLoading: Boolean = true
) {
    val pendingCount get() = allRequests.count { it.status == CoverStatus.PENDING }

    val filteredRequests get() = when (filter) {
        AdminFilter.ALL -> allRequests
        AdminFilter.PENDING -> allRequests.filter { it.status == CoverStatus.PENDING }
        AdminFilter.APPROVED -> allRequests.filter { it.status == CoverStatus.APPROVED }
        AdminFilter.REJECTED -> allRequests.filter { it.status == CoverStatus.REJECTED }
    }
}

sealed interface AdminCoverAction {
    data class Approve(val id: String) : AdminCoverAction
    data class Reject(val id: String, val note: String) : AdminCoverAction
    data class SetFilter(val filter: AdminFilter) : AdminCoverAction
}

class AdminCoverViewModel(
    private val coverRepository: CoverRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminCoverState())
    val state = _state.asStateFlow()

    init {
        // ✅ Reads from Room — works on both Android and Desktop
        viewModelScope.launch {
            coverRepository.getAllRequests().collectLatest { requests ->
                _state.update { it.copy(allRequests = requests, isLoading = false) }
            }
        }
    }

    fun onAction(action: AdminCoverAction) {
        when (action) {
            is AdminCoverAction.Approve -> {
                viewModelScope.launch {
                    coverRepository.approveRequest(action.id)
                }
            }
            is AdminCoverAction.Reject -> {
                viewModelScope.launch {
                    coverRepository.rejectRequest(action.id, action.note)
                }
            }
            is AdminCoverAction.SetFilter -> {
                _state.update { it.copy(filter = action.filter) }
            }
        }
    }
}