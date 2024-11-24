package dev.syoritohatsuki.duckyupdaterrework.core.util

import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork
import dev.syoritohatsuki.duckyupdaterrework.core.config.ConfigManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import net.lingala.zip4j.ZipFile
import java.io.File
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.absolutePathString
import kotlin.io.path.pathString

object FileActions {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val defaultModsDirectory = DuckyUpdaterReWork.rootModsDir

    private val configDir: File = Paths.get(FabricLoader.getInstance().configDir.absolutePathString(), "durw").toFile()

    private val prepareFile = configDir.resolve(".prepair.json").apply {
        if (!exists()) {
            createNewFile()
            writeText(json.encodeToString(mutableMapOf<String, String>()))
        }
    }

    private val prepare: MutableMap<String, String> = json.decodeFromString(prepareFile.readText())

    enum class FileAction {
        DELETE, DISABLE, ARCHIVE
    }

    fun prepareAction(file: String, path: String?) {
        prepareFile.writeText(json.encodeToString(prepare.apply {
            put(file, path ?: defaultModsDirectory.toAbsolutePath().pathString)
        }))
    }

    fun run() {
        var date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        if (System.getProperty("os.name").lowercase().contains("windows")) date = date.replace(":", "-")

        DuckyUpdaterReWork.logger.debug("-----[ Runtime of File Action ]-----")
        DuckyUpdaterReWork.logger.debug("Default mods directory: {}", defaultModsDirectory)
        DuckyUpdaterReWork.logger.debug("Operation System: ${System.getProperty("os.name")}")
        DuckyUpdaterReWork.logger.debug("Filename for zip file: DURW [${date}].zip")
        DuckyUpdaterReWork.logger.debug("------------------------------------")

        val action = ConfigManager.read().fileAction
        prepare.apply {
            forEach {
                when (action) {
                    FileAction.DELETE -> deleteOldMod(it.key, it.value)
                    FileAction.DISABLE -> disableOldMod(it.key, it.value)
                    FileAction.ARCHIVE -> archiveOldMods(it.key, it.value, date)
                }
            }
            clear()
            prepareFile.writeText(json.encodeToString(this))
        }
    }

    private fun archiveOldMods(oldFile: String, path: String, date: String) {
        File(path).apply {
            val old = resolve("old")
            if (!old.exists()) old.mkdir()
            ZipFile(old.resolve("DURW [${date}].zip")).apply {
                resolve(oldFile).apply {
                    if (exists()) addFile(this)
                    else DuckyUpdaterReWork.logger.warn("File not exist for archive: $oldFile (Can happen sometimes :D)")
                }
            }
        }

        deleteOldMod(oldFile, path)
    }

    private fun disableOldMod(oldFilePath: String, path: String) {
        val dir = File(path)
        dir.resolve(oldFilePath).apply {
            if (exists()) renameTo(dir.resolve("${oldFilePath}.disabled"))
            else DuckyUpdaterReWork.logger.warn("File not exist for disable: $oldFilePath (Can happen sometimes :D)")
        }
    }

    private fun deleteOldMod(oldFilePath: String, path: String) {
        File(path).resolve(oldFilePath).apply {
            if (exists()) delete()
            else DuckyUpdaterReWork.logger.warn("File not exist for deleting: $oldFilePath (Can happen sometimes :D)")
        }
    }
}