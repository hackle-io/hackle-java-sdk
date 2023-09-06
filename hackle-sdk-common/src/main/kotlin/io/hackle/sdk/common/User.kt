package io.hackle.sdk.common

/**
 * @author Yong
 */
data class User internal constructor(
    val id: String?,
    val userId: String?,
    val deviceId: String?,
    val identifiers: Map<String, String>,
    val properties: Map<String, Any>,
    val hackleProperties: Map<String, Any>
) {

    fun toBuilder(): Builder {
        return Builder(this)
    }

    class Builder internal constructor() {

        internal constructor(user: User) : this() {
            id(user.id)
            userId(user.userId)
            deviceId(user.deviceId)
            identifiers.add(user.identifiers)
            properties.add(user.properties)
            hackleProperties.add(user.hackleProperties)
        }

        private var id: String? = null
        private var userId: String? = null
        private var deviceId: String? = null
        private val identifiers = IdentifiersBuilder()
        private val properties = PropertiesBuilder()
        private val hackleProperties = PropertiesBuilder()

        fun id(id: String?) = apply { this.id = id }
        fun userId(userId: String?) = apply { this.userId = userId }
        fun deviceId(deviceId: String?) = apply { this.deviceId = deviceId }
        fun identifier(type: String, value: String?) = apply { identifiers.add(type, value) }
        fun identifiers(identifiers: Map<String, String?>?) = apply { identifiers?.let { this.identifiers.add(it) } }

        fun property(key: String, value: Any?) = apply { properties.add(key, value) }
        fun properties(properties: Map<String, Any?>?) = apply { properties?.let { this.properties.add(it) } }

        fun platform(platform: String?) = hackleProperty("platform", platform)
        fun osName(osName: String?) = hackleProperty("osName", osName)
        fun osVersion(osVersion: String?) = hackleProperty("osVersion", osVersion)
        fun deviceModel(deviceModel: String?) = hackleProperty("deviceModel", deviceModel)
        fun deviceType(deviceType: String?) = hackleProperty("deviceType", deviceType)
        fun deviceBrand(deviceBrand: String?) = hackleProperty("deviceBrand", deviceBrand)
        fun deviceManufacturer(deviceManufacturer: String?) = hackleProperty("deviceManufacturer", deviceManufacturer)
        fun locale(locale: String?) = hackleProperty("locale", locale)
        fun language(language: String?) = hackleProperty("language", language)
        fun timeZone(timeZone: String?) = hackleProperty("timeZone", timeZone)
        fun orientation(orientation: String?) = hackleProperty("orientation", orientation)
        fun screenWidth(screenWidth: Int) = hackleProperty("screenWidth", screenWidth)
        fun screenHeight(screenHeight: Int) = hackleProperty("screenHeight", screenHeight)
        fun carrierCode(carrierCode: String?) = hackleProperty("carrierCode", carrierCode)
        fun carrierName(carrierName: String?) = hackleProperty("carrierName", carrierName)
        fun isWifi(isWifi: Boolean) = hackleProperty("isWifi", isWifi)
        fun packageName(packageName: String?) = hackleProperty("packageName", packageName)
        fun versionCode(versionCode: Int) = hackleProperty("versionCode", versionCode)
        fun versionName(versionName: String?) = hackleProperty("versionName", versionName)
        fun isApp(isApp: Boolean) = hackleProperty("isApp", isApp)

        private fun hackleProperty(key: String, value: Any?) = apply { hackleProperties.add(key, value) }

        fun build(): User {
            return User(
                id = id,
                userId = userId,
                deviceId = deviceId,
                identifiers = identifiers.build(),
                properties = properties.build(),
                hackleProperties = hackleProperties.build()
            )
        }
    }

    companion object {

        @JvmStatic
        fun of(id: String): User {
            return Builder().id(id).build()
        }

        @JvmStatic
        fun builder(id: String): Builder {
            return Builder().id(id)
        }

        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }
}
