package org.sammomanyi.mediaccess.features.identity.presentation.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.core.presentation.UiText
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GetProfileUseCase
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GetRecordsUseCase
import org.sammomanyi.mediaccess.features.identity.domain.use_case.SyncRecordsUseCase

class RecordsViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val getRecordsUseCase: GetRecordsUseCase,
    private val syncRecordsUseCase: SyncRecordsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RecordsState())
    val state = _state.asStateFlow()

    init {
        loadRecords()
    }

    private fun loadRecords() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            getProfileUseCase().collectLatest { user ->
                if (user != null) {
                    // Sync from Firestore first
                    syncRecords(user.id)

                    // Then observe local records
                    getRecordsUseCase(user.id).collectLatest { records ->
                        _state.update {
                            it.copy(
                                records = records,
                                isLoading = false
                            )
                        }
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = UiText.DynamicString("Please log in to view records")
                        )
                    }
                }
            }
        }
    }

    private suspend fun syncRecords(patientId: String) {
        _state.update { it.copy(isSyncing = true) }

        when (syncRecordsUseCase(patientId)) {
            is Result.Success -> {
                _state.update { it.copy(isSyncing = false) }
            }
            is Result.Error -> {
                _state.update {
                    it.copy(
                        isSyncing = false,
                        errorMessage = UiText.DynamicString("Failed to sync records")
                    )
                }
            }
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            getProfileUseCase().firstOrNull()?.let { user ->
                user?.id?.let { syncRecords(it) }
            }
        }
    }
}