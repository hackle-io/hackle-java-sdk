package io.hackle.sdk.core.internal.time

import io.hackle.sdk.core.model.DayOfWeek
import java.util.Calendar
import java.util.TimeZone

internal object TimeUtil {
    private val UTC_TIMEZONE = TimeZone.getTimeZone("UTC")

    fun dayOfWeek(timestamp: Long): DayOfWeek {
        val calendar = Calendar.getInstance(UTC_TIMEZONE)
        calendar.timeInMillis = timestamp
        return when (val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> DayOfWeek.MONDAY
            Calendar.TUESDAY -> DayOfWeek.TUESDAY
            Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
            Calendar.THURSDAY -> DayOfWeek.THURSDAY
            Calendar.FRIDAY -> DayOfWeek.FRIDAY
            Calendar.SATURDAY -> DayOfWeek.SATURDAY
            Calendar.SUNDAY -> DayOfWeek.SUNDAY
            else -> throw IllegalStateException("Invalid Calendar day value: $dayOfWeek")
        }
    }

    fun midnight(timestamp: Long): Long {
        val calendar = Calendar.getInstance(UTC_TIMEZONE)
        calendar.timeInMillis = timestamp

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }
}
