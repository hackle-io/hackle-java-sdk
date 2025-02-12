package io.hackle.sdk.core.model

import java.util.Objects.hash

/**
 * Audience 타겟팅을 위한 Event 객체
 *
 * @param eventKey 타겟팅 할 이벤트 키
 * @param stats 이벤트 통계
 * @param property 추가로 타겟팅에 이용 할 이벤트의 프로퍼티
 */
data class TargetEvent(
    val eventKey: String,
    val stats: List<Stat>,
    val property: Property? = null
    ) {
    /**
     * 이벤트 프로퍼티
     *
     * @param key 프로퍼티 키
     * @param value 프로퍼티 값
     */
    data class Property(
        val key: String,
        val type: Target.Key.Type,
        val value: Any
    )

    /**
     * 이벤트 통계
     *
     * @param date UTC 날짜 타임스탬프 (이벤트가 발생한 날의 00시 00분 00초 기준의 타임스탬프)
     * @param count 이벤트 발생 횟수
     */
    data class Stat(
        val date: Long,
        val count: Int
    )

    override fun hashCode(): Int = hash(eventKey, property)

    override fun equals(other: Any?): Boolean {
        if (other !is TargetEvent) return false

        return eventKey == other.eventKey &&
                property == other.property
    }
}
