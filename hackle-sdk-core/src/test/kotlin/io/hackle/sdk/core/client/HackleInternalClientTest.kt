package io.hackle.sdk.core.client

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.User
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.core.allocation.Allocation
import io.hackle.sdk.core.allocation.Allocation.ForcedAllocated
import io.hackle.sdk.core.allocation.Allocator
import io.hackle.sdk.core.event.EventProcessor
import io.hackle.sdk.core.event.UserEvent
import io.hackle.sdk.core.model.EventType
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Variation
import io.hackle.sdk.core.workspace.Workspace
import io.hackle.sdk.core.workspace.WorkspaceFetcher
import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

/**
 * @author Yong
 */
@ExtendWith(MockKExtension::class)
internal class HackleInternalClientTest {

    @MockK
    private lateinit var allocator: Allocator

    @MockK
    private lateinit var workspaceFetcher: WorkspaceFetcher

    @RelaxedMockK
    private lateinit var eventProcessor: EventProcessor

    @InjectMockKs
    private lateinit var sut: HackleInternalClient

    @Nested
    inner class Variations {

        @Test
        fun `Workspace를 가져오지 못하면 defaultVariation으로 결정한다`() {
            // given
            every { workspaceFetcher.fetch() } returns null

            val defaultVariation = io.hackle.sdk.common.Variation.J

            // when
            val actual = sut.variation(42, User.of("TEST_USER_ID"), defaultVariation)

            //then
            expectThat(actual) {
                get { reason } isEqualTo DecisionReason.SDK_NOT_READY
                get { variation } isEqualTo defaultVariation
            }
        }

        @Test
        fun `experimentKey에 해당하는 experiment가 없으면 defaultVariation으로 결정한다`() {
            // given
            val workspace = mockk<Workspace> {
                every { getExperimentOrNull(any()) } returns null
            }

            every { workspaceFetcher.fetch() } returns workspace

            val defaultVariation = io.hackle.sdk.common.Variation.J

            // when
            val actual = sut.variation(42, User.of("TEST_USER_ID"), defaultVariation)

            //then
            expectThat(actual) {
                get { reason } isEqualTo DecisionReason.EXPERIMENT_NOT_FOUND
                get { variation } isEqualTo defaultVariation
            }
        }

        @Test
        fun `실험에 할당 되지 않으면 defaultVariation으로 결정한다`() {
            // given
            val user = User.of("TEST_USER_ID")
            val experiment = mockk<Experiment>()
            val workspace = mockk<Workspace> {
                every { getExperimentOrNull(any()) } returns experiment
            }
            every { workspaceFetcher.fetch() } returns workspace
            every { allocator.allocate(experiment, user) } returns Allocation.NotAllocated(TRAFFIC_NOT_ALLOCATED)

            val defaultVariation = io.hackle.sdk.common.Variation.J

            // when
            val actual = sut.variation(42, user, defaultVariation)

            //then
            expectThat(actual) {
                get { reason } isEqualTo TRAFFIC_NOT_ALLOCATED
                get { variation } isEqualTo defaultVariation
            }
        }

        @Test
        fun `강제할당된 경우 강제할당된 Variaton으로 결정하고 노출이벤트는 전송하지 않는다`() {
            // given
            val user = User.of("TEST_USER_ID")
            val experiment = mockk<Experiment>()
            val workspace = mockk<Workspace> {
                every { getExperimentOrNull(any()) } returns experiment
            }
            every { workspaceFetcher.fetch() } returns workspace

            val forcedAllocatedVariation = io.hackle.sdk.common.Variation.E
            every { allocator.allocate(experiment, user) } returns ForcedAllocated(
                OVERRIDDEN,
                forcedAllocatedVariation.name
            )

            val defaultVariation = io.hackle.sdk.common.Variation.J

            // when
            val actual = sut.variation(42, user, defaultVariation)

            //then
            expectThat(actual) {
                get { reason } isEqualTo OVERRIDDEN
                get { variation } isEqualTo forcedAllocatedVariation
            }
            verify { eventProcessor wasNot Called }
        }

        @Test
        fun `할당 된 경우 노출 이벤트를 전송하고 할당된 Variation으로 결정한다`() {
            // given
            val user = User.of("TEST_USER_ID")
            val experiment = mockk<Experiment>()
            val workspace = mockk<Workspace> {
                every { getExperimentOrNull(any()) } returns experiment
            }
            every { workspaceFetcher.fetch() } returns workspace

            val variation = mockk<Variation> {
                every { key } returns io.hackle.sdk.common.Variation.E.name
            }

            every { allocator.allocate(experiment, user) } returns Allocation.Allocated(TRAFFIC_ALLOCATED, variation)

            val defaultVariation = io.hackle.sdk.common.Variation.J

            // when
            val actual = sut.variation(42, user, defaultVariation)

            //then
            expectThat(actual) {
                get { reason } isEqualTo TRAFFIC_ALLOCATED
                get { this.variation } isEqualTo io.hackle.sdk.common.Variation.E
            }
            verify(exactly = 1) {
                eventProcessor.process(withArg {
                    expectThat(it)
                        .isA<UserEvent.Exposure>()
                        .and {
                            get { variation } isSameInstanceAs variation
                        }
                })
            }
        }
    }

    @Nested
    inner class Track {

        @Test
        fun `Workspace를 가져오지 못하면 이벤트를 전송하지 않는다`() {
            // given
            every { workspaceFetcher.fetch() } returns null

            // when
            sut.track(Event.of("test_event_key"), User.of("TEST_USER_ID"))

            //then
            verify { eventProcessor wasNot Called }
        }

        @Test
        fun `eventKey에 대한 eventType을 찾지 못하면 Undefined 이벤트를 전송한다`() {
            // given
            val workspace = mockk<Workspace> {
                every { getEventTypeOrNull(any()) } returns null
            }
            every { workspaceFetcher.fetch() } returns workspace

            // when
            sut.track(Event.of("undefined_event_key"), User.of("TEST_USER_ID"))

            //then
            verify(exactly = 1) {
                eventProcessor.process(withArg {
                    expectThat(it)
                        .isA<UserEvent.Track>()
                        .and {
                            get { eventType }
                                .isA<EventType.Undefined>()
                                .get { key }
                                .isEqualTo("undefined_event_key")
                        }
                })
            }
        }

        @Test
        fun `eventKey에 대한 evenType이 정의 되어 있으면 해당 이벤트를 전송한다`() {
            // given
            val eventType = mockk<EventType>()
            val workspace = mockk<Workspace> {
                every { getEventTypeOrNull(any()) } returns eventType
            }
            every { workspaceFetcher.fetch() } returns workspace

            // when
            sut.track(Event.of("custom_event_key"), User.of("TEST_USER_ID"))

            //then
            verify(exactly = 1) {
                eventProcessor.process(withArg {
                    expectThat(it)
                        .get { eventType }
                        .isSameInstanceAs(eventType)
                })
            }
        }
    }
}
