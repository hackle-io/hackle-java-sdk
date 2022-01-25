package io.hackle.sdk.core.model

enum class TargetingType(
    private val supportedKeyTypes: Set<Target.Key.Type>
) {

    IDENTIFIER(
        Target.Key.Type.SEGMENT,
    ),

    PROPERTY(
        Target.Key.Type.SEGMENT,
        Target.Key.Type.USER_PROPERTY,
        Target.Key.Type.HACKLE_PROPERTY,
    ),

    SEGMENT(
        Target.Key.Type.USER_ID,
        Target.Key.Type.USER_PROPERTY,
        Target.Key.Type.HACKLE_PROPERTY,
    );

    constructor(vararg keyTypes: Target.Key.Type) : this(keyTypes.toSet())

    fun supports(keyType: Target.Key.Type): Boolean {
        return keyType in supportedKeyTypes
    }
}