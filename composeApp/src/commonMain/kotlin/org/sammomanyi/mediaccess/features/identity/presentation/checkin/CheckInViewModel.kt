package org.sammomanyi.mediaccess.features.identity.presentation.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.cover.data.CoverRepository
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverStatus
import org.sammomanyi.mediaccess.features.identity.domain.model.VisitCode
import org.sammomanyi.mediaccess.features.identity.domain.model.VisitPurpose
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GenerateVisitCodeUseCase
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GetCurrentUserUseCase
import org.sammomanyi.mediaccess.features.queue.data.QueueRepository
import org.sammomanyi.mediaccess.features.queue.domain.model.QueueStatus

sealed class CoverGateState {
    object Checking : CoverGateState()
    object Approved : CoverGateState()
    object Pending : CoverGateState()
    object None : CoverGateState()
    data class Error(val message: String) : CoverGateState()
}

sealed class CheckInCodeState {
    object Idle : CheckInCodeState()
    object Generating : CheckInCodeState()
    data class Ready(val visitCode: VisitCode, val secondsRemaining: Long) : CheckInCodeState()
    object Expired : CheckInCodeState()
    data class GenerationFailed(val message: String) : CheckInCodeState()
}

sealed class QueueState {
    object NotQueued : QueueState()
    data class Waiting(
        val position: Int,
        val doctorName: String,
        val roomNumber: String,
        val purpose: String
    ) : QueueState()
    data class YourTurn(
        val doctorName: String,
        val roomNumber: String
    ) : QueueState()
    object Done : QueueState()
}

data class CheckInUiState(
    val coverGate: CoverGateState = CoverGateState.Checking,
    val codeState: CheckInCodeState = CheckInCodeState.Idle,
    val queueState: QueueState = QueueState.NotQueued,
    val insuranceName: String? = null,
    val memberNumber: String? = null,
    val triggerHaptic: Boolean = false,
    val shouldNavigateToWaitingRoom: Boolean = false
)

class CheckInViewModel(
    private val generateVisitCodeUseCase: GenerateVisitCodeUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val coverRepository: CoverRepository,
    private val queueRepository: QueueRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CheckInUiState())
    val state: StateFlow<CheckInUiState> = _state.asStateFlow()

    private var countdownJob: Job? = null
    private var queueListenerJob: Job? = null
    private var currentUserId: String? = null

    init {
        checkCoverStatus()
    }

    fun checkCoverStatus() {
        viewModelScope.launch {
            _state.update { it.copy(coverGate = CoverGateState.Checking) }

            val user = getCurrentUserUseCase().firstOrNull()
            val userId = user?.id

            if (userId == null) {
                _state.update { it.copy(coverGate = CoverGateState.Error("Please log in to check in.")) }
                return@launch
            }

            currentUserId = userId
            coverRepository.syncFromFirestore(userId)

            val allRequests = coverRepository.getAllRequests().firstOrNull() ?: emptyList()
            val latestRequest = allRequests
                .filter { it.userId == userId }
                .sortedByDescending { it.submittedAt }
                .firstOrNull()

            when {
                latestRequest == null -> _state.update { it.copy(coverGate = CoverGateState.None) }
                latestRequest.status == CoverStatus.APPROVED -> _state.update { it.copy(
                    coverGate = CoverGateState.Approved,
                    insuranceName = latestRequest.insuranceName,
                    memberNumber = latestRequest.memberNumber
                ) }
                latestRequest.status == CoverStatus.PENDING -> _state.update { it.copy(coverGate = CoverGateState.Pending) }
                else -> _state.update { it.copy(coverGate = CoverGateState.None) }
            }
        }
    }

    fun generateCode(purpose: VisitPurpose) {
        if (_state.value.coverGate !is CoverGateState.Approved) return
        val userId = currentUserId ?: return

        viewModelScope.launch {
            _state.update { it.copy(codeState = CheckInCodeState.Generating) }

            // Check if already has active queue entry
            val activeEntry = queueRepository.observePatientQueueEntry(userId).firstOrNull()
            if (activeEntry != null) {
                _state.update { it.copy(
                    codeState = CheckInCodeState.GenerationFailed(
                        "You already have an active check-in. Please wait for your turn."
                    )
                ) }
                return@launch
            }

            val result = generateVisitCodeUseCase(userId, purpose)
            when (result) {
                is Result.Success -> {
                    _state.update { it.copy(
                        codeState = CheckInCodeState.Ready(
                            visitCode = result.data,
                            secondsRemaining = 900L
                        ),
                        queueState = QueueState.NotQueued
                    ) }
                    startCountdown(result.data)
                    startQueueListener(userId)
                }
                is Result.Error -> {
                    _state.update { it.copy(
                        codeState = CheckInCodeState.GenerationFailed(
                            "Could not generate code. Please try again."
                        )
                    ) }
                }
            }
        }
    }

    private fun startQueueListener(userId: String) {
        queueListenerJob?.cancel()
        queueListenerJob = viewModelScope.launch {
            queueRepository.observePatientQueueEntry(userId).collect { entry ->
                val newQueueState = when {
                    entry == null -> QueueState.NotQueued
                    entry.status == QueueStatus.DONE.name -> QueueState.Done
                    entry.status == QueueStatus.IN_PROGRESS.name -> {
                        QueueState.YourTurn(
                            doctorName = entry.doctorName,
                            roomNumber = entry.roomNumber
                        )
                    }
                    entry.status == QueueStatus.WAITING.name -> {
                        QueueState.Waiting(
                            position = entry.queuePosition,
                            doctorName = entry.doctorName,
                            roomNumber = entry.roomNumber,
                            purpose = entry.purpose
                        )
                    }
                    else -> QueueState.NotQueued
                }

                val wasNotQueued = _state.value.queueState is QueueState.NotQueued
                val isNowQueued = newQueueState !is QueueState.NotQueued
                val wasAlreadyYourTurn = _state.value.queueState is QueueState.YourTurn
                val isNowYourTurn = newQueueState is QueueState.YourTurn

                _state.update { it.copy(
                    queueState = newQueueState,
                    shouldNavigateToWaitingRoom = wasNotQueued && isNowQueued,  // ✅ Navigate on first queue entry
                    triggerHaptic = isNowYourTurn && !wasAlreadyYourTurn
                ) }
            }
        }
    }

    fun onNavigatedToWaitingRoom() {
        _state.update { it.copy(shouldNavigateToWaitingRoom = false) }
    }

    fun onHapticTriggered() {
        _state.update { it.copy(triggerHaptic = false) }
    }

    private fun startCountdown(code: VisitCode) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            val expiresAt = code.expiresAt.toEpochMilliseconds()
            while (isActive) {
                val remaining = (expiresAt - System.currentTimeMillis()) / 1000
                if (remaining <= 0) {
                    _state.update { it.copy(codeState = CheckInCodeState.Expired) }
                    break
                }
                val current = _state.value.codeState
                if (current is CheckInCodeState.Ready) {
                    _state.update { it.copy(codeState = current.copy(secondsRemaining = remaining)) }
                }
                delay(1000)
            }
        }
    }

    fun resetCode() {
        countdownJob?.cancel()
        queueListenerJob?.cancel()
        _state.update { it.copy(
            codeState = CheckInCodeState.Idle,
            queueState = QueueState.NotQueued
        ) }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        queueListenerJob?.cancel()
    }
}