package dev.syoritohatsuki.duckyupdaterrework.core.util

// I didn't get a better idea to handle a process that works in diff threads...
object TaskManager {
    private var isLocked = false
    private var task = ""

    @Throws(IllegalStateException::class)
    private fun throwIfLocked(newTask: String) = when {
        isLocked -> throw IllegalStateException("Task Manager is locked by [$task]")
        else -> task = newTask
    }

    private fun unlock() {
        isLocked = false
        task = ""
    }

    fun updateProjectsDB() {
        throwIfLocked("Updating Projects in DB")
        // Quake Quake Quake...
        unlock()
    }

    fun updateAllMods() {
        throwIfLocked("Updating all mods")
        // Quake Quake Quake...
        unlock()
    }

    fun updateSpecificMods(vararg mods: String) {
        throwIfLocked("Updating specific mods (${mods.joinToString()})")
        // Quake Quake Quake...
        unlock()
    }

    // Quack... QUACK!!!!
}

