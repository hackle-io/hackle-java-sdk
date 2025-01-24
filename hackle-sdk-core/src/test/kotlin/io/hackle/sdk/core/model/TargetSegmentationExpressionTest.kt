import io.hackle.sdk.core.model.PropertyKey
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.TargetSegmentationExpression.NumberOfEventsInDays
import io.hackle.sdk.core.model.TargetSegmentationOption
import io.hackle.sdk.core.model.ValueType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TargetSegmentationExpressionTest {

    @Test
    fun `test NumberOfEventsInDays with valid period for DAYS`() {
        val timeRangeDays = TargetSegmentationOption.TimeRange(10, TargetSegmentationOption.TimeRange.TimeUnit.DAYS)
        val expressionDays = NumberOfEventsInDays("eventKey", timeRangeDays, null)
        assert(expressionDays.timeRange.period <= NumberOfEventsInDays.MAX_DAY_PERIOD)

        val timeRangeWeeks = TargetSegmentationOption.TimeRange(4, TargetSegmentationOption.TimeRange.TimeUnit.WEEKS)
        val expressionWeeks = NumberOfEventsInDays("eventKey", timeRangeWeeks, null)
        assert(expressionWeeks.timeRange.period <= NumberOfEventsInDays.MAX_DAY_PERIOD)
        assert(expressionWeeks.timeRange.periodDays == 4 * 7)
    }

    @Test
    fun `test NumberOfEventsInDays with invalid period`() {
        val timeRangeDays = TargetSegmentationOption.TimeRange(31, TargetSegmentationOption.TimeRange.TimeUnit.DAYS)
        assertThrows<IllegalArgumentException> {
            NumberOfEventsInDays("eventKey", timeRangeDays, null)
        }

        val timeRangeWeeks = TargetSegmentationOption.TimeRange(5, TargetSegmentationOption.TimeRange.TimeUnit.WEEKS)
        assertThrows<IllegalArgumentException> {
            NumberOfEventsInDays("eventKey", timeRangeWeeks, null)
        }
    }

    @Test
    fun `test NumberOfEventsInDays with non-null filters`() {
        val timeRange = TargetSegmentationOption.TimeRange(10, TargetSegmentationOption.TimeRange.TimeUnit.DAYS)
        val filters = listOf(TargetSegmentationOption.PropertyFilter(
            PropertyKey(PropertyKey.Type.EVENT, "propertyKey"), Target.Match(
                Target.Match.Type.MATCH, Target.Match.Operator.IN, ValueType.NUMBER, listOf(1))))
        val expression = NumberOfEventsInDays("eventKey", timeRange, filters)
        assert(expression.filters == filters)
    }
}