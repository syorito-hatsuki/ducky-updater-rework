package dev.syoritohatsuki.duckyupdater.dto

data class VersionDiff(
    val oldVersion: String, val newVersion: String, val matched: String
)