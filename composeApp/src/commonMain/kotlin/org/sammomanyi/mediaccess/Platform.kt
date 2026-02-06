package org.sammomanyi.mediaccess

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform