package dev.syoritohatsuki.duckyupdater.util

import com.google.common.hash.Hashing
import com.google.common.io.Files
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.metadata.ModOrigin
import java.io.IOException
import kotlin.jvm.optionals.getOrNull

typealias VersionDiff = Triple<String, String, String>

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

fun diff(oldVersion: String, newVersion: String): VersionDiff? = if (oldVersion == newVersion) null else {
    val commonPrefix = oldVersion.commonPrefixWith(newVersion)
    VersionDiff(oldVersion.removePrefix(commonPrefix), newVersion.removePrefix(commonPrefix), commonPrefix)
}