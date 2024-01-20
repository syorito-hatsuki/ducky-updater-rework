package dev.syoritohatsuki.duckyupdaterrework.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Version(
    val name: String,
    val changelog: String,
    @SerialName("date_published")
    val datePublished: String,
    val featured: Boolean,
    val files: List<File>,
    @SerialName("version_number")
    val versionNumber: String
) {
    @Serializable
    data class File(
        val url: String
    )
}