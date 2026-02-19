package org.sammomanyi.mediaccess.features.pharmacy.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.features.auth.data.local.AdminAccountEntity
import org.sammomanyi.mediaccess.features.pharmacy.data.desktop.PharmacyDesktopRepository
import org.sammomanyi.mediaccess.features.pharmacy.domain.model.PharmacyQueueEntry
import org.sammomanyi.mediaccess.features.pharmacy.domain.model.Prescription
import org.sammomanyi.mediaccess.features.queue.data.QueueRepository

data class PharmacistQueueState(
    val waitingQueue: List<PharmacyQueueEntry> = emptyList(),
    val currentPrescription: Prescription? = null,
    val currentQueueEntry: PharmacyQueueEntry? = null,
    val isLoading: Boolean = false,
    val actionInProgress: Boolean = false,
    val error: String? = null,
    val lastRefreshedAt: Long? = null
)

class PharmacistQueueViewModel(
    private val pharmacyRepository: PharmacyDesktopRepository,
    private val pharmacist: AdminAccountEntity
) : ViewModel() {

    private val _state = MutableStateFlow(PharmacistQueueState())
    val state: StateFlow<PharmacistQueueState> = _state.asStateFlow()

    private var autoPollJob: Job? = null

    init {
        refresh()
        startAutoPoll()
    }

    private fun startAutoPoll() {
        autoPollJob = viewModelScope.launch {
            while (isActive) {
                delay(30_000)
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val date = QueueRepository.todayString()
            val queue = pharmacyRepository.getPharmacyQueue(date)

            _state.update { it.copy(
                waitingQueue = queue,
                isLoading = false,
                lastRefreshedAt = System.currentTimeMillis()
            ) }
        }
    }

    fun servePatient(entry: PharmacyQueueEntry) {
        viewModelScope.launch {
            _state.update { it.copy(actionInProgress = true) }
            val prescription = pharmacyRepository.getPrescription(entry.prescriptionId)
            _state.update { it.copy(
                currentPrescription = prescription,
                currentQueueEntry = entry,
                actionInProgress = false
            ) }
        }
    }

    fun dispenseAndBill(totalCost: Double) {
        val entry = _state.value.currentQueueEntry ?: return
        val prescription = _state.value.currentPrescription ?: return

        viewModelScope.launch {
            _state.update { it.copy(actionInProgress = true) }
            val result = pharmacyRepository.markAsDispensed(
                queueEntryId = entry.id,
                prescriptionId = prescription.id,
                totalCost = totalCost
            )
            result.fold(
                onSuccess = {
                    _state.update { it.copy(
                        actionInProgress = false,
                        currentPrescription = null,
                        currentQueueEntry = null
                    ) }
                    refresh()
                },
                onFailure = { e ->
                    _state.update { it.copy(
                        actionInProgress = false,
                        error = "Failed to dispense: ${e.message}"
                    ) }
                }
            )
        }
    }

    fun dismissError() = _state.update { it.copy(error = null) }

    override fun onCleared() {
        super.onCleared()
        autoPollJob?.cancel()
    }
}