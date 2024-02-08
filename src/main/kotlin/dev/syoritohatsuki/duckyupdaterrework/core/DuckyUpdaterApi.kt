package dev.syoritohatsuki.duckyupdaterrework.core

import dev.syoritohatsuki.duckyupdaterrework.core.api.models.Version

interface DuckyUpdaterApi {
    suspend fun getUpdates(result: (Map<String, Version>?) -> Unit)
    suspend fun getUpdate(modId: String, result: (Version?) -> Unit)
    suspend fun update(result: (Boolean) -> Unit)
    suspend fun ignoreUpdate(modId: String, result: (Boolean) -> Unit)
}