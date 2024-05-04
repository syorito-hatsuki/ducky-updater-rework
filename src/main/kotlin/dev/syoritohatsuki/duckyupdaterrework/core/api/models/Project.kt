package dev.syoritohatsuki.duckyupdaterrework.core.api.models

import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val title: String,
    val id: String
)
