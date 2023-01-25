package io.hackle.sdk.core.internal.time

import java.util.concurrent.TimeUnit


private const val C0 = 1L
private const val C1 = C0 * 1000L
private const val C2 = C1 * 1000L
private const val C3 = C2 * 1000L
private const val C4 = C3 * 60L
private const val C5 = C4 * 60L
private const val C6 = C5 * 24L

internal fun TimeUnit.convert(sourceAmount: Double, sourceUnit: TimeUnit): Double {
    return when (sourceUnit) {
        TimeUnit.NANOSECONDS -> nanosToUnit(sourceAmount, this)
        TimeUnit.MICROSECONDS -> microsToUnit(sourceAmount, this)
        TimeUnit.MILLISECONDS -> millisToUnit(sourceAmount, this)
        TimeUnit.SECONDS -> secondsToUnit(sourceAmount, this)
        TimeUnit.MINUTES -> minutesToUnit(sourceAmount, this)
        TimeUnit.HOURS -> hoursToUnit(sourceAmount, this)
        TimeUnit.DAYS -> daysToUnit(sourceAmount, this)
    }
}

internal fun nanosToUnit(nanos: Double, destinationUnit: TimeUnit): Double {
    return when (destinationUnit) {
        TimeUnit.NANOSECONDS -> nanos
        TimeUnit.MICROSECONDS -> nanos / (C1 / C0)
        TimeUnit.MILLISECONDS -> nanos / (C2 / C0)
        TimeUnit.SECONDS -> nanos / (C3 / C0)
        TimeUnit.MINUTES -> nanos / (C4 / C0)
        TimeUnit.HOURS -> nanos / (C5 / C0)
        TimeUnit.DAYS -> nanos / (C6 / C0)
    }
}

internal fun microsToUnit(micros: Double, destinationUnit: TimeUnit): Double {
    return when (destinationUnit) {
        TimeUnit.NANOSECONDS -> micros * (C1 / C0)
        TimeUnit.MICROSECONDS -> micros
        TimeUnit.MILLISECONDS -> micros / (C2 / C1)
        TimeUnit.SECONDS -> micros / (C3 / C1)
        TimeUnit.MINUTES -> micros / (C4 / C1)
        TimeUnit.HOURS -> micros / (C5 / C1)
        TimeUnit.DAYS -> micros / (C6 / C1)
    }
}

internal fun millisToUnit(millis: Double, destinationUnit: TimeUnit): Double {
    return when (destinationUnit) {
        TimeUnit.NANOSECONDS -> millis * (C2 / C0)
        TimeUnit.MICROSECONDS -> millis * (C2 / C1)
        TimeUnit.MILLISECONDS -> millis
        TimeUnit.SECONDS -> millis / (C3 / C2)
        TimeUnit.MINUTES -> millis / (C4 / C2)
        TimeUnit.HOURS -> millis / (C5 / C2)
        TimeUnit.DAYS -> millis / (C6 / C2)
    }
}

internal fun secondsToUnit(seconds: Double, destinationUnit: TimeUnit): Double {
    return when (destinationUnit) {
        TimeUnit.NANOSECONDS -> seconds * (C3 / C0)
        TimeUnit.MICROSECONDS -> seconds * (C3 / C1)
        TimeUnit.MILLISECONDS -> seconds * (C3 / C2)
        TimeUnit.SECONDS -> seconds
        TimeUnit.MINUTES -> seconds / (C4 / C3)
        TimeUnit.HOURS -> seconds / (C5 / C3)
        TimeUnit.DAYS -> seconds / (C6 / C3)
    }
}

internal fun minutesToUnit(minutes: Double, destinationUnit: TimeUnit): Double {
    return when (destinationUnit) {
        TimeUnit.NANOSECONDS -> minutes * (C4 / C0)
        TimeUnit.MICROSECONDS -> minutes * (C4 / C1)
        TimeUnit.MILLISECONDS -> minutes * (C4 / C2)
        TimeUnit.SECONDS -> minutes * (C4 / C3)
        TimeUnit.MINUTES -> minutes
        TimeUnit.HOURS -> minutes / (C5 / C4)
        TimeUnit.DAYS -> minutes / (C6 / C4)
    }
}

internal fun hoursToUnit(hours: Double, destinationUnit: TimeUnit): Double {
    return when (destinationUnit) {
        TimeUnit.NANOSECONDS -> hours * (C5 / C0)
        TimeUnit.MICROSECONDS -> hours * (C5 / C1)
        TimeUnit.MILLISECONDS -> hours * (C5 / C2)
        TimeUnit.SECONDS -> hours * (C5 / C3)
        TimeUnit.MINUTES -> hours * (C5 / C4)
        TimeUnit.HOURS -> hours
        TimeUnit.DAYS -> hours / (C6 / C5)
    }
}

internal fun daysToUnit(days: Double, destinationUnit: TimeUnit): Double {
    return when (destinationUnit) {
        TimeUnit.NANOSECONDS -> days * (C6 / C0)
        TimeUnit.MICROSECONDS -> days * (C6 / C1)
        TimeUnit.MILLISECONDS -> days * (C6 / C2)
        TimeUnit.SECONDS -> days * (C6 / C3)
        TimeUnit.MINUTES -> days * (C6 / C4)
        TimeUnit.HOURS -> days * (C6 / C5)
        TimeUnit.DAYS -> days
    }
}
