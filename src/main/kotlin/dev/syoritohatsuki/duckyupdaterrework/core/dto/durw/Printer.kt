package dev.syoritohatsuki.duckyupdaterrework.core.dto.durw

data class Printer(
    val projectId: String,
    val prefix: String,
    val currentExist: Boolean,
    val matchedVersion: String,
    val currentUnMatchVersion: String,
    val newUnMatchVersion: String,
)