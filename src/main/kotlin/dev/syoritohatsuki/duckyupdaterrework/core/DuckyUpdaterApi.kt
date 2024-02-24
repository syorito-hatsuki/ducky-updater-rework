package dev.syoritohatsuki.duckyupdaterrework.core

import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork
import dev.syoritohatsuki.duckyupdaterrework.core.api.ModrinthApi
import dev.syoritohatsuki.duckyupdaterrework.core.api.models.Version
import dev.syoritohatsuki.duckyupdaterrework.util.Hash
import java.util.concurrent.ConcurrentHashMap

object DuckyUpdaterApi {

    private val _updates = mutableMapOf<String, Version>()

    private val _urls = ConcurrentHashMap<String, String>()
    private val _versions = mutableSetOf<String>()

    suspend fun checkForUpdates() {
        _updates.putAll(ModrinthApi.getLatestVersionsFromHashes(Hash.getSha512Hashes()))
    }

    suspend fun checkForUpdates(modsIdsHashes: List<String>) {
        _updates.putAll(ModrinthApi.getLatestVersionsFromHashes(modsIdsHashes))
    }

    suspend fun checkForUpdate(modId: String) {
        val hash = Hash.getSha512Hash(modId) ?: return
        _updates[hash] = ModrinthApi.getLatestVersionFromHash(hash)
    }

    suspend fun getUpdatesUrls(): Map<String, String> {
        _updates.forEach { (hash, version) ->
            version.dependencies.forEach dep@{ dependency ->
                DuckyUpdaterReWork.logger.info("PVF: ${dependency.projectId} | ${dependency.versionId} | ${dependency.fileName}")
                when {
                    dependency.fileName != null -> _urls[dependency.projectId!!] =
                        "https://cdn.modrinth.com/data/${dependency.projectId}/versions/${dependency.versionId}/${dependency.fileName}"

                    dependency.versionId != null -> _versions.add(dependency.versionId)
                    dependency.projectId != null -> _urls[dependency.projectId] =
                        ModrinthApi.getProjectVersions(dependency.projectId).first().files.first().url

                    else -> return@dep
                }
            }
            _urls[version.projectId] = version.files.first().url
        }

        ModrinthApi.getMultiplyVersions(_versions).forEach {
            _urls[it.projectId] = it.files.first().url
        }

        return _urls
    }
}