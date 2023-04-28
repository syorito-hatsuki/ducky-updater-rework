package dev.syoritohatsuki.duckyupdater.util

import com.google.common.hash.Hashing
import com.google.common.io.Files
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.metadata.ModOrigin
import java.io.IOException
import kotlin.jvm.optionals.getOrNull

object Util {
    fun getSha512Hash(modContainer: ModContainer): String? {
        if (modContainer.containingMod.isEmpty && modContainer.origin.kind == ModOrigin.Kind.PATH) {
            modContainer.origin.paths.stream().filter {
                it.toString().lowercase().endsWith(".jar")
            }.findFirst().getOrNull()?.let {
                val file = it.toFile()
                if (file.isFile) try {
                    return Files.asByteSource(file).hash(Hashing.sha512()).toString()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    fun match(oldVersion: CharArray, newVersion: CharArray): String? {
        if (oldVersion.contentEquals(newVersion)) return null
        val builder = StringBuilder()
        var index = 0
        runCatching {
            while (oldVersion[index] == newVersion[index]) {
                builder.append(oldVersion[index])
                index++
            }
        }
        return builder.toString()
    }
}