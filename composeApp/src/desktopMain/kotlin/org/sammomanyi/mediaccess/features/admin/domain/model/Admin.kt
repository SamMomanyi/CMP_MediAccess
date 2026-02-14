package org.sammomanyi.mediaccess.features.admin.domain.model

data class Admin(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val createdAt: Long
)