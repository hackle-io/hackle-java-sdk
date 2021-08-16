//package io.hackle.sdk.internal.workspace
//
//import com.fasterxml.jackson.module.kotlin.readValue
//import io.hackle.sdk.core.model.*
//import io.hackle.sdk.internal.utils.OBJECT_MAPPER
//import org.junit.jupiter.api.Test
//import strikt.api.expectThat
//import strikt.assertions.isEqualTo
//import strikt.assertions.isNull
//import java.nio.file.Files
//import java.nio.file.Paths
//
//internal class WorkspaceImplTest {
//
//
//    @Test
//    fun `worksapce config test`() {
//        val dto =
//            OBJECT_MAPPER.readValue<WorkspaceDto>(Files.readAllBytes(Paths.get("src/test/resources/workspace_config.json")))
//
//        val workspace = WorkspaceImpl.from(dto)
//
//        expectThat(workspace) {
//
//            get { getExperimentOrNull(4) }.isNull()
//
//            get { getExperimentOrNull(5) } isEqualTo Experiment.Draft(
//                id = 1478,
//                key = 5,
//                type = Experiment.Type.AB_TEST,
//                variations = mapOf(
//                    4050L to Variation(4050, "A", false),
//                    4051L to Variation(4051, "B", false)
//                ),
//                overrides = mapOf(
//                    "51" to 4050L,
//                    "52" to 4051L
//                )
//            )
//
//            get { getExperimentOrNull(6) } isEqualTo Experiment.Running(
//                id = 1479,
//                key = 6,
//                type = Experiment.Type.AB_TEST,
//                variations = mapOf(
//                    4052L to Variation(4052L, "A", false),
//                    4053L to Variation(4053L, "B", false)
//                ),
//                overrides = mapOf(
//                    "61" to 4052L,
//                    "62" to 4053L
//                ),
//                bucket = Bucket(
//                    seed = 916529528,
//                    slotSize = 10000,
//                    slots = emptyList()
//                )
//            )
//
//            get { getExperimentOrNull(7) } isEqualTo Experiment.Running(
//                id = 1480,
//                key = 7,
//                type = Experiment.Type.AB_TEST,
//                variations = mapOf(
//                    4054L to Variation(4054L, "A", false),
//                    4055L to Variation(4055L, "B", false),
//                    4056L to Variation(4056L, "C", true),
//                ),
//                overrides = emptyMap(),
//                bucket = Bucket(
//                    seed = 1728289230,
//                    slotSize = 10000,
//                    slots = listOf(
//                        Slot(0, 1667, 4054),
//                        Slot(1667, 3333, 4055),
//                        Slot(3333, 5000, 4056),
//                    )
//                )
//            )
//
//            get { getExperimentOrNull(8) } isEqualTo Experiment.Paused(
//                id = 1481,
//                key = 8,
//                type = Experiment.Type.AB_TEST,
//                variations = mapOf(
//                    4057L to Variation(4057L, "A", false),
//                    4058L to Variation(4058L, "B", false),
//                    4059L to Variation(4059L, "C", false),
//                    4060L to Variation(4060L, "D", false),
//                ),
//                overrides = emptyMap()
//            )
//
//            get { getExperimentOrNull(9) } isEqualTo Experiment.Completed(
//                id = 1482,
//                key = 9,
//                type = Experiment.Type.AB_TEST,
//                variations = mapOf(
//                    4061L to Variation(4061L, "A", false),
//                    4062L to Variation(4062L, "B", false),
//                ),
//                overrides = emptyMap(),
//                winnerVariationId = 4062L
//            )
//
//            get { getFeatureFlagOrNull(1) } isEqualTo Experiment.Running(
//                id = 1483,
//                key = 1,
//                type = Experiment.Type.FEATURE_FLAG,
//                variations = mapOf(
//                    4063L to Variation(4063L, "A", false),
//                    4064L to Variation(4064L, "B", false),
//                ),
//                overrides = mapOf(
//                    "11" to 4063L,
//                    "12" to 4064L,
//                ),
//                bucket = Bucket(
//                    seed = 1653168523,
//                    slotSize = 10000,
//                    slots = listOf(
//                        Slot(0, 10000, 4063),
//                    )
//                )
//            )
//
//            get { getFeatureFlagOrNull(2) } isEqualTo Experiment.Running(
//                id = 1484,
//                key = 2,
//                type = Experiment.Type.FEATURE_FLAG,
//                variations = mapOf(
//                    4065L to Variation(4065L, "A", false),
//                    4066L to Variation(4066L, "B", false),
//                ),
//                overrides = emptyMap(),
//                bucket = Bucket(
//                    seed = 1791240461,
//                    slotSize = 10000,
//                    slots = listOf(
//                        Slot(0, 6500, 4065),
//                        Slot(6500, 10000, 4066),
//                    )
//                )
//            )
//
//            get { getFeatureFlagOrNull(3) } isEqualTo Experiment.Running(
//                id = 1485,
//                key = 3,
//                type = Experiment.Type.FEATURE_FLAG,
//                variations = mapOf(
//                    4067L to Variation(4067L, "A", false),
//                    4068L to Variation(4068L, "B", false),
//                ),
//                overrides = mapOf(
//                    "31" to 4067L,
//                    "32" to 4068L,
//                ),
//                bucket = Bucket(
//                    seed = 809049883,
//                    slotSize = 10000,
//                    slots = listOf(
//                        Slot(0, 10000, 4068),
//                    )
//                )
//            )
//
//            get { getFeatureFlagOrNull(4) }.isNull()
//
//            get { getEventTypeOrNull("event_type_1") } isEqualTo EventType.Custom(1, "event_type_1")
//            get { getEventTypeOrNull("event_type_2") } isEqualTo EventType.Custom(2, "event_type_2")
//            get { getEventTypeOrNull("event_type_3") } isEqualTo EventType.Custom(3, "event_type_3")
//            get { getEventTypeOrNull("event_type_4") }.isNull()
//        }
//    }
//}