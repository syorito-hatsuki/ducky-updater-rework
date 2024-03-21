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

                val file = version.files.firstOrNull() ?: return@forEach

                if (file.hashes.sha512 == hash) return@forEach

                Database.insertOrUpdateProject(
                    modId = modsHashes[hash]?.id,
                    projectId = version.projectId,
                    name = (modsHashes[hash]?.name ?: version.name).escaping(),
                    changelog = version.changelog.escaping(),
                    fileHash = hash,
                    version = version.versionNumber,
                    url = file.url,
                    outdated = true
                )

                version.dependencies.checkForDependency(version.projectId)

            }

            fixNullModNames()
        }
    }

    private suspend fun List<Version.Dependency>.checkForDependency(projectId: String) {
        val missing = mutableMapOf<String, HashSet<String>>()

        forEach { dependency ->
            when {
                !dependency.dependencyType.equals("required") -> return@forEach
                dependency.versionId != null -> missing.computeIfAbsent(projectId) { hashSetOf() }
                    .add(dependency.versionId)

                dependency.projectId != null -> {
                    ModrinthApi.getProjectVersions(dependency.projectId).ifEmpty { return@forEach }[0].let {

                        val remoteHash = it.files.first().hashes.sha512

                        if (modsHashes.contains(remoteHash)) return@forEach

                        Database.insertOrUpdateProject(
                            projectId = it.projectId,
                            changelog = it.changelog.escaping(),
                            version = it.versionNumber,
                            url = it.files[0].url,
                            outdated = true
                        )

                        Database.update(
                            """INSERT INTO dependencies (
                                projectId, 
                                dependencyProjectId
                            ) VALUES (
                                '${projectId}', 
                                '${it.projectId}'
                            )""".trimMargin()
                        )
                    }
                }
            }
        }

        missing.forEach {
            ModrinthApi.getMultiplyVersions(it.value).forEach { depVersion ->
                depVersion.dependencies.checkForDependency(it.key)
            }
        }
    }

    private suspend fun fixNullModNames() {
        val projectIds = mutableSetOf<String>()

        Database.query("SELECT projects.projectId FROM projects WHERE name IS NULL") {
            while (it.next()) projectIds.add(it.getString("projectId"))
        }

        ModrinthApi.getMultiplyProjects(projectIds).forEach { project ->
            Database.update("UPDATE projects SET name = '${project.title.escaping()}' WHERE projectId IS '${project.id}'")
        }
    }

    fun setIgnore(modId: String? = null, projectId: String? = null, boolean: Boolean) = when {
        modId != null -> Database.update("UPDATE projects SET ignore = '$boolean' WHERE modId IS '$modId'")
        projectId != null -> Database.update("UPDATE projects SET ignore = '$boolean' WHERE projectId IS '$projectId'")
        else -> -1
    }

    private fun String.escaping(): String = replace("'", "''")
}