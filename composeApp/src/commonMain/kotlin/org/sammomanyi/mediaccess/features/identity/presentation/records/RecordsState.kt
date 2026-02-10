package org.sammomanyi.mediaccess.features.identity.presentation.records

import org.sammomanyi.mediaccess.core.presentation.UiText
import org.sammomanyi.mediaccess.features.identity.domain.model.MedicalRecord

data class RecordsState(
    val records: List<MedicalRecord> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val errorMessage: UiText? = null
)