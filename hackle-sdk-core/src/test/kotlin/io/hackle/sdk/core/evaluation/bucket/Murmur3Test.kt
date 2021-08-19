package io.hackle.sdk.core.evaluation.bucket

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths

/**
 * @author Yong
 */
internal class Murmur3Test {


    @Test
    fun `murmur_all`() {
        Files.lines(Paths.get("src/test/resources/murmur_all.csv"))
            .map { Data.from(it) }
            .forEach { check(it) }
    }

    @Test
    fun `murmur_alphabetic`() {
        Files.lines(Paths.get("src/test/resources/murmur_alphabetic.csv"))
            .map { Data.from(it) }
            .forEach { check(it) }
    }

    @Test
    fun `murmur_alphanumeric`() {
        Files.lines(Paths.get("src/test/resources/murmur_alphanumeric.csv"))
            .map { Data.from(it) }
            .forEach { check(it) }
    }

    @Test
    fun `murmur_numeric`() {
        Files.lines(Paths.get("src/test/resources/murmur_numeric.csv"))
            .map { Data.from(it) }
            .forEach { check(it) }
    }

    @Test
    fun `murmur_uuid`() {
        Files.lines(Paths.get("src/test/resources/murmur_uuid.csv"))
            .map { Data.from(it) }
            .forEach { check(it) }
    }

    private fun check(data: Data) {
        val hashValue = Murmur3.murmurhash3_x86_32(data = data.input, seed = data.seed)
        assertEquals(data.hashValue, hashValue)
    }

    private class Data(
        val input: String,
        val seed: Int,
        val hashValue: Int
    ) {
        companion object {
            fun from(line: String): Data {
                return line.split(",").let {
                    Data(
                        input = it[0],
                        seed = it[1].toInt(),
                        hashValue = it[2].toInt()
                    )
                }
            }
        }
    }
}
