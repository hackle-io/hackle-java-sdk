package io.hackle.sdk.internal.workspace

internal data class WorkspaceDto(
    val experiments: List<ExperimentDto>,
    val featureFlags: List<ExperimentDto>,
    val buckets: List<BucketDto>,
    val events: List<EventTypeDto>,
    val segments: List<SegmentDto>,
    val containers: List<ContainerDto>
)

internal data class ExperimentDto(
    val id: Long,
    val key: Long,
    val status: String,
    val version: Int,
    val variations: List<VariationDto>,
    val execution: ExecutionDto,
    val winnerVariationId: Long?,
    val identifierType: String,
    val containerId: Long?
)

internal data class VariationDto(
    val id: Long,
    val key: String,
    val status: String,
)

internal data class ExecutionDto(
    val status: String,
    val userOverrides: List<UserOverrideDto>,
    val segmentOverrides: List<TargetRuleDto>,
    val targetAudiences: List<TargetDto>,
    val targetRules: List<TargetRuleDto>,
    val defaultRule: TargetActionDto,
)

internal data class UserOverrideDto(
    val userId: String,
    val variationId: Long,
)

internal data class BucketDto(
    val id: Long,
    val seed: Int,
    val slotSize: Int,
    val slots: List<SlotDto>,
)

internal data class SlotDto(
    val startInclusive: Int,
    val endExclusive: Int,
    val variationId: Long,
)

internal data class EventTypeDto(
    val id: Long,
    val key: String,
)

internal data class TargetDto(
    val conditions: List<ConditionDto>
) {
    data class ConditionDto(
        val key: KeyDto,
        val match: MatchDto
    )

    data class KeyDto(
        val type: String,
        val name: String
    )

    data class MatchDto(
        val type: String,
        val operator: String,
        val valueType: String,
        val values: List<Any>
    )
}

internal data class TargetActionDto(
    val type: String,
    val variationId: Long?,
    val bucketId: Long?
)

internal data class TargetRuleDto(
    val target: TargetDto,
    val action: TargetActionDto
)

internal data class SegmentDto(
    val id: Long,
    val key: String,
    val type: String,
    val targets: List<TargetDto>,
)

internal data class ContainerDto(
    val containerId: Long,
    val environmentId: Long,
    val bucketId: Long,
    val groups: List<ContainerGroupDto>
)

internal data class ContainerGroupDto(
    val containerGroupId: Long,
    val containerId: Long,
    val experiments: List<Long>
)
