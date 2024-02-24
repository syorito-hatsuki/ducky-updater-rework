package dev.syoritohatsuki.duckyupdaterrework.util

import com.google.common.hash.Hashing
import com.google.common.io.Files
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.metadata.ModOrigin
import java.io.File
import java.io.IOException
import kotlin.jvm.optionals.getOrNull

object Hash {
    fun getSha512Hash(modId: String): String? {
        FabricLoader.getInstance().getModContainer(modId).getOrNull()?.let { container ->
            containerToFile(container)?.let { file ->
                if (file.isFile) return hashFile(file)
            }
        }

        return null
    }

    fun getSha512Hashes(modsIds: List<String>): List<String> = mutableListOf<String>().apply {
        FabricLoader.getInstance().allMods.filter { modsIds.contains(it.metadata.id) }.forEach { container ->
            containerToFile(container)?.let { file ->
                if (file.isFile) hashFile(file)?.let {
                    add(it)
                }
            }
        }
    }

    fun getSha512Hashes(): List<String> = mutableListOf<String>().apply {
        FabricLoader.getInstance().allMods.forEach { container ->
            containerToFile(container)?.let { file ->
                if (file.isFile) hashFile(file)?.let {
                    add(it)
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