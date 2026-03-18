package org.sammomanyi.mediaccess.app

import kotlinx.datetime.Clock
//import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

//object DateProvider {
//    fun today(): String {
//        val now = Clock.System.now()
//        val date = now.toLocalDateTime(TimeZone.UTC) // 🔥 force UTC
//        return "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${
//            date.dayOfMonth.toString().padStart(2, '0')
//        }"
//    }
//}

object DateProvider {
    fun today(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("Africa/Nairobi")
        return sdf.format(Date())
    }
}