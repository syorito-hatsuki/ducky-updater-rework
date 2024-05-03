package dev.syoritohatsuki.duckyupdaterrework.core.config

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.nio.file.Paths

object ConfigManager {
    private val configDir: File = FabricLoader.getInstance().configDir.toFile()
    private val configFile = Paths.get(configDir.path, "durw", "config.json").toFile()

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