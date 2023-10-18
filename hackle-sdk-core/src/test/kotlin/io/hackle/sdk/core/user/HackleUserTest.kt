package io.hackle.sdk.core.user

import io.hackle.sdk.core.model.Cohort
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class HackleUserTest {
    @Test
    fun `build`() {

        val user = HackleUser.builder()
            .identifiers(mapOf("type-1" to "value-1"))
            .identifier("type-2", "value-2")
            .identifier(IdentifierType.ID, "id")
            .identifier(IdentifierType.USER, "userId")
            .identifier(IdentifierType.DEVICE, "deviceId")
            .identifier(IdentifierType.HACKLE_DEVICE_ID, "hackleDeviceId")
            .identifier(IdentifierType.SESSION, "sessionId")
            .properties(mapOf("key-1" to "value-1"))
            .property("key-2", "value-2")
            .hackleProperties(mapOf("hkey-1" to "hvalue-1"))
            .hackleProperty("hkey-2", "hvalue-2")
            .cohort(Cohort(42))
            .cohorts(listOf(Cohort(43), Cohort(44)))
            .build()

        expectThat(user) {
            get { id } isEqualTo "id"
            get { userId } isEqualTo "userId"
            get { deviceId } isEqualTo "deviceId"
            get { sessionId } isEqualTo "sessionId"
            get { identifiers } isEqualTo mapOf(
                "type-1" to "value-1",
                "type-2" to "value-2",
                "\$id" to "id",
                "\$userId" to "userId",
                "\$deviceId" to "deviceId",
                "\$hackleDeviceId" to "hackleDeviceId",
                "\$sessionId" to "sessionId",
            )
            get { properties } isEqualTo mapOf(
                "key-1" to "value-1",
                "key-2" to "value-2",
            )
            get { hackleProperties } isEqualTo mapOf(
                "hkey-1" to "hvalue-1",
                "hkey-2" to "hvalue-2",
            )
            get { cohorts } isEqualTo listOf(
                Cohort(42), Cohort(43), Cohort(44)
            )
        }
        expectThat(user.toBuilder().build()) isEqualTo user
    }
}