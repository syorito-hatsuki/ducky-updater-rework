package dev.syoritohatsuki.duckyupdaterrework.core

import dev.syoritohatsuki.duckyupdaterrework.core.api.ModrinthApi
import dev.syoritohatsuki.duckyupdaterrework.core.api.models.Version
import dev.syoritohatsuki.duckyupdaterrework.util.Hash

object CommonDuckyUpdaterApi : DuckyUpdaterApi {
    override suspend fun getUpdates(result: (Map<String, Version>?) -> Unit) {
        result(ModrinthApi.getLatestVersionsFromHashes(Hash.getSha512Hashes()))
    }

    override suspend fun getUpdates(modsIds: List<String>, result: (Map<String, Version>?) -> Unit) {
        result(ModrinthApi.getLatestVersionsFromHashes(Hash.getSha512Hashes(modsIds)))
    }

    override suspend fun getUpdate(modId: String, result: (Version?) -> Unit) {
        result(ModrinthApi.getLatestVersionFromHash(Hash.getSha512Hash(modId) ?: return result(null)))
    }

    override suspend fun update(result: (Boolean) -> Unit) {
        TODO("Not yet implemented")
    }
}