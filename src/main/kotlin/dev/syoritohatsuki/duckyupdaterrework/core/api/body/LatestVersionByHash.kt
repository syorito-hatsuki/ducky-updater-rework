package dev.syoritohatsuki.duckyupdaterrework.core.api.body

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.SharedConstants

@Serializable
data class LatestVersionByHash(
    val hashes: List<String> = listOf(),
    val algorithm: String = "sha512",
    val loaders: List<String> = listOf("fabric"),
    @SerialName("game_versions")
    val gameVersion: List<String> = listOf(SharedConstants.getGameVersion().name)
)
