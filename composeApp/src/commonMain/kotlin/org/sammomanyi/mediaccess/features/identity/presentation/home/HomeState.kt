package org.sammomanyi.mediaccess.features.identity.presentation.home

import org.sammomanyi.mediaccess.core.presentation.UiText
import org.sammomanyi.mediaccess.features.identity.domain.model.User
import org.sammomanyi.mediaccess.features.identity.domain.model.VisitCode

data class HomeState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val activeVisitCode: VisitCode? = null, // Add this
    val errorMessage: UiText? = null

)