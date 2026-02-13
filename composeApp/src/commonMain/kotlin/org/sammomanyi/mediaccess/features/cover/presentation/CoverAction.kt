package org.sammomanyi.mediaccess.features.cover.presentation

sealed interface CoverAction {
    // Admin actions (desktop only)
    data class OnApproveRequest(val id: String) : CoverAction
    data class OnRejectRequest(val id: String, val note: String) : CoverAction
}