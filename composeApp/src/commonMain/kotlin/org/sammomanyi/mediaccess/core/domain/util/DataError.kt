package org.sammomanyi.mediaccess.core.domain.util

sealed interface DataError : Error{
    enum class Network : DataError {
        REQUEST_TIMEOUT,
        NO_INTERNET,
        SERVER_ERROR,
        SERIALIZATION,
        UNAUTHORIZED,
        UNKNOWN,
        NOT_FOUND
    }

    enum class Local : DataError {
        DISK_FULL,
        DATABASE_ERROR,
        UNKNOWN
    }

    // Add validation errors
    enum class Validation : DataError {
        EMPTY_FIELD,
        INVALID_EMAIL,
        PASSWORD_TOO_SHORT,
        PASSWORD_TOO_WEAK,
        INVALID_PHONE_NUMBER,
        INVALID_DATE_OF_BIRTH,
        USER_UNDER_AGE,
        INVALID_MEDICAL_ID,
        INVALID_VISIT_CODE,   // Add this
        VISIT_CODE_EXPIRED,   // Add this
        VISIT_CODE_USED       // Add this
    }

    // Add authentication errors
    enum class Auth : DataError {
        INVALID_CREDENTIALS,
        USER_NOT_FOUND,
        EMAIL_ALREADY_EXISTS,
        ACCOUNT_DISABLED,
        SESSION_EXPIRED
    }
}