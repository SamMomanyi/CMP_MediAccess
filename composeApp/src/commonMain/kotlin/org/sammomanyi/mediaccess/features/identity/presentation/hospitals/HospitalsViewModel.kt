package org.sammomanyi.mediaccess.features.identity.presentation.hospitals


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.core.presentation.UiText
import org.sammomanyi.mediaccess.features.identity.domain.use_case.*

class HospitalsViewModel(
    private val getHospitalsUseCase: GetHospitalsUseCase,
    private val syncHospitalsUseCase: SyncHospitalsUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val generateVisitCodeUseCase: GenerateVisitCodeUseCase,
    private val bookAppointmentUseCase: BookAppointmentUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HospitalsState())
    val state = _state.asStateFlow()

    init {
        loadHospitals()
    }

    private fun loadHospitals() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Sync from Firestore first
            syncHospitals()

            // Then observe local hospitals
            getHospitalsUseCase().collectLatest { hospitals ->
                _state.update {
                    it.copy(
                        hospitals = hospitals,
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun syncHospitals() {
        _state.update { it.copy(isSyncing = true) }

        when (syncHospitalsUseCase()) {
            is Result.Success -> {
                _state.update { it.copy(isSyncing = false) }
            }
            is Result.Error -> {
                _state.update {
                    it.copy(
                        isSyncing = false,
                        errorMessage = UiText.DynamicString("Failed to sync hospitals")
                    )
                }
            }
        }
    }

    fun onAction(action: HospitalsAction) {
        when (action) {
            HospitalsAction.OnRefresh -> {
                viewModelScope.launch { syncHospitals() }
            }
            is HospitalsAction.OnHospitalClick -> {
                _state.update {
                    it.copy(
                        selectedHospital = action.hospital,
                        showBookingDialog = true
                    )
                }
            }
            HospitalsAction.OnDismissBooking -> {
                _state.update {
                    it.copy(
                        showBookingDialog = false,
                        selectedHospital = null,
                        bookingPurpose = "",
                        errorMessage = null
                    )
                }
            }
            is HospitalsAction.OnPurposeChange -> {
                _state.update { it.copy(bookingPurpose = action.purpose) }
            }
            HospitalsAction.OnConfirmBooking -> {
                bookAppointment()
            }
        }
    }

    private fun bookAppointment() {
        viewModelScope.launch {
            val hospital = state.value.selectedHospital ?: return@launch
            val purpose = state.value.bookingPurpose

            if (purpose.isBlank()) {
                _state.update {
                    it.copy(errorMessage = UiText.DynamicString("Please enter a purpose"))
                }
                return@launch
            }

            _state.update { it.copy(isLoading = true) }

            val user = getProfileUseCase().firstOrNull()
            if (user == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = UiText.DynamicString("Please log in first")
                    )
                }
                return@launch
            }

            // Generate visit code
            val codeResult = generateVisitCodeUseCase(
                userId = user.id,
                purpose = org.sammomanyi.mediaccess.features.identity.domain.model.VisitPurpose.GENERAL_VISIT
            )

            when (codeResult) {
                is Result.Success -> {
                    val visitCode = codeResult.data

                    // Book appointment
                    val scheduledDate = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()

                    val bookingResult = bookAppointmentUseCase(
                        user = user,
                        hospitalId = hospital.id,
                        hospitalName = hospital.name,
                        visitCode = visitCode.code,
                        purpose = purpose,
                        scheduledDate = scheduledDate
                    )

                    when (bookingResult) {
                        is Result.Success -> {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    showBookingDialog = false,
                                    selectedHospital = null,
                                    bookingPurpose = "",
                                    successMessage = UiText.DynamicString(
                                        "Appointment booked! Visit code: ${visitCode.code}"
                                    )
                                )
                            }
                        }
                        is Result.Error -> {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = UiText.from(bookingResult.error)
                                )
                            }
                        }
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = UiText.from(codeResult.error)
                        )
                    }
                }
            }
        }
    }
}