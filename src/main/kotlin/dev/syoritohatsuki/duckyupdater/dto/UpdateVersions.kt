package dev.syoritohatsuki.duckyupdater.dto

import java.nio.file.Path

data class UpdateVersions(
    val modId: String,
    val modName: String,
    val modPath: Path,
    val remoteFileName: String,
    val url: String,
    val changeLog: String,
    val versions: Versions
) {
    data class Versions(
        val oldVersion: String,
        val newVersion: String,
        val matched: String
    )
}
