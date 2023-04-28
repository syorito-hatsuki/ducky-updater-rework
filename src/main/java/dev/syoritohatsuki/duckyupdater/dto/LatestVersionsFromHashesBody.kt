package dev.syoritohatsuki.duckyupdater.dto

import com.google.gson.annotations.SerializedName
import net.minecraft.SharedConstants
import java.util.Set

data class LatestVersionsFromHashesBody(
    var hashes: Collection<String>,
    var algorithm: String = "sha512",
    var loaders: Collection<String> = setOf("fabric"),
    @SerializedName("game_versions")
    var gameVersions: Collection<String> = mutableSetOf(SharedConstants.getGameVersion().name)
)