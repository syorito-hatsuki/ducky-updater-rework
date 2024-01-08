package dev.syoritohatsuki.duckyupdaterrework.util

import com.google.gson.GsonBuilder
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork.MOD_ID
import java.io.File
import java.nio.file.Paths

object ConfigManager {

    private val configDir = Paths.get("", "config", MOD_ID).toFile()
    private val configFile = File(configDir, "config.json")

    private val gson = GsonBuilder().setPrettyPrinting().create()

    init {
        if (!configDir.exists()) configDir.mkdirs()
        if (!configFile.exists()) configFile.writeText(gson.toJson(Config()))
    }

    fun getIgnored(): MutableMap<String, String> =
        gson.fromJson(configFile.readText(), Config::class.java).ignoreUpdate

    fun isIgnoredVersion(modId: String, version: String): Boolean =
        gson.fromJson(configFile.readText(), Config::class.java).ignoreUpdate[modId] == version

    fun addVersionToIgnore(modId: String, version: String) {
        gson.fromJson(configFile.readText(), Config::class.java).apply {
            ignoreUpdate[modId] = version
        }.write()
    }

    fun isUpdateOnStartUpEnabled(): Boolean {
        return gson.fromJson(configFile.readText(), Config::class.java).updateOnStartup
    }

    fun changeUpdateOnStartUp(enable: Boolean) {
        gson.fromJson(configFile.readText(), Config::class.java).apply {
            updateOnStartup = enable
        }.write()
    }

    fun getThreadCount(): Int = gson.fromJson(configFile.readText(), Config::class.java).threadCount

    private fun Config.write() = configFile.writeText(gson.toJson(this))

    data class Config(
        var updateOnStartup: Boolean = false,
        val threadCount: Int = 4,
        val ignoreUpdate: MutableMap<String, String> = mutableMapOf()
    )
}