package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.common.Variation.A
import io.hackle.sdk.common.Variation.B
import io.hackle.sdk.core.evaluation.action.ActionResolver
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.model.*
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.workspace.workspace
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs

@ExtendWith(MockKExtension::class)
internal class OverrideResolverTest {

    @MockK
    private lateinit var targetMatcher: TargetMatcher

    @MockK
    private lateinit var actionResolver: ActionResolver

    @InjectMockKs
    private lateinit var sut: OverrideResolver

    @Test
    fun `직접 입력한 override 를 먼저 평가한다`() {
        // given
        var experiment: Experiment? = null
        workspace {
            experiment = experiment(status = Experiment.Status.DRAFT) {
                variations(A, B)
                overrides {
                    A {
                        segment("seg_01")
                    }
                    B {
                        user("user_01")
                    }
                }
            }
            segment(key = "seg_01", type = Segment.Type.USER_ID) {
                target {
                    condition {
                        Target.Key.Type.USER_ID("USER_ID")
                        Target.Match.Operator.IN("user_01")
                    }
                }
            }
        }

        val user = HackleUser.of("user_01")

        // when
        val actual = sut.resolveOrNull(mockk(), experiment!!, user)

        // then
        expectThat(actual)
            .isNotNull()
            .get { key } isEqualTo "B"
    }

    @Test
    fun `직접 입력된 override 가 없으면 SegmentOverride 를 확인한다`() {
        var experiment: Experiment? = null
        val workspace = workspace {
            experiment = experiment(status = Experiment.Status.DRAFT) {
                variations(A, B)
                overrides {
                    A {
                        segment("seg_01")
                    }
                    B {
                        user("user_02")
                    }
                }
            }
            segment(key = "seg_01", type = Segment.Type.USER_ID) {
                target {
                    condition {
                        Target.Key.Type.USER_ID("USER_ID")
                        Target.Match.Operator.IN("user_01")
                    }
                }
            }
        }

        every { targetMatcher.matches(any(), any(), any()) } returns true

        val variation = mockk<Variation>()
        every { actionResolver.resolveOrNull(any(), workspace, experiment!!, any()) } returns variation

        val user = HackleUser.of("user_01")

        // when
        val actual = sut.resolveOrNull(workspace, experiment!!, user)

        // then
        expectThat(actual) isSameInstanceAs variation
    }

    @Test
    fun `직접입력, Segment 둘다 override 되어 있지않으면 null 리턴`() {
        // given
        var experiment: Experiment? = null
        val workspace = workspace {
            experiment = experiment(status = Experiment.Status.DRAFT) {
                variations(A, B)
                overrides {
                    A {
                        segment("seg_01")
                    }
                    B {
                        user("user_02")
                        segment("seg_03")
                    }
                }
            }
            segment(key = "seg_01", type = Segment.Type.USER_ID) {
                target {
                    condition {
                        Target.Key.Type.USER_ID("USER_ID")
                        Target.Match.Operator.IN("user_01")
                    }
                }
            }
        }

        every { targetMatcher.matches(any(), any(), any()) } returns false

        val user = HackleUser.of("user_01")

        // when
        val actual = sut.resolveOrNull(mockk(), experiment!!, user)

        // then
        expectThat(actual).isNull()
    }
}