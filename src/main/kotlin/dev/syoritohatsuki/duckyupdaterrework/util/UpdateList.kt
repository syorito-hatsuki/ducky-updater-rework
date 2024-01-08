package dev.syoritohatsuki.duckyupdaterrework.util

import dev.syoritohatsuki.duckyupdaterrework.dto.UpdateVersions
import java.nio.file.Path

object UpdateList {
    private val _updateVersions: MutableSet<UpdateVersions> = hashSetOf()
    private val _alreadyUpdatedVersions: MutableSet<UpdateVersions> = hashSetOf()

    fun getUpdates(): MutableSet<UpdateVersions> = mutableSetOf<UpdateVersions>().apply {
        addAll(_updateVersions.filter {
            !_alreadyUpdatedVersions.contains(it)
        }.filter {
            !ConfigManager.getIgnored().any { item ->
                item.key == it.modId && item.value == (it.versions.matched + it.versions.newVersion)
            }
        })
    }

    fun markAsUpdated(updateVersions: UpdateVersions) {
        _alreadyUpdatedVersions.add(updateVersions)
    }

    fun addAvailableUpdates(
        id: String,
        name: String,
        path: Path,
        remoteFileName: String,
        url: String,
        changeLog: String,
        versions: UpdateVersions.Versions
    ) {
        _updateVersions.add(UpdateVersions(id, name, path, remoteFileName, url, changeLog, versions))
    }
}
