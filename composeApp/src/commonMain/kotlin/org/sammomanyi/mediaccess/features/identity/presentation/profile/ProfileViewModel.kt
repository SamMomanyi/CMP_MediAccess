package org.sammomanyi.mediaccess.features.identity.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GetProfileUseCase

class ProfileViewModel(
    getProfileUseCase: GetProfileUseCase
) : ViewModel() {

    val userState = getProfileUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}