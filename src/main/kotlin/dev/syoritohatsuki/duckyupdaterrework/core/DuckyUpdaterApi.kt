package dev.syoritohatsuki.duckyupdaterrework.core

import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork
import dev.syoritohatsuki.duckyupdaterrework.core.api.ModrinthApi
import dev.syoritohatsuki.duckyupdaterrework.core.api.models.Loader
import dev.syoritohatsuki.duckyupdaterrework.core.api.models.Version
import dev.syoritohatsuki.duckyupdaterrework.core.storage.Database
import dev.syoritohatsuki.duckyupdaterrework.core.util.Hash
import java.io.File
import kotlin.io.path.absolutePathString
import kotlin.io.path.name

object DuckyUpdaterApi {

    private val modsHashes = Hash.getSha512Hashes()

    suspend fun checkForUpdates() {
        ModrinthApi.getLatestVersionsFromHashes(modsHashes.keys.toList(), Loader.FABRIC).forEach { (hash, version) ->

            val file = version.files.firstOrNull() ?: return@forEach

            if (file.hashes.sha512 == hash) {
                DuckyUpdaterReWork.logger.debug("1.1: ${version.projectId} ${(modsHashes[hash]?.metadata?.name ?: version.name).escaping()} | ${file.url}")
                return@forEach
            }

            DuckyUpdaterReWork.logger.debug("1.2: ${version.projectId} ${(modsHashes[hash]?.metadata?.name ?: version.name).escaping()} | ${file.url}")

            Database.insertOrUpdateProject(
                modId = modsHashes[hash]?.metadata?.id,
                projectId = version.projectId,
                name = (modsHashes[hash]?.metadata?.name ?: version.name).escaping(),
                changelog = version.changelog.escaping(),
                fileHash = hash,
                version = version.versionNumber,
                url = file.url,
                filePath = modsHashes[hash]?.origin?.paths?.get(0)?.toAbsolutePath()?.absolutePathString()
                    ?.substringBeforeLast(File.separator),
                fileName = modsHashes[hash]?.origin?.paths?.get(0)?.name,
                outdated = true
            )

            version.dependencies.checkForDependency(version.projectId)

        }

        fixNullModNames()
    }

    private suspend fun List<Version.Dependency>.checkForDependency(projectId: String) {
        val missing = mutableMapOf<String, HashSet<String>>()

        forEach { dependency ->
            when {
                !dependency.dependencyType.equals("required") -> {
                    DuckyUpdaterReWork.logger.debug("2.1: {} | {}", projectId, dependency)
                    return@forEach
                }

                dependency.versionId != null -> {
                    DuckyUpdaterReWork.logger.debug("2.2: {} | {}", projectId, dependency)
                    missing.computeIfAbsent(projectId) { hashSetOf() }.add(dependency.versionId)
                }

                dependency.projectId != null -> {
                    DuckyUpdaterReWork.logger.debug("2.3: {} | {}", projectId, dependency)
                    ModrinthApi.getProjectVersions(dependency.projectId, Loader.FABRIC).ifEmpty {
                        DuckyUpdaterReWork.logger.debug("2.3.1: {} | {}", projectId, dependency)
                        return@forEach
                    }[0].let {

                        if (modsHashes.contains(it.files.first().hashes.sha512)) {
                            DuckyUpdaterReWork.logger.debug("2.3.2: {} | {}", projectId, dependency)
                            return@forEach
                        }

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
            DuckyUpdaterReWork.logger.debug("3: {} | {}", it.key, it.value)
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

    private fun String.escaping(): String = replace("'", "''")
}
