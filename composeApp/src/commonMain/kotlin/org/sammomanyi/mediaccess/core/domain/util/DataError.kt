package org.sammomanyi.mediaccess.core.domain.util


sealed interface DataError : Error {
    //we can have two types of errors that is Remote error and local error
    //the remote error is such as the one from server, the local error could be where the database is filled
    //inside this enum class we try to think of the type of remote errors we can have
    enum class Remote: DataError{
        REQUEST_TIMEOUT,
        TOO_MANY_REQUEST,
        NO_INTERNET,
        SERVER,
        SERIALIZATION,
        UNKNOWN
    }
    enum class Local: DataError{
        DISK_FULL,
        UNKNOWN
    }
}