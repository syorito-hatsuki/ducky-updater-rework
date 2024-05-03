package dev.syoritohatsuki.duckyupdaterrework.core.util

import net.fabricmc.loader.api.FabricLoader
import net.lingala.zip4j.ZipFile
import kotlin.io.path.createDirectory
import kotlin.io.path.notExists

object FileActions {
    private val modsDirectory = FabricLoader.getInstance().gameDir.resolve("mods")
    private val modsDirectoryFile = modsDirectory.toFile()
    private val archivedModsDirectory = modsDirectory.resolve("old").apply {
        if (notExists()) createDirectory()
    }.toFile()

    enum class FileAction {
        DELETE, DISABLE, ARCHIVE
    }

    fun archiveOldMods(oldFile: String, date: String) {
        ZipFile(
            archivedModsDirectory.resolve("DURW [${date}].zip")
        ).addFile(modsDirectoryFile.resolve(oldFile))
        deleteOldMod(oldFile)
    }

    fun disableOldMod(oldFilePath: String) {
        modsDirectoryFile.resolve(oldFilePath).renameTo(modsDirectoryFile.resolve("${oldFilePath}.disabled"))
    }

    fun deleteOldMod(oldFilePath: String) {
        modsDirectoryFile.resolve(oldFilePath).delete()
    }
}