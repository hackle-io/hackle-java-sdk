package io.hackle.sdk.core.internal.time

interface Clock {
    /**
     * current time in millis
     *
     * utc 기준
     */
    fun currentMillis(): Long

    /**
     * current time in nanos
     *
     * utc 기준
     */
    fun tick(): Long

    companion object {
        /**
         * system clock
         */
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
