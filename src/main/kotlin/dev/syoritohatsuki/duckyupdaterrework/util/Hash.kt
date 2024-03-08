package dev.syoritohatsuki.duckyupdaterrework.util

import com.google.common.hash.Hashing
import com.google.common.io.Files
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.metadata.ModMetadata
import net.fabricmc.loader.api.metadata.ModOrigin
import java.io.File
import java.io.IOException
import kotlin.jvm.optionals.getOrNull

object Hash {
    fun getSha512Hashes(): Map<String, ModMetadata> = mutableMapOf<String, ModMetadata>().apply {
        FabricLoader.getInstance().allMods.forEach { container ->
            containerToFile(container)?.let { file ->
                if (file.isFile) hashFile(file)?.let {
                    this[it] = container.metadata
                }
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