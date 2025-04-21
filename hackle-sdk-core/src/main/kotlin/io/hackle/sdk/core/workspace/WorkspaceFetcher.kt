package io.hackle.sdk.core.workspace

/**
 * @author Yong
 */
interface WorkspaceFetcher {
    val lastModified: String?
    fun fetch(): Workspace?
}
