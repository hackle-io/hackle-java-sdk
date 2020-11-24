package io.hackle.sdk

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.User

/**
 * @author Yong
 */
object Hackle

fun Hackle.client(sdkKey: String): HackleClient = HackleClients.create(sdkKey)

fun Hackle.user(id: String) = User.of(id)
fun Hackle.user(id: String, init: User.Builder.() -> Unit) = User.builder(id).apply(init).build()

fun Hackle.event(key: String) = Event.of(key)
fun Hackle.event(key: String, init: Event.Builder.() -> Unit) = Event.builder(key).apply(init).build()
