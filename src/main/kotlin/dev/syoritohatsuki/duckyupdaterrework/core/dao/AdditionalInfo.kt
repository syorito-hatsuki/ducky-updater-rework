package dev.syoritohatsuki.duckyupdaterrework.core.dao

data class AdditionalInfo(
    val name: String,
    val changeLog: String,
    val url: String,
    val version: Version
)