package dev.syoritohatsuki.duckyupdaterrework.core.config

import dev.syoritohatsuki.duckyupdaterrework.core.util.Downloader
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val ignoreSide: Boolean = false,
    val downloadMode: Downloader.Mode = Downloader.Mode.PARALLEL,
    val fileAction: Downloader.FileAction = Downloader.FileAction.ARCHIVE
)
