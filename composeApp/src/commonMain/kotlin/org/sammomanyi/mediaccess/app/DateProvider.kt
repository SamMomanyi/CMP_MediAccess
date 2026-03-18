package org.sammomanyi.mediaccess.app

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object DateProvider {
    fun today(): String {
        val now = Clock.System.now()
        val date = now.toLocalDateTime(TimeZone.UTC) // 🔥 force UTC
        return "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${
            date.dayOfMonth.toString().padStart(2, '0')
        }"
    }
}