package io.hackle.sdk.internal.workspace

import com.fasterxml.jackson.module.kotlin.readValue
import io.hackle.sdk.core.model.*
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import io.hackle.sdk.core.model.Experiment.Type.FEATURE_FLAG
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.*
import io.hackle.sdk.core.model.Target.Key.Type.USER_PROPERTY
import io.hackle.sdk.core.model.Target.Match.Operator.*
import io.hackle.sdk.core.model.Target.Match.Type.MATCH
import io.hackle.sdk.core.model.Target.Match.Type.NOT_MATCH
import io.hackle.sdk.core.model.Target.Match.ValueType.*
import io.hackle.sdk.internal.utils.OBJECT_MAPPER
import org.junit.jupiter.api.Test
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.*
import java.nio.file.Files
import java.nio.file.Paths

internal class WorkspaceImplTest {


    @Test
    fun `workspace config test`() {
        val dto =
            OBJECT_MAPPER.readValue<WorkspaceDto>(Files.readAllBytes(Paths.get("src/test/resources/workspace_config.json")))

        val workspace = WorkspaceImpl.from(dto)


        expectThat(workspace.getExperimentOrNull(4)).isNull()

        expectThat(workspace.getExperimentOrNull(5))
            .isNotNull()
            .isA<Experiment.Draft>()
            .identifier(4318, 5, AB_TEST)
            .hasVariations(
                Variation(13378, "A", false),
                Variation(13379, "B", false),
            )
            .hasOverrides()

        expectThat(workspace.getExperimentOrNull(6))
            .isNotNull()
            .isA<Experiment.Draft>()
            .identifier(4319, 6, AB_TEST)
            .hasVariations(
                Variation(13380, "A", false),
                Variation(13381, "B", false),
            )
            .hasOverrides(
                "user_1" to 13380,
                "user_2" to 13381
            )

        expectThat(workspace.getExperimentOrNull(7))
            .isNotNull()
            .isA<Experiment.Running>()
            .identifier(4320, 7, AB_TEST)
            .hasVariations(
                Variation(13382, "A", false),
                Variation(13383, "B", false),
                Variation(13384, "C", false),
            )
            .hasOverrides()
            .and {
                get { targetAudiences }
                    .hasSize(3)
                    .and {
                        get { this[0] } isEqualTo Target(
                            listOf(
                                Condition(
                                    Key(USER_PROPERTY, "age"),
                                    Match(MATCH, GTE, NUMBER, listOf(20))
                                ),
                                Condition(
                                    Key(USER_PROPERTY, "age"),
                                    Match(MATCH, LT, NUMBER, listOf(30))
                                )
                            )
                        )

                        get { this[1] } isEqualTo Target(
                            listOf(
                                Condition(
                                    Key(USER_PROPERTY, "platform"),
                                    Match(MATCH, IN, STRING, listOf("android", "ios"))
                                )
                            )
                        )

                        get { this[2] } isEqualTo Target(
                            listOf(
                                Condition(
                                    Key(USER_PROPERTY, "membership"),
                                    Match(MATCH, IN, BOOLEAN, listOf(true))
                                )
                            )
                        )
                    }

                get { defaultRule }
                    .isA<Action.Bucket>()
                    .get { bucketId } isEqualTo 6100
            }



        expectThat(workspace.getExperimentOrNull(8))
            .isNotNull()
            .isA<Experiment.Running>()
            .identifier(4321, 8, AB_TEST)
            .hasVariations(
                Variation(13385, "A", false),
                Variation(13386, "B", false),
            )
            .hasOverrides()
            .and {
                get { targetAudiences }
                    .hasSize(4)
                    .and {
                        get { this[0] } isEqualTo Target(
                            listOf(
                                Condition(
                                    Key(USER_PROPERTY, "address"),
                                    Match(MATCH, CONTAINS, STRING, listOf("seoul"))
                                )
                            )
                        )

                        get { this[1] } isEqualTo Target(
                            listOf(
                                Condition(
                                    Key(USER_PROPERTY, "name"),
                                    Match(MATCH, STARTS_WITH, STRING, listOf("kim"))
                                )
                            )
                        )

                        get { this[2] } isEqualTo Target(
                            listOf(
                                Condition(
                                    Key(USER_PROPERTY, "message"),
                                    Match(NOT_MATCH, ENDS_WITH, STRING, listOf("!"))
                                )
                            )
                        )

                        get { this[3] } isEqualTo Target(
                            listOf(
                                Condition(
                                    Key(USER_PROPERTY, "point"),
                                    Match(MATCH, GT, NUMBER, listOf(100))
                                ),
                                Condition(
                                    Key(USER_PROPERTY, "point"),
                                    Match(MATCH, LTE, NUMBER, listOf(200))
                                )
                            )
                        )
                    }

                get { defaultRule }
                    .isA<Action.Bucket>()
                    .get { bucketId } isEqualTo 6103
            }


        expectThat(workspace.getExperimentOrNull(9))
            .isNotNull()
            .isA<Experiment.Running>()
            .identifier(4322, 9, AB_TEST)
            .hasVariations(
                Variation(13387, "A", false),
                Variation(13388, "B", false),
                Variation(13389, "C", true),
            )
            .hasOverrides()
            .get { defaultRule }
            .isA<Action.Bucket>()
            .get { bucketId } isEqualTo 6106

        expectThat(workspace.getExperimentOrNull(10))
            .isNotNull()
            .isA<Experiment.Paused>()
            .identifier(4323, 10, AB_TEST)
            .hasVariations(
                Variation(13390, "A", false),
                Variation(13391, "B", false),
            )
            .hasOverrides()

        expectThat(workspace.getExperimentOrNull(11))
            .isNotNull()
            .isA<Experiment.Completed>()
            .identifier(4324, 11, AB_TEST)
            .hasVariations(
                Variation(13392, "A", false),
                Variation(13393, "B", false),
                Variation(13394, "C", false),
                Variation(13395, "D", false),
            )
            .hasOverrides()
            .get { winnerVariation } isEqualTo Variation(13395, "D", false)


        expectThat(workspace.getFeatureFlagOrNull(1))
            .isNotNull()
            .isA<Experiment.Paused>()
            .identifier(4325, 1, FEATURE_FLAG)
            .hasVariations(
                Variation(13396, "A", false),
                Variation(13397, "B", false),
            )
            .hasOverrides()

        expectThat(workspace.getFeatureFlagOrNull(2))
            .isNotNull()
            .isA<Experiment.Running>()
            .identifier(4326, 2, FEATURE_FLAG)
            .hasVariations(
                Variation(13398, "A", false),
                Variation(13399, "B", false),
            )
            .hasOverrides()
            .and {
                get { targetAudiences }.hasSize(0)
                get { targetRules }.hasSize(0)
                get { defaultRule } isEqualTo Action.Bucket(6118)
            }

        expectThat(workspace.getFeatureFlagOrNull(3))
            .isNotNull()
            .isA<Experiment.Running>()
            .identifier(4327, 3, FEATURE_FLAG)
            .hasVariations(
                Variation(13400, "A", false),
                Variation(13401, "B", false),
            )
            .hasOverrides()
            .and {
                get { targetAudiences }.hasSize(0)
                get { targetRules }.hasSize(0)
                get { defaultRule } isEqualTo Action.Bucket(6121)
            }

        expectThat(workspace.getFeatureFlagOrNull(4))
            .isNotNull()
            .isA<Experiment.Running>()
            .identifier(4328, 4, FEATURE_FLAG)
            .hasVariations(
                Variation(13402, "A", false),
                Variation(13403, "B", false),
            )
            .hasOverrides(
                "user1" to 13402,
                "user2" to 13403,
            )
            .and {
                get { targetAudiences }.hasSize(0)
                get { targetRules }
                    .hasSize(4)
                    .and {
                        get { this[0] } isEqualTo TargetRule(
                            Target(
                                listOf(
                                    Condition(
                                        Key(USER_PROPERTY, "device"),
                                        Match(MATCH, IN, STRING, listOf("android"))
                                    ),
                                    Condition(
                                        Key(USER_PROPERTY, "version"),
                                        Match(MATCH, IN, STRING, listOf("1.0.0", "1.1.0"))
                                    )
                                )
                            ),
                            Action.Bucket(6125)
                        )

                        get { this[1] } isEqualTo TargetRule(
                            Target(
                                listOf(
                                    Condition(
                                        Key(USER_PROPERTY, "device"),
                                        Match(MATCH, IN, STRING, listOf("ios"))
                                    ),
                                    Condition(
                                        Key(USER_PROPERTY, "version"),
                                        Match(MATCH, IN, STRING, listOf("2.0.0", "2.1.0"))
                                    )
                                )
                            ),
                            Action.Bucket(6126)
                        )

                        get { this[2] } isEqualTo TargetRule(
                            Target(
                                listOf(
                                    Condition(
                                        Key(USER_PROPERTY, "grade"),
                                        Match(MATCH, IN, STRING, listOf("GOLD", "SILVER"))
                                    )
                                )
                            ),
                            Action.Variation(13403)
                        )

                        get { this[3] } isEqualTo TargetRule(
                            Target(
                                listOf(
                                    Condition(
                                        Key(USER_PROPERTY, "grade"),
                                        Match(MATCH, IN, STRING, listOf("BRONZE"))
                                    )
                                )
                            ),
                            Action.Variation(13402)
                        )
                    }
                get { defaultRule } isEqualTo Action.Bucket(6124)
            }


        expectThat(workspace.getBucketOrNull(5823))
            .isNotNull()
            .and {
                get { seed } isEqualTo 875758774
                get { slotSize } isEqualTo 10000
                (0..9999).forEach {
                    get { getSlotOrNull(it) }.isNull()
                }
            }

        expectThat(workspace.getBucketOrNull(5829))
            .isNotNull()
            .and {
                get { seed } isEqualTo 1634243589
                get { slotSize } isEqualTo 10000
                slot(0, 667, 12919)
                slot(667, 1333, 12920)
                slot(1333, 2000, 12921)
            }

        expectThat(workspace.getBucketOrNull(6106))
            .isNotNull()
            .and {
                get { seed } isEqualTo 789801074
                get { slotSize } isEqualTo 10000
                slot(0, 3333, 13387)
                slot(3333, 6667, 13388)
                slot(6667, 10000, 13389)
            }

        expectThat(workspace.getBucketOrNull(6112))
            .isNotNull()
            .and {
                get { seed } isEqualTo 2026965524
                get { slotSize } isEqualTo 10000
                slot(0, 250, 13392)
                slot(250, 500, 13393)
                slot(500, 750, 13394)
                slot(750, 1000, 13395)

                slot(1000, 2000, 13392)
                slot(2000, 3000, 13393)
                slot(3000, 4000, 13394)
                slot(4000, 5000, 13395)

                slot(5000, 6250, 13392)
                slot(6250, 7500, 13393)
                slot(7500, 8750, 13394)
                slot(8750, 10000, 13395)
            }

        expectThat(workspace.getBucketOrNull(6115))
            .isNotNull()
            .and {
                get { seed } isEqualTo 228721685
                get { slotSize } isEqualTo 10000
                slot(0, 10000, 13396)
            }

        expectThat(workspace.getEventTypeOrNull("a"))
            .isEqualTo(EventType.Custom(3072, "a"))

        expectThat(workspace.getEventTypeOrNull("b"))
            .isEqualTo(EventType.Custom(3073, "b"))

        expectThat(workspace.getEventTypeOrNull("c"))
            .isEqualTo(EventType.Custom(3074, "c"))

        expectThat(workspace.getEventTypeOrNull("d"))
            .isEqualTo(EventType.Custom(3075, "d"))
    }

