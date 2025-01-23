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

        /**
         * days to millis
         * @param day
         */
        fun daysToMillis(day: Int): Long {
            // 하루의 밀리초 값 (24시간 * 60분 * 60초 * 1000밀리초)
            return day * 24 * 60 * 60 * 1000L
        }
    }
}
