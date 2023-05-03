package dev.syoritohatsuki.duckyupdater.dto

data class UpdateVersions(
    val modId: String,
    val modName: String,
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
