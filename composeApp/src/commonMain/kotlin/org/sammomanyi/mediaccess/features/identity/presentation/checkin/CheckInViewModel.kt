package org.sammomanyi.mediaccess.features.identity.presentation.checkin


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.cover.data.CoverRepository
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverStatus
import org.sammomanyi.mediaccess.features.identity.domain.model.VisitCode
import org.sammomanyi.mediaccess.features.identity.domain.model.VisitPurpose
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GenerateVisitCodeUseCase
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GetCurrentUserUseCase

// ── Cover gate state ──────────────────────────────────────────
sealed class CoverGateState {
    object Checking : CoverGateState()
    object Approved : CoverGateState()           // Can check in
    object Pending : CoverGateState()            // Submitted but awaiting admin
    object None : CoverGateState()               // Never submitted
    data class Error(val message: String) : CoverGateState()
}

// ── Code state ────────────────────────────────────────────────
sealed class CheckInCodeState {
    object Idle : CheckInCodeState()
    object Generating : CheckInCodeState()
    data class Ready(
        val visitCode: VisitCode,
        val secondsRemaining: Long
    ) : CheckInCodeState()
    object Expired : CheckInCodeState()
    data class GenerationFailed(val message: String) : CheckInCodeState()
}

data class CheckInUiState(
    val coverGate: CoverGateState = CoverGateState.Checking,
    val codeState: CheckInCodeState = CheckInCodeState.Idle,
    val insuranceName: String? = null,
    val memberNumber: String? = null
)

class CheckInViewModel(
    private val generateVisitCodeUseCase: GenerateVisitCodeUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val coverRepository: CoverRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CheckInUiState())
    val state: StateFlow<CheckInUiState> = _state.asStateFlow()

    private var countdownJob: Job? = null
    private var currentUserId: String? = null

    init {
        checkCoverStatus()
    }

    // ── Step 1: Check cover status before anything ────────────
    fun checkCoverStatus() {
        viewModelScope.launch {
            _state.value = _state.value.copy(coverGate = CoverGateState.Checking)

// 1. Get the Flow from the UseCase
            val userFlow = getCurrentUserUseCase()

            // 2. Unwrap the Flow to get the actual User object (or null)
            // We use .firstOrNull() to grab the current data from the stream
            val user = userFlow.firstOrNull()

            // 3. Now we have the direct User object. No Result wrapper needed.
            val userId = user?.id

            if (userId == null) {
                _state.value = _state.value.copy(
                    coverGate = CoverGateState.Error("Please log in to check in.")
                )
                return@launch
            }

            currentUserId = userId

            // Sync from Firestore before reading Room — this is what was missing
            coverRepository.syncFromFirestore(userId)

            // Get the user's most recent cover request from local Room DB
            // (synced from Firestore on app start via CoverRepository.syncFromFirestore)
            val allRequests = coverRepository.getAllRequests().first()
            val userRequests = allRequests
                .filter { it.userId == userId }
                .sortedByDescending { it.submittedAt }

            val latestRequest = userRequests.firstOrNull()

            when {
                latestRequest == null -> {
                    _state.value = _state.value.copy(coverGate = CoverGateState.None)
                }
                latestRequest.status == CoverStatus.APPROVED -> {
                    _state.value = _state.value.copy(
                        coverGate = CoverGateState.Approved,
                        insuranceName = latestRequest.insuranceName,
                        memberNumber = latestRequest.memberNumber
                    )
                }
                latestRequest.status == CoverStatus.PENDING -> {
                    _state.value = _state.value.copy(coverGate = CoverGateState.Pending)
                }
                latestRequest.status == CoverStatus.REJECTED -> {
                    // Rejected — treat as None, they need to resubmit
                    _state.value = _state.value.copy(coverGate = CoverGateState.None)
                }
                else -> {
                    _state.value = _state.value.copy(coverGate = CoverGateState.None)
                }
            }
        }
    }

    // ── Step 2: Generate code only if cover is approved ───────
    fun generateCode(purpose: VisitPurpose) {
        // Gate: only APPROVED covers can generate codes
        if (_state.value.coverGate !is CoverGateState.Approved) return

        val userId = currentUserId ?: return

        viewModelScope.launch {
            _state.value = _state.value.copy(codeState = CheckInCodeState.Generating)

            val result = generateVisitCodeUseCase(userId, purpose)
            when (result) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        codeState = CheckInCodeState.Ready(
                            visitCode = result.data,
                            secondsRemaining = 900L  // 15 minutes
                        )
                    )
                    startCountdown(result.data)
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        codeState = CheckInCodeState.GenerationFailed(
                            "Could not generate check-in code. Please try again."
                        )
                    )
                }
            }
        }
    }

    // ── Countdown timer ───────────────────────────────────────
    private fun startCountdown(code: VisitCode) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            val expiresAt = code.expiresAt.toEpochMilliseconds()
            while (isActive) {
                val remaining = (expiresAt - System.currentTimeMillis()) / 1000
                if (remaining <= 0) {
                    _state.value = _state.value.copy(codeState = CheckInCodeState.Expired)
                    break
                }
                val current = _state.value.codeState
                if (current is CheckInCodeState.Ready) {
                    _state.value = _state.value.copy(
                        codeState = current.copy(secondsRemaining = remaining)
                    )
                }
                delay(1000)
            }
        }
    }

    // ── Reset back to purpose picker ──────────────────────────
    fun resetCode() {
        countdownJob?.cancel()
        _state.value = _state.value.copy(codeState = CheckInCodeState.Idle)
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}