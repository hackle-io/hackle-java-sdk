package io.hackle.sdk.core.model

class Container (
    val id: Long,
    val bucketId: Long,
    val groups: List<ContainerGroup>
) {

    fun findGroup(containerGroupId: Long): ContainerGroup? {
        return groups.firstOrNull { it.id == containerGroupId }
    }
}