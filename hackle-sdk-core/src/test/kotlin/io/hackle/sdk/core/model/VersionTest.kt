package io.hackle.sdk.core.model

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs

internal class VersionTest {

    @Nested
    inner class ParseTest {

        @Test
        fun `이미 Version 타입이면 그대로 리턴한다`() {
            // given
            val version = mockk<Version>()

            // when
            val actual = Version.parseOrNull(version)

            // then
            expectThat(actual) isSameInstanceAs version
        }

        @Test
        fun `Version, String 타입이 아니면 null 리턴`() {
            val actual = Version.parseOrNull(1.2)
            expectThat(actual).isNull()
        }

        @Test
        fun `invalid format`() {
            verifyNull("01.0.0")
            verifyNull("1.01.0")
            verifyNull("1.1.01")
            verifyNull("2.x")
            verifyNull("2.3.x")
            verifyNull("2.3.1.4")
            verifyNull("2.3.1*beta")
            verifyNull("2.3.1-beta*")
            verifyNull("2.3.1-beta_4")
        }

        @Test
        fun `semantic core version parse`() {
            verify("1.0.0", 1, 0, 0)
            verify("14.165.14", 14, 165, 14)
        }

        @Test
        fun `semantic version with pre release`() {
            verify("1.0.0-beta1", 1, 0, 0, listOf("beta1"))
            verify("1.0.0-beta.1", 1, 0, 0, listOf("beta", "1"))
            verify("1.0.0-x.y.z", 1, 0, 0, listOf("x", "y", "z"))
        }

        @Test
        fun `semantic version with build`() {
            verify("1.0.0+build1", 1, 0, 0, emptyList(), listOf("build1"))
            verify("1.0.0+build.1", 1, 0, 0, emptyList(), listOf("build", "1"))
            verify("1.0.0+1.2.3", 1, 0, 0, emptyList(), listOf("1", "2", "3"))
        }

        @Test
        fun `semantic version with prerelease and build`() {
            verify("1.0.0-alpha.3.rc.5+build.53", 1, 0, 0, listOf("alpha", "3", "rc", "5"), listOf("build", "53"))
        }

        @Test
        fun `minor, patch 가 없는 경우 0으로 채워준다`() {
            verify("15", 15, 0, 0)
            verify("15.143", 15, 143, 0)
            verify("15-x.y.z", 15, 0, 0, listOf("x", "y", "z"))
            verify("15-x.y.z+a.b.c", 15, 0, 0, listOf("x", "y", "z"), listOf("a", "b", "c"))
        }

        @Test
        fun `core 버전만 있는 경우 core 버전이 같으면 같은 버전이다`() {
            val version = v("2.3.4")
            assertTrue(version == version)
            assertTrue(v("2.3.4") == v("2.3.4"))
        }

        @Test
        fun `core + prerelease 버전이 모두 같아야 같은 버전이다`() {
            assertTrue(v("2.3.4-beta.1") == v("2.3.4-beta.1"))
        }

        @Test
        fun `prerelease 버전이 다르면 다른버전이다`() {
            assertTrue(v("2.3.4-beta.1") != v("2.3.4-beta.2"))
        }

        @Test
        fun `build 가 달라도 나머지가 같으면 같은 버전이다`() {
            assertTrue(v("2.3.4+build.111") == v("2.3.4+build.222"))
            assertTrue(v("2.3.4-beta.1+build.111") == v("2.3.4-beta.1+build.222"))
        }

        @Test
        fun `major를 제일 먼저 비교한다`() {
            assertTrue(v("4.5.7") > v("3.5.7"))
            assertTrue(v("2.5.7") < v("3.5.7"))
        }

        @Test
        fun `major 가 같으면 minor 를 다음으로 비교한다`() {
            assertTrue(v("3.6.7") > v("3.5.7"))
            assertTrue(v("3.4.7") < v("3.5.7"))
        }

        @Test
        fun `minor 까지 같으면 patch를 비교한다`() {
            assertTrue(v("3.5.8") > v("3.5.7"))
            assertTrue(v("3.5.6") < v("3.5.7"))
        }

        @Test
        fun `정식 배포 버전이 더 높은 버전이다`() {
            assertTrue(v("3.5.7") > v("3.5.7-beta"))
            assertTrue(v("3.5.7-alpha") < v("3.5.7"))
        }

        @Test
        fun `prerelease 숫자로만 구성된 식별자는 수의 크기로 비교한다`() {
            assertTrue(v("3.5.7-1") < v("3.5.7-2"))
            assertTrue(v("3.5.7-1.1") < v("3.5.7-1.2"))
            assertTrue(v("3.5.7-11") > v("3.5.7-1"))
        }

        @Test
        fun `prerelease 알파벳이 포함된 경우에는 아스키 문자열 정렬을 한다`() {
            assertTrue(v("3.5.7-a") == v("3.5.7-a"))
            assertTrue(v("3.5.7-a") < v("3.5.7-b"))
            assertTrue(v("3.5.7-az") > v("3.5.7-ab"))
        }

        @Test
        fun `prerelease 숫자로만 구성된 식별자는 어떤 경우에도 문자와 붙임표가 있는 식별자보다 낮은 우선순위로 여긴다`() {
            assertTrue(v("3.5.7-9") < v("3.5.7-a"))
            assertTrue(v("3.5.7-9") < v("3.5.7-a-9"))
            assertTrue(v("3.5.7-beta") > v("3.5.7-1"))
        }

        @Test
        fun `prerelease 앞선 식별자가 모두 같은 배포 전 버전의 경우에는 필드 수가 많은 쪽이 더 높은 우선순위를 가진다`() {
            assertTrue(v("1.0.0-alpha") < v("1.0.0-alpha.1"))
            assertTrue(v("1.0.0-1.2.3") < v("1.0.0-1.2.3.4"))
        }

        @Test
        fun `toStringTest`() {
            expectThat(v("1.0.0").toString()) isEqualTo "Version(1.0.0)"
            expectThat(v("1.0.0-beta").toString()) isEqualTo "Version(1.0.0-beta)"
            expectThat(v("1.0.0-beta+build").toString()) isEqualTo "Version(1.0.0-beta+build)"
            expectThat(v("1.0.0+build").toString()) isEqualTo "Version(1.0.0+build)"
        }

        @Test
        fun `hashTest`() {
            expectThat(v("1.0.0").hashCode()) isEqualTo v("1.0.0").hashCode()
            expectThat(v("1.0.0-beta").hashCode()) isEqualTo v("1.0.0-beta").hashCode()
            expectThat(v("1.0.0-beta+build").hashCode()) isEqualTo v("1.0.0-beta+build").hashCode()
            expectThat(v("1.0.0+build").hashCode()) isEqualTo v("1.0.0+build").hashCode()
        }

        @Test
        fun `equalTest`() {
            expectThat(v("1.0.0") == v("1.0.0"))
            expectThat(v("1.0.0") != v("1.0.0-beta"))
            assertFalse(v("1.0.0").equals(1))
        }

        private fun verifyNull(version: String) {
            expectThat(Version.parseOrNull(version)).isNull()
        }

        private fun verify(
            version: String,
            major: Int,
            minor: Int,
            patch: Int,
            prerelease: List<String> = emptyList(),
            build: List<String> = emptyList()
        ) {
            expectThat(Version.parseOrNull(version))
                .isNotNull()
                .and {
                    get { this.coreVersion } isEqualTo CoreVersion(major, minor, patch)
                    get { this.prerelease } isEqualTo MetadataVersion(prerelease)
                    get { this.build } isEqualTo MetadataVersion(build)
                }
        }
    }

    private fun v(value: String): Version {
        return Version.parseOrNull(value)!!
    }
}