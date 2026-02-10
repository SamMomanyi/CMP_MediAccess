package org.sammomanyi.mediaccess.features.identity.presentation.home

sealed interface HomeAction {
    data object OnGenerateVisitCode : HomeAction
    data object OnRefreshVisitCode : HomeAction
}