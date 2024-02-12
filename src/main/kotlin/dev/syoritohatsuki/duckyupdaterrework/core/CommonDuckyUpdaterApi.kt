package dev.syoritohatsuki.duckyupdaterrework.core

import dev.syoritohatsuki.duckyupdaterrework.core.api.ModrinthApi
import dev.syoritohatsuki.duckyupdaterrework.core.api.models.Version
import dev.syoritohatsuki.duckyupdaterrework.util.Hash

object CommonDuckyUpdaterApi : DuckyUpdaterApi {
    override suspend fun getUpdates(result: (Map<String, Version>?) -> Unit) {
        result(ModrinthApi.getLatestVersionsFromHashes(Hash.getSha512Hashes()))
    }

    override suspend fun getUpdate(modId: String, result: (Version?) -> Unit) {
        val hash = Hash.getSha512Hash(modId) ?: return result(null)
        result(ModrinthApi.getLatestVersionFromHash(hash))
    }

    override suspend fun update(result: (Boolean) -> Unit) {
        TODO("Not yet implemented")
    }
}