    @Test
    fun `Unsupported Type Test`() {
        val dto =
            OBJECT_MAPPER.readValue<WorkspaceDto>(Files.readAllBytes(Paths.get("src/test/resources/unsupported_type_workspace_config.json")))

        val workspace = WorkspaceImpl.from(dto)

        expectThat(workspace.getExperimentOrNull(1))
            .isNotNull()
            .isA<Experiment.Running>()
            .and {
                get { targetAudiences }.hasSize(0)
                get { targetRules }.hasSize(0)
            }

        expectThat(workspace.getExperimentOrNull(22))
            .isNull()

        expectThat(workspace.getExperimentOrNull(23))
            .isNull()

        expectThat(workspace.getFeatureFlagOrNull(1))
            .isNotNull()
            .isA<Experiment.Running>()
            .and {
                get { targetAudiences }.hasSize(0)
                get { targetRules }.hasSize(0)
            }
    }

    private fun <T : Experiment> Assertion.Builder<T>.identifier(id: Long, key: Long, type: Experiment.Type) =
        compose("Experiment") {
            get("Experiment.id") { this.id } isEqualTo id
            get("Experiment.key") { this.key } isEqualTo key
            get("Experiment.type") { this.type } isEqualTo type
        } then {
            if (allPassed) pass() else fail()
        }

    private fun <T : Experiment> Assertion.Builder<T>.hasVariations(vararg variations: Variation) =
        assert("Experiment.variations") {
            val actual = variations.associateBy(Variation::id)
            if (it.variations == actual) {
                pass()
            } else {
                fail(actual)
            }
        }

    private fun <T : Experiment> Assertion.Builder<T>.hasOverrides(vararg overrides: Pair<String, Long>) =
        assert("Experiment.overrides") {
            val actual = overrides.toMap()
            if (it.overrides == actual) {
                pass()
            } else {
                fail(actual)
            }
        }

    private fun Assertion.Builder<Bucket>.slot(startInclusive: Int, endExclusive: Int, variationId: Long) =
        compose("Bucket.getSlotOrNull()") {
            for (slotNumber in (startInclusive until endExclusive)) {
                get { getSlotOrNull(slotNumber) }
                    .isNotNull()
                    .get { this.variationId } isEqualTo variationId
            }
        } then {
            if (allPassed) pass() else fail()
        }
}
