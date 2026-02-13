package org.sammomanyi.mediaccess.features.cover.presentation

import org.sammomanyi.mediaccess.features.cover.domain.model.CoverLinkRequest

data class CoverState(
    val requests: List<CoverLinkRequest> = emptyList(),
    val userEmail: String = "",
    val userId: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)