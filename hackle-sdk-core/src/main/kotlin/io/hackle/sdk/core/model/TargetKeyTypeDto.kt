package io.hackle.sdk.core.model

import io.hackle.sdk.core.internal.log.Logger
import io.hackle.sdk.core.internal.utils.enumValueOfOrNull

data class TargetKeyTypeDto(val type: Target.Key.Type) {
    companion object {
        fun from(rawValue: String): TargetKeyTypeDto? {
            val type = enumValueOfOrNull<Target.Key.Type>(rawValue)
            if (type == null) {
                log.warn { "Unsupported type[$rawValue]. Please use the latest version of sdk." }
                return null
            }

            return TargetKeyTypeDto(type)
        }

        private val log = Logger<TargetKeyTypeDto>()
    }
}
