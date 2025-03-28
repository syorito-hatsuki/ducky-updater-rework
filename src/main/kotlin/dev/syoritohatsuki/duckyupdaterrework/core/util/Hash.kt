package dev.syoritohatsuki.duckyupdaterrework.core.util

import com.google.common.hash.Hashing
import com.google.common.io.Files
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.metadata.ModOrigin
import java.io.File
import java.io.IOException
import kotlin.io.path.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.jvm.optionals.getOrNull

object Hash {
    fun getSha512Hashes(): Map<String, ModContainer> = mutableMapOf<String, ModContainer>().apply {
        FabricLoader.getInstance().allMods.forEach { container ->
            containerToFile(container)?.let { file ->
                if (file.isFile) hashFile(file)?.let {
                    this[it] = container
                }
            }
        }
    }

    fun getSha512Hashes(searchPath: String): Map<String, File> = mutableMapOf<String, File>().apply {
        Path(searchPath).listDirectoryEntries().forEach { path ->
            val file = path.toFile()
            if (path.isRegularFile()) hashFile(file)?.let {
                this[it] = file
            }
        }
    }

    private fun hashFile(file: File): String? = try {
        Files.asByteSource(file).hash(Hashing.sha512()).toString()
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }

    private fun containerToFile(container: ModContainer): File? = when {
        container.containingMod.isEmpty && container.origin.kind == ModOrigin.Kind.PATH -> container.origin.paths.stream()
            .filter { path ->
                path.toString().lowercase().endsWith(".jar")
            }.findFirst().getOrNull()?.toFile()

        else -> null
    }
}
