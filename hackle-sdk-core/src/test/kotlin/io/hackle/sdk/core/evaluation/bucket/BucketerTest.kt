package io.hackle.sdk.core.evaluation

import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.model.Bucket
import io.hackle.sdk.core.model.HackleUser
import io.hackle.sdk.core.model.Slot
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isNotNull
import strikt.assertions.isSameInstanceAs
import java.nio.file.Files
import java.nio.file.Paths

/**
 * @author Yong
 */
internal class BucketerTest {

    @Nested
    inner class Bucketing {

        private val sut = spyk<Bucketer>()

        @Test
        fun `버켓정보로 계산된 슬롯번호로 슬롯을 가져온다`() {
            // given
            val slot = mockk<Slot>()
            val bucket = spyk(Bucket(32, 10000, emptyList())) {
                every { getSlotOrNull(any()) } returns slot
            }

            val userId = "abc"
            val slotNumber = 5123
            every { sut.calculateSlotNumber(1, 10000, userId) } returns slotNumber

            val user = HackleUser.of(userId)

            // when
            val actual = sut.bucketing(bucket, user)

            //then
            expectThat(actual)
                .isNotNull()
                .isSameInstanceAs(slot)
        }
    }

    @Nested
    inner class CalculateSlotNumber {

        private val sut = Bucketer()

        @Test
        fun `bucketing_all`() {
            Files.lines(Paths.get("src/test/resources/bucketing_all.csv"))
                .map { Data.from(it) }
                .forEach { check(it) }
        }

        @Test
        fun `bucketing_alphabetic`() {
            Files.lines(Paths.get("src/test/resources/bucketing_alphabetic.csv"))
                .map { Data.from(it) }
                .forEach { check(it) }
        }


        @Test
        fun `bucketing_alphanumeric`() {
            Files.lines(Paths.get("src/test/resources/bucketing_alphanumeric.csv"))
                .map { Data.from(it) }
                .forEach { check(it) }
        }

        @Test
        fun `bucketing_numeric`() {
            Files.lines(Paths.get("src/test/resources/bucketing_numeric.csv"))
                .map { Data.from(it) }
                .forEach { check(it) }
        }

        @Test
        fun `bucketing_uuid`() {
            Files.lines(Paths.get("src/test/resources/bucketing_uuid.csv"))
                .map { Data.from(it) }
                .forEach { check(it) }
        }


        private fun check(data: Data) {
            Assertions.assertEquals(data.slotNumber, sut.calculateSlotNumber(data.seed, data.slotSize, data.value))
        }
    }

    private class Data(
        val seed: Int,
        val slotSize: Int,
        val value: String,
        val slotNumber: Int
    ) {

        companion object {
            fun from(line: String): Data {
                return line.split(",").let {
                    Data(
                        seed = it[0].toInt(),
                        slotSize = it[1].toInt(),
                        value = it[2],
                        slotNumber = it[3].toInt()
                    )
                }
            }
        }
    }
}
