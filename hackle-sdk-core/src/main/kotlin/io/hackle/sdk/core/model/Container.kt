package io.hackle.sdk.core.model

class Container(
    val id: Long,
    val bucketId: Long,
    val groups: List<ContainerGroup>
) {

    fun getGroupOrNull(containerGroupId: Long): ContainerGroup? {
        return groups.firstOrNull { it.id == containerGroupId }
    }
}

internal val Slot.containerGroupId: Long get() = variationId