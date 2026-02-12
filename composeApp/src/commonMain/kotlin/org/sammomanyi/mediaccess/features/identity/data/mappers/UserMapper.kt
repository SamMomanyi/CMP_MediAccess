package org.sammomanyi.mediaccess.features.identity.data.mappers

import kotlinx.datetime.LocalDate
import org.sammomanyi.mediaccess.features.identity.data.local.UserEntity
import org.sammomanyi.mediaccess.features.identity.domain.model.User
import org.sammomanyi.mediaccess.features.identity.domain.model.Gender
import org.sammomanyi.mediaccess.features.identity.domain.model.UserRole

fun UserEntity.toUser(): User {
    return User(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        password = password,
        phoneNumber = phoneNumber,
        dateOfBirth = dateOfBirth, // Already a string
        gender = gender,           // Already a string
        role = role,               // Already a string
        medicalId = medicalId,
        nationalId = nationalId,
        balance = balance,
        profileImageUrl = profileImageUrl,
        isEmailVerified = isEmailVerified,
        createdAt = createdAt
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        password = password,
        phoneNumber = phoneNumber,
        dateOfBirth = dateOfBirth, // Already a string
        gender = gender,           // Already a string
        role = role,               // Already a string
        medicalId = medicalId,
        nationalId = nationalId,
        balance = balance,
        profileImageUrl = profileImageUrl,
        isEmailVerified = isEmailVerified,
        createdAt = createdAt
    )
}

// Helper extensions for UI layer
fun User.getGenderEnum(): Gender = Gender.valueOf(gender)
fun User.getRoleEnum(): UserRole = UserRole.valueOf(role)
fun User.getDateOfBirthLocal(): LocalDate = LocalDate.parse(dateOfBirth)