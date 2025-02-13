package io.hackle.sdk.core.internal.workspace

import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.TargetingType
import io.hackle.sdk.core.model.ValueType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class targetDtoExtensionsTest {

    @Test
    fun `toConditionOrNull should return condition when valid`() {
        val conditionDto = TargetDto.ConditionDto(
            key = TargetDto.KeyDto(type = Target.Key.Type.EVENT_PROPERTY.toString(), name = "userId"),
            match = TargetDto.MatchDto(type = "MATCH", operator = "IN", valueType = "STRING", values = listOf("value1"))
        )
        val targetingType = TargetingType.PROPERTY

        val result = conditionDto.toConditionOrNull(targetingType)

        assertNotNull(result)
        assertEquals("userId", result?.key?.name)
        assertEquals(Target.Match.Type.MATCH, result?.match?.type)
    }

    @Test
    fun `toConditionOrNull should return null when key is not support type`() {
        val conditionDto = TargetDto.ConditionDto(
            key = TargetDto.KeyDto(type = Target.Key.Type.USER_ID.toString(), name = "userId"),
            match = TargetDto.MatchDto(type = "MATCH", operator = "IN", valueType = "STRING", values = listOf("value1"))
        )
        val targetingType = TargetingType.PROPERTY

        val result = conditionDto.toConditionOrNull(targetingType)

        assertNull(result)
    }

    @Test
    fun `toConditionOrNull should return null when key is invalid`() {
        val conditionDto = TargetDto.ConditionDto(
            key = TargetDto.KeyDto(type = "INVALID_TYPE", name = "userId"),
            match = TargetDto.MatchDto(type = "MATCH", operator = "IN", valueType = "STRING", values = listOf("value1"))
        )
        val targetingType = TargetingType.PROPERTY

        val result = conditionDto.toConditionOrNull(targetingType)

        assertNull(result)
    }

    @Test
    fun `toTargetKeyOrNull should return key when valid`() {
        val keyDto = TargetDto.KeyDto(type = "USER_ID", name = "userId")

        val result = keyDto.toTargetKeyOrNull()

        assertNotNull(result)
        assertEquals(Target.Key.Type.USER_ID, result?.type)
        assertEquals("userId", result?.name)
    }

    @Test
    fun `toTargetKeyOrNull should return null when type is invalid`() {
        val keyDto = TargetDto.KeyDto(type = "INVALID_TYPE", name = "userId")

        val result = keyDto.toTargetKeyOrNull()

        assertNull(result)
    }

    @Test
    fun `toMatchOrNull should return match when valid`() {
        val matchDto = TargetDto.MatchDto(type = "MATCH", operator = "IN", valueType = "STRING", values = listOf("value1"))

        val result = matchDto.toMatchOrNull()

        assertNotNull(result)
        assertEquals(Target.Match.Type.MATCH, result?.type)
        assertEquals(Target.Match.Operator.IN, result?.operator)
        assertEquals(ValueType.STRING, result?.valueType)
    }

    @Test
    fun `toMatchOrNull should return null when type is invalid`() {
        val matchDto = TargetDto.MatchDto(type = "INVALID_TYPE", operator = "IN", valueType = "STRING", values = listOf("value1"))

        val result = matchDto.toMatchOrNull()

        assertNull(result)
    }

    @Test
    fun `toTargetSegmentationExpression should return NumberOfEventsInDays`() {
        val dto = TargetDto.NumberOfEventsInDaysDto(eventKey = "eventKey", days = 5)

        val result = dto.toTargetSegmentationExpression()

        assertNotNull(result)
        assertEquals("eventKey", result.eventKey)
        assertEquals(5, result.days)
    }

    @Test
    fun `toTargetSegmentationExpression should return NumberOfEventsWithPropertyInDays`() {
        val conditionDto = TargetDto.ConditionDto(
            key = TargetDto.KeyDto(type = Target.Key.Type.EVENT_PROPERTY.toString(), name = "userId"),
            match = TargetDto.MatchDto(type = "MATCH", operator = "IN", valueType = "STRING", values = listOf("value1"))
        )
        val dto = TargetDto.NumberOfEventsWithPropertyInDaysDto(eventKey = "eventKey", days = 5, propertyFilter = conditionDto)

        val result = dto.toTargetSegmentationExpression()

        assertNotNull(result)
        assertEquals("eventKey", result.eventKey)
        assertEquals(5, result.days)
        assertEquals("userId", result.propertyFilter.key.name)
    }
}