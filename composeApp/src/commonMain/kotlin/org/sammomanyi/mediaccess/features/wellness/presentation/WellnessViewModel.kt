package org.sammomanyi.mediaccess.features.wellness.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.features.wellness.data.WellnessRepository
import org.sammomanyi.mediaccess.features.wellness.domain.model.WellnessData

data class WellnessState(
    val data: WellnessData = WellnessData(),
    val isOnboarded: Boolean = false,
    val isLoading: Boolean = true,
    val hasStepSensor: Boolean = false,
    val liveSteps: Int = 0
)

sealed interface WellnessAction {
    data object OnCompleteOnboarding : WellnessAction
    data object OnAddHydration : WellnessAction
    data object OnRemoveHydration : WellnessAction
    data class OnToggleHabit(val habitId: String) : WellnessAction
    data class OnStepsUpdated(val steps: Int) : WellnessAction
}

class WellnessViewModel(
    private val repository: WellnessRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WellnessState())
    val state = _state.asStateFlow()

    init {
        loadWellnessData()
    }

    private fun loadWellnessData() {
        viewModelScope.launch {
            combine(
                repository.isOnboarded,
                repository.wellnessData
            ) { onboarded, data ->
                WellnessState(
                    data = data,
                    isOnboarded = onboarded,
                    isLoading = false,
                    liveSteps = data.steps
                )
            }.collectLatest { newState ->
                _state.update { newState }
            }
        }
    }

    fun onAction(action: WellnessAction) {
        when (action) {
            WellnessAction.OnCompleteOnboarding -> {
                viewModelScope.launch {
                    repository.setOnboarded(true)
                }
            }
            WellnessAction.OnAddHydration -> {
                viewModelScope.launch {
                    repository.addHydrationGlass()
                }
            }
            WellnessAction.OnRemoveHydration -> {
                viewModelScope.launch {
                    repository.removeHydrationGlass()
                }
            }
            is WellnessAction.OnToggleHabit -> {
                viewModelScope.launch {
                    repository.toggleHabit(action.habitId, _state.value.data.habits)
                }
            }
            is WellnessAction.OnStepsUpdated -> {
                viewModelScope.launch {
                    repository.updateSteps(action.steps)
                }
            }
        }
    }
}