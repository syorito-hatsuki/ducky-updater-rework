package dev.syoritohatsuki.duckyupdaterrework.core

import dev.syoritohatsuki.duckyupdaterrework.core.api.ModrinthApi
import dev.syoritohatsuki.duckyupdaterrework.core.api.models.Version
import dev.syoritohatsuki.duckyupdaterrework.storage.Database
import dev.syoritohatsuki.duckyupdaterrework.util.Hash
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DuckyUpdaterApi {

    private val modsHashes = Hash.getSha512Hashes()

    fun checkForUpdates() {
        CoroutineScope(Dispatchers.IO).launch {
            ModrinthApi.getLatestVersionsFromHashes(modsHashes.keys.toList()).forEach { (hash, version) ->
                Database.update("INSERT INTO projects (modId, projectId, fileHash, version, url) VALUES ('${modsHashes[hash]}', '${version.projectId}', '${hash}', '${version.versionNumber}', '${version.files[0].url}')")
                version.dependencies.checkForDependency(version.projectId)
            }
        }
    }

    private suspend fun List<Version.Dependency>.checkForDependency(projectId: String) {

        val missing = mutableMapOf<String, HashSet<String>>()

        forEach { dependency ->
            when {
                !dependency.dependencyType.equals("required") -> return
                dependency.versionId != null -> missing.computeIfAbsent(projectId) { hashSetOf() }
                    .add(dependency.versionId)

                dependency.projectId != null -> ModrinthApi.getProjectVersions(dependency.projectId)[0].let {
                    Database.update("INSERT INTO projects (projectId, version, url) VALUES ('${it.projectId}', '${it.versionNumber}', '${it.files[0].url}')")
                    Database.update("INSERT INTO dependencies (projectId, dependencyProjectId) VALUES ('${projectId}', '${it.projectId}')")
                }
            }
        }

        missing.forEach {
            ModrinthApi.getMultiplyVersions(it.value).forEach { depVersion ->
                depVersion.dependencies.checkForDependency(it.key)
            }
        }
    }
}