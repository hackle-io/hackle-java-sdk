package io.hackle.sdk.core.evaluation.service.experiment.mode.remote

import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.Workspaces
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.user.IdentifierType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isSameInstanceAs
import strikt.assertions.isTrue

internal class ExperimentRemoteEvaluateRequestTest {

    @Nested
    inner class OfTest {

        @Test
        fun `workspace, entity, user, record 로 생성한다`() {
            val workspace = Workspaces.evaluation()
            val experiment = Experiments.remoteResult()
            val user = HackleUser.builder().identifier(IdentifierType.ID, "user").build()

            val request = ExperimentRemoteEvaluateRequest.of(
                workspace = workspace,
                entity = experiment,
                user = user,
                record = false
            )

            expectThat(request) {
                get { this.workspace } isSameInstanceAs workspace
                get { entity } isSameInstanceAs experiment
                get { this.user } isSameInstanceAs user
                get { record }.isFalse()
            }
        }

        @Test
        fun `record 기본값은 true`() {
            val request = ExperimentRemoteEvaluateRequest.of(
                workspace = Workspaces.evaluation(),
                entity = Experiments.remoteResult(),
                user = HackleUser.builder().identifier(IdentifierType.ID, "user").build()
            )

            expectThat(request.record).isTrue()
        }

        @Test
        fun `원본 request 의 workspace, user, record 를 유지하고 entity 만 교체한다`() {
            val origin = Experiments.remoteRequest(record = false)
            val experiment = Experiments.remoteResult(id = 320)

            val request = ExperimentRemoteEvaluateRequest.of(origin, experiment)

            expectThat(request) {
                get { workspace } isSameInstanceAs origin.workspace
                get { entity } isSameInstanceAs experiment
                get { user } isSameInstanceAs origin.user
                get { record }.isFalse()
            }
        }
    }
}
