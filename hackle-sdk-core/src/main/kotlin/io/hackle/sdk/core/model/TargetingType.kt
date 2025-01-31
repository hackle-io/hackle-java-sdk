package io.hackle.sdk.core.model

/**
 * 타겟팅 타입
 *
 * 논리적으로 올바르지 않은 타겟팅 타입을 방어하기 위한 열거형
 *
 * 서버에 잘못된 타겟팅 타입이 전달된 경우 null로 처리
 */
enum class TargetingType(
    private val supportedKeyTypes: Set<Target.Key.Type>
) {
    /**
     * 식별자
     */
    IDENTIFIER(
        Target.Key.Type.SEGMENT,
    ),

    /**
     * 속성
     */
    PROPERTY(
        Target.Key.Type.SEGMENT,
        Target.Key.Type.USER_PROPERTY,
        Target.Key.Type.EVENT_PROPERTY,
        Target.Key.Type.HACKLE_PROPERTY,
        Target.Key.Type.AB_TEST,
        Target.Key.Type.FEATURE_FLAG,
        Target.Key.Type.COHORT,
        Target.Key.Type.NUMBER_OF_EVENTS_IN_DAYS,
        Target.Key.Type.NUMBER_OF_EVENT_WITH_PROPERTY_IN_DAYS
    ),

    /**
     * 세그먼트
     *
     * 대시보드 - 테스트 기기 관리 - 테스트 기기 / 타겟팅 그룹
     */
    SEGMENT(
        Target.Key.Type.USER_ID,
        Target.Key.Type.USER_PROPERTY,
        Target.Key.Type.HACKLE_PROPERTY,
        Target.Key.Type.COHORT,
        Target.Key.Type.NUMBER_OF_EVENTS_IN_DAYS,
        Target.Key.Type.NUMBER_OF_EVENT_WITH_PROPERTY_IN_DAYS
    );

    constructor(vararg keyTypes: Target.Key.Type) : this(keyTypes.toSet())

    fun supports(keyType: Target.Key.Type): Boolean {
        return keyType in supportedKeyTypes
    }
}
