package dev.syoritohatsuki.duckyupdaterrework.core.util

import dev.syoritohatsuki.duckyupdaterrework.core.config.ConfigManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import net.lingala.zip4j.ZipFile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.createDirectory
import kotlin.io.path.notExists

object FileActions {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val modsDirectory = FabricLoader.getInstance().gameDir.resolve("mods")
    private val modsDirectoryFile = modsDirectory.toFile()
    private val archivedModsDirectory = modsDirectory.resolve("old").apply {
        if (notExists()) createDirectory()
    }.toFile()
    private val prepareFile = archivedModsDirectory.resolve(".prepair.json").apply {
        if (!exists()) {
            createNewFile()
            writeText(json.encodeToString(mutableSetOf<String>()))
        }
    }
    private val prepare: MutableSet<String> = json.decodeFromString(prepareFile.readText())

    enum class FileAction {
        DELETE, DISABLE, ARCHIVE
    }

    fun prepareAction(file: String) {
        prepareFile.writeText(json.encodeToString(prepare.apply {
            add(file)
        }))
    }

    fun run() {
        val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val action = ConfigManager.read().fileAction
        prepare.apply {
            forEach {
                when (action) {
                    FileAction.DELETE -> deleteOldMod(it)
                    FileAction.DISABLE -> disableOldMod(it)
                    FileAction.ARCHIVE -> archiveOldMods(it, date)
                }
            }
            clear()
            prepareFile.writeText(json.encodeToString(this))
        }
    }

    private fun archiveOldMods(oldFile: String, date: String) {
        ZipFile(
            archivedModsDirectory.resolve("DURW [${date}].zip")
        ).addFile(modsDirectoryFile.resolve(oldFile))
        deleteOldMod(oldFile)
    }

    private fun disableOldMod(oldFilePath: String) {
        modsDirectoryFile.resolve(oldFilePath).renameTo(modsDirectoryFile.resolve("${oldFilePath}.disabled"))
    }

    private fun deleteOldMod(oldFilePath: String) {
        modsDirectoryFile.resolve(oldFilePath).delete()
    }
}