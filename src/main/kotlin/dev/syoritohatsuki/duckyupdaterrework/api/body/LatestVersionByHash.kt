package dev.syoritohatsuki.duckyupdaterrework.api.body

import kotlinx.serialization.Serializable
import net.minecraft.SharedConstants

@Serializable
data class LatestVersionByHash(
    val hashes: List<String> = listOf(),
    val algorithm: String = "sha512",
    val loaders: List<String> = listOf("fabric"),
    val gameVersion: List<String> = listOf(SharedConstants.getGameVersion().name)
)
