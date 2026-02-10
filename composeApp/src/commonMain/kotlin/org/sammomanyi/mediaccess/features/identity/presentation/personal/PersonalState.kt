package org.sammomanyi.mediaccess.features.identity.presentation.personal

import org.sammomanyi.mediaccess.features.identity.domain.model.User

data class PersonalState(
    val user: User? = null,
    val recoveryEmails: List<String> = emptyList(),
    val recoveryPhones: List<String> = emptyList(),
    val selectedTopics: List<String> = listOf("Cancer", "Diabetes", "Hypertension", "Mental Health"),
    val isLoading: Boolean = false
)