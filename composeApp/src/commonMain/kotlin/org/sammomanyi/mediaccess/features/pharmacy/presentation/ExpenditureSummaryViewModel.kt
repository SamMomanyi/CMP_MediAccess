package org.sammomanyi.mediaccess.features.pharmacy.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GetCurrentUserUseCase
import org.sammomanyi.mediaccess.features.pharmacy.data.PharmacyQueueRepository
import org.sammomanyi.mediaccess.features.pharmacy.data.PrescriptionRepository
import org.sammomanyi.mediaccess.features.pharmacy.domain.model.Prescription

class ExpenditureSummaryViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val pharmacyQueueRepository: PharmacyQueueRepository,
    private val prescriptionRepository: PrescriptionRepository
) : ViewModel() {

    private val _prescription = MutableStateFlow<Prescription?>(null)
    val prescription: StateFlow<Prescription?> = _prescription.asStateFlow()

    init {
        loadPrescription()
    }

    private fun loadPrescription() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().firstOrNull() ?: return@launch
            val queueEntry = pharmacyQueueRepository.observePatientPharmacyQueue(user.id).firstOrNull() ?: return@launch

            prescriptionRepository.observePrescription(queueEntry.prescriptionId).collect { presc ->
                _prescription.value = presc
            }
        }
    }
}