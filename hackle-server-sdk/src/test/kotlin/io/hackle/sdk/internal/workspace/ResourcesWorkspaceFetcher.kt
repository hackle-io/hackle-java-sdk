package io.hackle.sdk.internal.workspace

import io.hackle.sdk.core.workspace.Workspace
import io.hackle.sdk.core.workspace.WorkspaceFetcher
import io.hackle.sdk.internal.utils.parseJson
import java.nio.file.Files
import java.nio.file.Paths

internal class ResourcesWorkspaceFetcher(fileName: String) : WorkspaceFetcher {

    private val workspace: Workspace

    init {
        val dto = String(Files.readAllBytes(Paths.get("src/test/resources/$fileName"))).parseJson<WorkspaceDto>()
        workspace = DefaultWorkspace.from(dto)
    }

    override fun fetch(): Workspace {
        return workspace
    }
}
