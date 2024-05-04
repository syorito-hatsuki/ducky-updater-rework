package dev.syoritohatsuki.duckyupdaterrework.core.dto.modrinth

data class AdditionalInfo(
    val name: String,
    val changeLog: String,
    val url: String,
    val version: Version
)