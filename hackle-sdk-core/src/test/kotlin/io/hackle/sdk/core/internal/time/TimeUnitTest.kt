package io.hackle.sdk.core.internal.time

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit.*

internal class TimeUnitTest {

    @Test
    fun `convert`() {
        assertEquals(1.0, NANOSECONDS.convert(1.0, NANOSECONDS))
        assertEquals(1.0, MICROSECONDS.convert(1.0, MICROSECONDS))
        assertEquals(1.0, MILLISECONDS.convert(1.0, MILLISECONDS))
        assertEquals(1.0, SECONDS.convert(1.0, SECONDS))
        assertEquals(1.0, MINUTES.convert(1.0, MINUTES))
        assertEquals(1.0, HOURS.convert(1.0, HOURS))
        assertEquals(1.0, DAYS.convert(1.0, DAYS))
    }

    @Test
    fun nanos() {
        assertEquals(1.0 * (1L), nanosToUnit(1.0, NANOSECONDS))
        assertEquals(1.0 / (1L * 1_000L), nanosToUnit(1.0, MICROSECONDS))
        assertEquals(1.0 / (1L * 1_000_000L), nanosToUnit(1.0, MILLISECONDS))
        assertEquals(1.0 / (1L * 1_000_000_000L), nanosToUnit(1.0, SECONDS))
        assertEquals(1.0 / (1L * 1_000_000_000L * 60L), nanosToUnit(1.0, MINUTES))
        assertEquals(1.0 / (1L * 1_000_000_000L * 60L * 60L), nanosToUnit(1.0, HOURS))
        assertEquals(1.0 / (1L * 1_000_000_000L * 60L * 60L * 24L), nanosToUnit(1.0, DAYS))
    }

    @Test
    fun `micros`() {
        assertEquals(1.0 * (1L * 1_000L), microsToUnit(1.0, NANOSECONDS))
        assertEquals(1.0 * (1L), microsToUnit(1.0, MICROSECONDS))
        assertEquals(1.0 / (1L * 1_000L), microsToUnit(1.0, MILLISECONDS))
        assertEquals(1.0 / (1L * 1_000_000L), microsToUnit(1.0, SECONDS))
        assertEquals(1.0 / (1L * 1_000_000L * 60L), microsToUnit(1.0, MINUTES))
        assertEquals(1.0 / (1L * 1_000_000L * 60L * 60L), microsToUnit(1.0, HOURS))
        assertEquals(1.0 / (1L * 1_000_000L * 60L * 60L * 24L), microsToUnit(1.0, DAYS))
    }

    @Test
    fun `millis`() {
        assertEquals(1.0 * (1L * 1_000_000L), millisToUnit(1.0, NANOSECONDS))
        assertEquals(1.0 * (1L * 1_000L), millisToUnit(1.0, MICROSECONDS))
        assertEquals(1.0 * (1L), millisToUnit(1.0, MILLISECONDS))
        assertEquals(1.0 / (1L * 1_000L), millisToUnit(1.0, SECONDS))
        assertEquals(1.0 / (1L * 1_000L * 60L), millisToUnit(1.0, MINUTES))
        assertEquals(1.0 / (1L * 1_000L * 60L * 60L), millisToUnit(1.0, HOURS))
        assertEquals(1.0 / (1L * 1_000L * 60L * 60L * 24L), millisToUnit(1.0, DAYS))
    }

    @Test
    fun `seconds`() {
        assertEquals(1.0 * (1L * 1_000_000_000L), secondsToUnit(1.0, NANOSECONDS))
        assertEquals(1.0 * (1L * 1_000_000L), secondsToUnit(1.0, MICROSECONDS))
        assertEquals(1.0 * (1L * 1_000L), secondsToUnit(1.0, MILLISECONDS))
        assertEquals(1.0 * (1L), secondsToUnit(1.0, SECONDS))
        assertEquals(1.0 / (1L * 60L), secondsToUnit(1.0, MINUTES))
        assertEquals(1.0 / (1L * 60L * 60L), secondsToUnit(1.0, HOURS))
        assertEquals(1.0 / (1L * 60L * 60L * 24L), secondsToUnit(1.0, DAYS))
    }

    @Test
    fun `minutes`() {
        assertEquals(1.0 * (1L * 1_000_000_000L * 60L), minutesToUnit(1.0, NANOSECONDS))
        assertEquals(1.0 * (1L * 1_000_000L * 60L), minutesToUnit(1.0, MICROSECONDS))
        assertEquals(1.0 * (1L * 1_000L * 60L), minutesToUnit(1.0, MILLISECONDS))
        assertEquals(1.0 * (1L * 60L), minutesToUnit(1.0, SECONDS))
        assertEquals(1.0 * (1L), minutesToUnit(1.0, MINUTES))
        assertEquals(1.0 / (1L * 60L), minutesToUnit(1.0, HOURS))
        assertEquals(1.0 / (1L * 60L * 24L), minutesToUnit(1.0, DAYS))
    }

    @Test
    fun `hours`() {
        assertEquals(1.0 * (1L * 1_000_000_000L * 60L * 60L), hoursToUnit(1.0, NANOSECONDS))
        assertEquals(1.0 * (1L * 1_000_000L * 60L * 60L), hoursToUnit(1.0, MICROSECONDS))
        assertEquals(1.0 * (1L * 1_000L * 60L * 60L), hoursToUnit(1.0, MILLISECONDS))
        assertEquals(1.0 * (1L * 60L * 60L), hoursToUnit(1.0, SECONDS))
        assertEquals(1.0 * (1L * 60L), hoursToUnit(1.0, MINUTES))
        assertEquals(1.0 * (1L), hoursToUnit(1.0, HOURS))
        assertEquals(1.0 / (1L * 24L), hoursToUnit(1.0, DAYS))
    }

    @Test
    fun `days`() {
        assertEquals(1.0 * (1L * 1_000_000_000L * 60L * 60L * 24L), daysToUnit(1.0, NANOSECONDS))
        assertEquals(1.0 * (1L * 1_000_000L * 60L * 60L * 24L), daysToUnit(1.0, MICROSECONDS))
        assertEquals(1.0 * (1L * 1_000L * 60L * 60L * 24L), daysToUnit(1.0, MILLISECONDS))
        assertEquals(1.0 * (1L * 60L * 60L * 24L), daysToUnit(1.0, SECONDS))
        assertEquals(1.0 * (1L * 60L * 24L), daysToUnit(1.0, MINUTES))
        assertEquals(1.0 * (1L * 24L), daysToUnit(1.0, HOURS))
        assertEquals(1.0 * (1L), daysToUnit(1.0, DAYS))
    }
}