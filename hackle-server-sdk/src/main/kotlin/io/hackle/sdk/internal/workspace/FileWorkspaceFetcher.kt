package io.hackle.sdk.internal.workspace

import io.hackle.sdk.core.workspace.Workspace
import io.hackle.sdk.core.workspace.WorkspaceFetcher
import io.hackle.sdk.internal.utils.parseJson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.System.lineSeparator
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors.joining

internal class FileWorkspaceFetcher : WorkspaceFetcher {

    private val workspace: Workspace

    init {
        val json = FileWorkspaceFetcher::class.java.getResourceAsStream("/workspace_config.json").use {
            BufferedReader(InputStreamReader(it!!, StandardCharsets.UTF_8)).lines().collect(joining(lineSeparator()))
        }
        val dto = json.parseJson<WorkspaceDto>()
        workspace = WorkspaceImpl.from(dto)
    }


    override fun fetch(): Workspace? {
        return workspace
    }
}