package dev.syoritohatsuki.duckyupdaterrework.storage

import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork.configDir
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

typealias IgnoreUpdate = MutableMap<String, String>

object IgnoreManager {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    private val configFile = File(configDir, "ignore.json")
    private val ignoreList = json.decodeFromString<IgnoreUpdate>(configFile.readText())

    init {
        if (!configDir.exists()) configDir.mkdirs()
        if (!configFile.exists()) configFile.writeText(json.encodeToString<IgnoreUpdate>(mutableMapOf()))
    }

    fun isIgnored(modId: String, version: String): Boolean = ignoreList[modId] == version

    fun add(modId: String, version: String) {
        ignoreList[modId] = version
        configFile.writeText(json.encodeToString<IgnoreUpdate>(ignoreList))
    }

    fun remove(modId: String) {
        ignoreList.remove(modId)
        configFile.writeText(json.encodeToString<IgnoreUpdate>(ignoreList))
    }
}