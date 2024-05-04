package dev.syoritohatsuki.duckyupdaterrework.core.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Version(
    val name: String,
    val changelog: String,
    @SerialName("date_published") val datePublished: String,
    val dependencies: List<Dependency> = emptyList(),
    val featured: Boolean,
    val files: List<File>,
    @SerialName("project_id") val projectId: String,
    @SerialName("version_number") val versionNumber: String
) {
    @Serializable
    data class Dependency(
        @SerialName("version_id") val versionId: String? = "",
        @SerialName("project_id") val projectId: String? = "",
        @SerialName("file_name") val fileName: String? = "",
        @SerialName("dependency_type") val dependencyType: String? = ""
    )

    @Serializable
    data class File(
        val url: String,
        val primary: Boolean,
        val hashes: Hashes
    ) {
        @Serializable
        data class Hashes(
            val sha512: String
        )
    }
}