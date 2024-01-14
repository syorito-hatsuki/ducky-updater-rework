package dev.syoritohatsuki.duckyupdaterrework.api.body

import kotlinx.serialization.Serializable
import net.minecraft.SharedConstants

@Serializable
data class LatestVersionHash(
    val loaders: List<String> = listOf("fabric"),
    val gameVersion: List<String> = listOf(SharedConstants.getGameVersion().name)
)
