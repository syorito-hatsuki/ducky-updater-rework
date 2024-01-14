package dev.syoritohatsuki.duckyupdaterrework.core

import dev.syoritohatsuki.duckyupdaterrework.api.ModrinthApi
import dev.syoritohatsuki.duckyupdaterrework.api.models.Project
import dev.syoritohatsuki.duckyupdaterrework.util.Hash

object CommonDuckyUpdaterApi : DuckyUpdaterApi {
    override fun getUpdates(result: (String?) -> Unit) {
        TODO("Not yet implemented")
    }

    override suspend fun getUpdate(modId: String, result: (Project?) -> Unit) {
        val hash = Hash.getSha512Hash(modId) ?: return result(null)
        result(ModrinthApi.getLatestVersionFromHash(hash))
    }

    override fun update(result: (Boolean) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun ignoreUpdate(modId: String, result: (Boolean) -> Unit) {
        TODO("Not yet implemented")
    }
}