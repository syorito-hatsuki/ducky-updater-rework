package dev.syoritohatsuki.duckyupdaterrework.core.util

import com.mojang.brigadier.context.CommandContext
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork.logger
import dev.syoritohatsuki.duckyupdaterrework.client.message.updateAvailable
import dev.syoritohatsuki.duckyupdaterrework.client.message.updatesCount
import dev.syoritohatsuki.duckyupdaterrework.core.DuckyUpdaterApi
import dev.syoritohatsuki.duckyupdaterrework.core.lang.TaskLockedException
import dev.syoritohatsuki.duckyupdaterrework.core.storage.Database
import dev.syoritohatsuki.duckyupdaterrework.server.message.*
import net.minecraft.command.CommandSource
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting

// I didn't get a better idea to handle a process that works in diff threads...
object TaskManager {
    private var isLocked = false
    private var task = ""

    @Throws(TaskLockedException::class)
    private fun throwIfLocked(newTask: String) = when {
        isLocked -> throw TaskLockedException(task)
        else -> {
            isLocked = true
            task = newTask
        }
    }

    private fun unlock() {
        isLocked = false
        task = ""
    }

    @Throws(TaskLockedException::class)
    suspend fun updateProjectsDB(context: CommandContext<out CommandSource>? = null) {
        throwIfLocked("Updating Projects in DB")
        context?.source?.sendMessageWithLog("Fetching updates from Modrinth...")
        DuckyUpdaterApi.checkForUpdates()
        context?.source?.sendMessageWithLog("Fetch success!")
        unlock()
    }

    @Throws(TaskLockedException::class)
    suspend fun getAvailableUpdatesFromDB(context: CommandContext<out CommandSource>) {
        updateProjectsDB(context)

        throwIfLocked("Getting updates from DB")

        val modsIds = Database.getOutdatedProjectIds()
        val additionalInfos = Database.getAdditionalInfoByProjectIds(modsIds)

        var updatesCount: Int

        buildModsTree(modsIds, additionalInfos).onEach { printer ->
            context.source.sendMessage(updateAvailable(context.source, printer, additionalInfos))
            if (context.source is ServerCommandSource) logger.updateAvailable(printer)
        }.also {
            updatesCount = it.map { printer -> printer.projectId }.toHashSet().size
        }

        if (updatesCount > 0) {
            context.source.sendMessage(Text.empty(), updatesCount(updatesCount))
            logger.updatesCount(updatesCount)
        } else context.source.sendMessageWithLog("No updates available")

        unlock()
    }

    @Throws(TaskLockedException::class)
    fun updateAllMods(context: CommandContext<out CommandSource>) {
        throwIfLocked("Updating all mods")
        Downloader.download(context, Database.getAllDownloadingData())
        unlock()
    }

    @Throws(TaskLockedException::class)
    fun updateSpecificMods(context: CommandContext<out CommandSource>, vararg mods: String) {
        throwIfLocked("Updating specific mods (${mods.joinToString()})")
        context.source.sendMessage(Text.literal("Getting date from DB...").formatted(Formatting.GREEN))
        logger.info("${BRIGHT_GREEN}Getting date from DB...$RESET")
        Downloader.download(context, Database.getDownloadingDataByModIds(mods.toSet()))
        unlock()
    }
}