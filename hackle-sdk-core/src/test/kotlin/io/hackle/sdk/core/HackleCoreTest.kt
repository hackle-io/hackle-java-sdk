package io.hackle.sdk.core

import io.hackle.sdk.common.User
import io.hackle.sdk.common.Variation
import io.hackle.sdk.common.Variation.*
import io.hackle.sdk.common.decision.Decision
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.core.event.EventProcessor
import io.hackle.sdk.core.event.UserEvent
import io.hackle.sdk.core.model.Experiment.Status.*
import io.hackle.sdk.core.model.HackleUser
import io.hackle.sdk.core.model.Target.Key.Type.USER_PROPERTY
import io.hackle.sdk.core.model.Target.Match.Operator.IN
import io.hackle.sdk.core.workspace.Workspace
import io.hackle.sdk.core.workspace.WorkspaceDsl
import io.hackle.sdk.core.workspace.WorkspaceFetcher
import io.hackle.sdk.core.workspace.workspace
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class HackleCoreTest {


    @Test
    fun `not found experiment`() {
        val workspaceFetcher = workspaceFetcher {}

        val client = HackleCore.client(workspaceFetcher, EventProcessorStub)

        client.experiment(1, HackleUser.of(User.builder("a").property("grade", "SILVER").build()), A)
            .expect(A, EXPERIMENT_NOT_FOUND)

    }

    @Test
    fun `draft`() {
        val workspaceFetcher = workspaceFetcher {
            experiment(key = 1, status = DRAFT) {
                variations(A, B, C)
                overrides {
                    A("a")
                    B("b")
                }
                audiences {
                    target {
                        condition {
                            USER_PROPERTY("grade")
                            IN("GOLD")
                        }
                    }
                }
            }

            experiment(key = 2, status = DRAFT) {
                variations(A, B, C)
            }
        }

        val client = HackleCore.client(workspaceFetcher, EventProcessorStub)


        client.experiment(1, HackleUser.of(User.builder("a").property("grade", "SILVER").build()), A)
            .expect(A, OVERRIDDEN)

        client.experiment(1, HackleUser.of(User.builder("b").property("grade", "SILVER").build()), A)
            .expect(B, OVERRIDDEN)

        client.experiment(1, HackleUser.of(User.builder("c").property("grade", "SILVER").build()), A)
            .expect(A, NOT_IN_EXPERIMENT_TARGET)

        client.experiment(1, HackleUser.of(User.builder("c").property("grade", "GOLD").build()), A)
            .expect(A, EXPERIMENT_DRAFT)


        client.experiment(2, HackleUser.of(User.builder("a").property("grade", "SILVER").build()), A)
            .expect(A, EXPERIMENT_DRAFT)

        client.experiment(2, HackleUser.of(User.builder("b").property("grade", "SILVER").build()), A)
            .expect(A, EXPERIMENT_DRAFT)

        client.experiment(2, HackleUser.of(User.builder("c").property("grade", "SILVER").build()), A)
            .expect(A, EXPERIMENT_DRAFT)

        client.experiment(2, HackleUser.of(User.builder("c").property("grade", "GOLD").build()), A)
            .expect(A, EXPERIMENT_DRAFT)

    }

    @Test
    fun `paused`() {
        val workspaceFetcher = workspaceFetcher {
            experiment(key = 1, status = PAUSED) {
                variations(A, B, C)
                overrides {
                    A("a")
                    B("b")
                }
                audiences {
                    target {
                        condition {
                            USER_PROPERTY("grade")
                            IN("GOLD")
                        }
                    }
                }
                defaultRule {
                    bucket {
                        A(0..5000)
                        B(5000..10000)
                    }
                }
            }

            experiment(key = 2, status = PAUSED) {
                variations(A, B, C)
                defaultRule {
                    bucket {
                        A(0..5000)
                        B(5000..10000)
                    }
                }
            }
        }

        val client = HackleCore.client(workspaceFetcher, EventProcessorStub)


        client.experiment(1, HackleUser.of(User.builder("a").property("grade", "SILVER").build()), A)
            .expect(A, OVERRIDDEN)

        client.experiment(1, HackleUser.of(User.builder("b").property("grade", "SILVER").build()), A)
            .expect(B, OVERRIDDEN)

        client.experiment(1, HackleUser.of(User.builder("c").property("grade", "SILVER").build()), A)
            .expect(A, NOT_IN_EXPERIMENT_TARGET)

        client.experiment(1, HackleUser.of(User.builder("c").property("grade", "GOLD").build()), A)
            .expect(A, EXPERIMENT_PAUSED)

        client.experiment(2, HackleUser.of(User.builder("a").property("grade", "SILVER").build()), A)
            .expect(A, EXPERIMENT_PAUSED)

        client.experiment(2, HackleUser.of(User.builder("b").property("grade", "SILVER").build()), A)
            .expect(A, EXPERIMENT_PAUSED)

        client.experiment(2, HackleUser.of(User.builder("c").property("grade", "SILVER").build()), A)
            .expect(A, EXPERIMENT_PAUSED)

        client.experiment(2, HackleUser.of(User.builder("c").property("grade", "GOLD").build()), A)
            .expect(A, EXPERIMENT_PAUSED)

    }

    @Test
    fun `running`() {
        val workspaceFetcher = workspaceFetcher {
            experiment(key = 1, status = RUNNING) {
                variations(A, B, C)
                overrides {
                    A("a")
                    B("b")
                }
                audiences {
                    target {
                        condition {
                            USER_PROPERTY("grade")
                            IN("GOLD")
                        }
                    }
                }
                defaultRule {
                    bucket {
                        B(0..10000)
                    }
                }
            }

            experiment(key = 2, status = RUNNING) {
                variations(A, B)
                defaultRule {
                    bucket {
                        A(0..10000)
                    }
                }
            }

            experiment(key = 3, status = RUNNING) {
                variations(A, B)
            }

            experiment(key = 4, status = RUNNING) {
                variations {
                    A(10001, false)
                    B(10002, false)
                    C(10003, true)
                }
                defaultRule {
                    bucket {
                        C(0..10000)
                    }
                }
            }
        }

        val client = HackleCore.client(workspaceFetcher, EventProcessorStub)


        client.experiment(1, HackleUser.of(User.builder("a").property("grade", "SILVER").build()), A)
            .expect(A, OVERRIDDEN)

        client.experiment(1, HackleUser.of(User.builder("b").property("grade", "SILVER").build()), A)
            .expect(B, OVERRIDDEN)

        client.experiment(1, HackleUser.of(User.builder("c").property("grade", "SILVER").build()), A)
            .expect(A, NOT_IN_EXPERIMENT_TARGET)

        client.experiment(1, HackleUser.of("c"), A)
            .expect(A, NOT_IN_EXPERIMENT_TARGET)

        client.experiment(1, HackleUser.of(User.builder("c").property("grade", "GOLD").build()), A)
            .expect(B, TRAFFIC_ALLOCATED)

        client.experiment(2, HackleUser.of(User.builder("a").property("grade", "SILVER").build()), A)
            .expect(A, TRAFFIC_ALLOCATED)

        client.experiment(2, HackleUser.of(User.builder("b").property("grade", "SILVER").build()), A)
            .expect(A, TRAFFIC_ALLOCATED)

        client.experiment(2, HackleUser.of(User.builder("c").property("grade", "SILVER").build()), A)
            .expect(A, TRAFFIC_ALLOCATED)

        client.experiment(2, HackleUser.of(User.builder("c").property("grade", "GOLD").build()), A)
            .expect(A, TRAFFIC_ALLOCATED)

        client.experiment(3, HackleUser.of(User.builder("a").property("grade", "GOLD").build()), A)
            .expect(A, TRAFFIC_NOT_ALLOCATED)

        client.experiment(4, HackleUser.of(User.builder("a").property("grade", "GOLD").build()), A)
            .expect(A, VARIATION_DROPPED)
    }

    @Test
    fun `complete`() {

        val workspaceFetcher = workspaceFetcher {
            experiment(key = 1, status = COMPLETED) {
                variations(A, B, C, D)
                winner(D)
                overrides {
                    A("a")
                    B("b")
                }
                audiences {
                    target {
                        condition {
                            USER_PROPERTY("grade")
                            IN("GOLD")
                        }
                    }
                }
                defaultRule {
                    bucket {
                        A(0..5000)
                        B(5000..10000)
                    }
                }
            }

            experiment(key = 2, status = COMPLETED) {
                variations(A, B, C, D)
                winner(D)
                overrides {
                    A("a")
                    B("b")
                }
                defaultRule {
                    bucket {
                        A(0..5000)
                        B(5000..10000)
                    }
                }
            }
        }

        val client = HackleCore.client(workspaceFetcher, EventProcessorStub)


        client.experiment(1, HackleUser.of(User.builder("a").property("grade", "SILVER").build()), A)
            .expect(A, OVERRIDDEN)

        client.experiment(1, HackleUser.of(User.builder("b").property("grade", "SILVER").build()), A)
            .expect(B, OVERRIDDEN)

        client.experiment(1, HackleUser.of(User.builder("abc").property("grade", "SILVER").build()), A)
            .expect(A, NOT_IN_EXPERIMENT_TARGET)

        client.experiment(1, HackleUser.of(User.builder("abc").property("grade", "GOLD").build()), A)
            .expect(D, EXPERIMENT_COMPLETED)

        client.experiment(2, HackleUser.of(User.builder("a").property("grade", "SILVER").build()), A)
            .expect(A, OVERRIDDEN)

        client.experiment(2, HackleUser.of(User.builder("b").property("grade", "SILVER").build()), A)
            .expect(B, OVERRIDDEN)

        client.experiment(2, HackleUser.of(User.builder("abc").property("grade", "SILVER").build()), A)
            .expect(D, EXPERIMENT_COMPLETED)

        client.experiment(2, HackleUser.of(User.builder("abc").property("grade", "GOLD").build()), A)
            .expect(D, EXPERIMENT_COMPLETED)
    }


    private fun Decision.expect(variation: Variation, reason: DecisionReason) {
        expectThat(this) {
            get { this.variation } isEqualTo variation
            get { this.reason } isEqualTo reason
        }
    }

    private fun workspaceFetcher(init: WorkspaceDsl.() -> Unit): WorkspaceFetcher {
        return WorkspaceFetcherStub(workspace(init))
    }

    private class WorkspaceFetcherStub(private val workspace: Workspace) : WorkspaceFetcher {
        override fun fetch(): Workspace {
            return workspace
        }
    }

    private object EventProcessorStub : EventProcessor {
        override fun process(event: UserEvent) {
        }
    }
}
