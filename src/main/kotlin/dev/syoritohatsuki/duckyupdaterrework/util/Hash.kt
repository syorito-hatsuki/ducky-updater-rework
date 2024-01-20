package dev.syoritohatsuki.duckyupdaterrework.util

import com.google.common.hash.Hashing
import com.google.common.io.Files
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModOrigin
import java.io.IOException
import kotlin.jvm.optionals.getOrNull

object Hash {
    fun getSha512Hash(modId: String): String? {
        FabricLoader.getInstance().getModContainer(modId).getOrNull()?.let { container ->
            if (container.containingMod.isEmpty && container.origin.kind == ModOrigin.Kind.PATH) container.origin.paths.stream()
                .filter { path ->
                    path.toString().lowercase().endsWith(".jar")
                }.findFirst().getOrNull()?.toFile()?.let {
                    if (it.isFile) try {
                        return Files.asByteSource(it).hash(Hashing.sha512()).toString()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
        }

        return null
    }

    fun getSha512Hashes(): List<String> = mutableListOf<String>().apply {
        FabricLoader.getInstance().allMods.forEach { container ->
            if (container.containingMod.isEmpty && container.origin.kind == ModOrigin.Kind.PATH) container.origin.paths.stream()
                .filter { path ->
                    path.toString().lowercase().endsWith(".jar")
                }.findFirst().getOrNull()?.toFile()?.let {
                    if (it.isFile) try {
                        add(Files.asByteSource(it).hash(Hashing.sha512()).toString())
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
        }
    }
}