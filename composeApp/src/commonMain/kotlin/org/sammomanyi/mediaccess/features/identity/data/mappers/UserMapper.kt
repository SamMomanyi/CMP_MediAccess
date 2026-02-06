package org.sammomanyi.mediaccess.features.identity.data.mappers

import org.sammomanyi.mediaccess.features.identity.data.local.UserEntity
import org.sammomanyi.mediaccess.features.identity.domain.model.User

fun UserEntity.toUser(): User {
    return User(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        balance = balance,
        medicalId = medicalId
    )
}

fun User.toUserEntity(): UserEntity {
    return UserEntity(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        balance = balance,
        medicalId = medicalId
    )
}