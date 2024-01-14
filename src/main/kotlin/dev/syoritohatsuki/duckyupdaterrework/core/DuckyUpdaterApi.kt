package dev.syoritohatsuki.duckyupdaterrework.core

import dev.syoritohatsuki.duckyupdaterrework.api.models.Project

interface DuckyUpdaterApi {
    fun getUpdates(result: (String?) -> Unit)
    suspend fun getUpdate(modId: String, result: (Project?) -> Unit)
    fun update(result: (Boolean) -> Unit)
    fun ignoreUpdate(modId: String, result: (Boolean) -> Unit)
}