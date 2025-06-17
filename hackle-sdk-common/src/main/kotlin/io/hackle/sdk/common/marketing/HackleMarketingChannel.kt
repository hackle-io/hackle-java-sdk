package io.hackle.sdk.common.marketing

enum class HackleMarketingChannel {
    PUSH {
        override fun subscriptionEventKey(): String = "\$push_subscriptions"
    },
    SMS {
        override fun subscriptionEventKey(): String = "\$sms_subscriptions"
    },
    KAKAO {
        override fun subscriptionEventKey(): String = "\$kakao_subscriptions"
    };

    abstract fun subscriptionEventKey(): String
}
