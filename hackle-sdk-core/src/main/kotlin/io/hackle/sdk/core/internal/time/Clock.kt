package io.hackle.sdk.core.internal.time

interface Clock {

    fun currentMillis(): Long

    fun tick(): Long

    companion object {
        val SYSTEM = object : Clock {
            override fun currentMillis(): Long {
                return System.currentTimeMillis()
            }

            override fun tick(): Long {
                return System.nanoTime()
            }
        }
    }
}
