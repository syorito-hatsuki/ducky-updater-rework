package dev.syoritohatsuki.duckyupdaterrework.util

import com.google.common.hash.Hashing
import com.google.common.io.Files
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.io.IOException
import kotlin.jvm.optionals.getOrNull

object Hash {
    fun getSha512Hash(modId: String): String? =
        FabricLoader.getInstance().getModContainer(modId).getOrNull()?.let { container ->
            container.origin.paths.firstOrNull { it.toString().lowercase().endsWith(".jar") }?.toFile()
                ?.takeIf(File::isFile)?.let { hashFile(it) }
        }

    fun getSha512Hashes(modsIds: List<String>): List<String> =
        FabricLoader.getInstance().allMods.filter { it.metadata.id in modsIds }.flatMap { container ->
            container.origin.paths.filter { it.toString().lowercase().endsWith(".jar") }
                .mapNotNull { it.toFile().takeIf(File::isFile) }
        }.mapNotNull { hashFile(it) }

    fun getSha512Hashes(): List<String> = FabricLoader.getInstance().allMods.flatMap { container ->
        container.origin.paths.filter {
            it.toString().lowercase().endsWith(".jar")
        }.mapNotNull { it.toFile().takeIf(File::isFile) }
    }.mapNotNull { hashFile(it) }

    private fun hashFile(file: File): String? = try {
        Files.asByteSource(file).hash(Hashing.sha512()).toString()
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}