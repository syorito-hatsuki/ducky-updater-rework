package dev.syoritohatsuki.duckyupdaterrework.core.config

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import java.io.File

object ConfigManager {
    private val configDir: File = FabricLoader.getInstance().configDir.toFile()
    private val configFile = File(configDir, "durw.json")

    private val configJson = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    init {
        if (!configDir.exists()) configDir.mkdirs()
        if (!configFile.exists()) configFile.writeText(configJson.encodeToString(Config()))
    }

    fun read() = configJson.decodeFromString<Config>(configFile.readText())

    fun Config.write() = configFile.writeText(configJson.encodeToString(this))
}