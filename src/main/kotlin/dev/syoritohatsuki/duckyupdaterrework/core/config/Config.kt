package dev.syoritohatsuki.duckyupdaterrework.core.config

import dev.syoritohatsuki.duckyupdaterrework.core.util.Downloader
import dev.syoritohatsuki.duckyupdaterrework.core.util.FileActions
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val checkUpdatesOnBoot: Boolean = true,
    val downloadMode: Downloader.Mode = Downloader.Mode.PARALLEL,
    val fileAction: FileActions.FileAction = FileActions.FileAction.ARCHIVE
)
