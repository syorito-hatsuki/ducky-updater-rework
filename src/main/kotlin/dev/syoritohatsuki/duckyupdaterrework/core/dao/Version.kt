package dev.syoritohatsuki.duckyupdaterrework.core.dao

data class Version(
    val currentVersion: String? = "",
    val newVersion: String,
) {
    val matched: String
        get() = currentVersion?.commonPrefixWith(newVersion) ?: ""
    val currentUnMatch: String
        get() = currentVersion?.removePrefix(matched) ?: ""
    val newUnMatched: String
        get() = newVersion.removePrefix(matched)
}
