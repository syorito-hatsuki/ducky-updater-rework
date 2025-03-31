package dev.syoritohatsuki.duckyupdaterrework.core.util

import com.mojang.brigadier.context.CommandContext
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork.logger
import dev.syoritohatsuki.duckyupdaterrework.client.message.updateAvailable
import dev.syoritohatsuki.duckyupdaterrework.client.message.updatesCount
import dev.syoritohatsuki.duckyupdaterrework.core.DuckyUpdaterApi
import dev.syoritohatsuki.duckyupdaterrework.core.api.models.Loader
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
    fun listIgnoredMods(context: CommandContext<out CommandSource>, loader: Loader) {
        throwIfLocked("Fetching list of ignored projects")

        context.source.sendMessage(Text.literal("Next projects are ignored"))
        logger.info("Next projects are ignored")

        Database.getListOfIgnoredProjects(loader).onEach {
            context.source.sendMessage(
                Text.literal(" - ${it.key} ").append(Text.literal("(${it.value})").formatted(Formatting.DARK_GRAY))
            )
            logger.info(" - ${it.key} $GRAY(${it.value})$RESET")
        }

        unlock()
    }

    @Throws(TaskLockedException::class)
    suspend fun updateProjectsDB(context: CommandContext<out CommandSource>? = null, loader: Loader) {
        throwIfLocked("Updating ${loader.name} in DB")
        context?.source?.sendMessageWithLog("Fetching updates from Modrinth...")
        when (loader) {
            Loader.FABRIC -> DuckyUpdaterApi.checkMods()
            Loader.DATAPACK -> DuckyUpdaterApi.checkDatapacks()
        }
        context?.source?.sendMessageWithLog("Fetch success!")
        unlock()
    }

    @Throws(TaskLockedException::class)
    fun clearCache(context: CommandContext<out CommandSource>? = null) {
        throwIfLocked("Clearing cache")
        context?.source?.sendMessageWithLog("Deleting cache...")
        Database.drop()
        context?.source?.sendMessageWithLog("Cache deleted!")
        unlock()
    }

    @Throws(TaskLockedException::class)
    suspend fun getAvailableUpdatesFromDB(context: CommandContext<out CommandSource>, loader: Loader) {
        updateProjectsDB(context, loader)

        throwIfLocked("Getting updates from DB for [${loader.name.replaceFirstChar { it.uppercaseChar() }}]")

        val modsIds = Database.getOutdatedProjectIds(loader)
        val additionalInfos = Database.getAdditionalInfoByProjectIds(modsIds, loader)

        var updatesCount: Int

        buildModsTree(modsIds, additionalInfos).onEach { printer ->
            context.source.sendMessage(updateAvailable(context.source, printer, additionalInfos, loader))
            if (context.source is ServerCommandSource) logger.updateAvailable(printer)
        }.also {
            updatesCount = it.map { printer -> printer.projectId }.toHashSet().size
        }

        if (updatesCount > 0) {
            context.source.sendMessage(Text.empty(), updatesCount(updatesCount, loader))
            logger.updatesCount(updatesCount)
        } else context.source.sendMessageWithLog("No updates available")

        unlock()
    }

    @Throws(TaskLockedException::class)
    fun updateAllMods(context: CommandContext<out CommandSource>, loader: Loader) {
        throwIfLocked("Updating all mods")
        Downloader.download(context, Database.getAllDownloadingData(loader), loader)
        unlock()
    }

    @Throws(TaskLockedException::class)
    fun updateSpecificModsByModIds(context: CommandContext<out CommandSource>, vararg mods: String) {
        throwIfLocked("Updating specific mods (${mods.joinToString()})")
        context.source.sendMessage(Text.literal("Getting date from DB...").formatted(Formatting.GREEN))
        logger.info("${BRIGHT_GREEN}Getting date from DB...$RESET")
        Downloader.download(context, Database.getDownloadingDataByModIds(mods.toSet()), Loader.FABRIC)
        unlock()
    }

    @Throws(TaskLockedException::class)
    fun updateSpecificModsByProjectIds(
        context: CommandContext<out CommandSource>, loader: Loader, vararg projectIds: String
    ) {
        throwIfLocked("Updating specific mods by ids (${projectIds.joinToString()})")
        context.source.sendMessage(Text.literal("Getting date from DB...").formatted(Formatting.GREEN))
        logger.info("${BRIGHT_GREEN}Getting date from DB...$RESET")
        Downloader.download(context, Database.getDownloadingDataByProjectIds(projectIds.toSet(), loader), loader)
        unlock()
    }

    @Throws(TaskLockedException::class)
    fun addToIgnoreByProjectId(
        context: CommandContext<out CommandSource>, projectId: String, status: Boolean, loader: Loader
    ) {
        throwIfLocked("Adding $projectId to ignore")

        val result = Database.setIgnore(projectId = projectId, status = status, loader = loader)

        if (result == -1) {
            context.source.sendMessage(Text.literal("Unexpected error...").formatted(Formatting.RED))
            logger.info("${BRIGHT_RED}Unexpected error...$RESET")
            unlock()
            return
        }

        if (result == -2) {
            context.source.sendMessage(Text.literal("Project not exist").formatted(Formatting.GRAY))
            logger.info("${BRIGHT_GRAY}Project not exist$RESET")
            unlock()
            return
        }

        if (result == -3) {
            context.source.sendMessage(Text.literal("Project already marked by $status").formatted(Formatting.GRAY))
            logger.info("${BRIGHT_GRAY}Project already marked by $status$RESET")
            unlock()
            return
        }

        when (status) {
            true -> {
                context.source.sendMessage(
                    Text.empty().append(Text.literal("Project "))
                        .append(Text.literal(projectId).formatted(Formatting.GREEN))
                        .append(Text.literal(" added to ignore list"))
                )
                logger.info("Project $projectId ${BRIGHT_GREEN}added$RESET to ignore list")
            }

            false -> {
                context.source.sendMessage(
                    Text.empty().append(Text.literal("Project "))
                        .append(Text.literal(projectId).formatted(Formatting.RED))
                        .append(Text.literal(" removed from ignore list"))
                )
                logger.info("Project $projectId ${BRIGHT_RED}removed$RESET from ignore list")
            }
        }
        unlock()
    }

    @Throws(TaskLockedException::class)
    fun addToIgnoreByModId(context: CommandContext<out CommandSource>, modId: String, status: Boolean) {
        throwIfLocked("Adding $modId to ignore")

        val result = Database.setIgnore(modId = modId, status = status, loader = Loader.FABRIC)

        if (result == -1) {
            context.source.sendMessage(Text.literal("Unexpected error...").formatted(Formatting.RED))
            logger.info("${BRIGHT_RED}Unexpected error...$RESET")
            unlock()
            return
        }

        if (result == -2) {
            context.source.sendMessage(Text.literal("Mod not exist").formatted(Formatting.GRAY))
            logger.info("${BRIGHT_GRAY}Mod not exist$RESET")
            unlock()
            return
        }

        if (result == -3) {
            context.source.sendMessage(Text.literal("$modId already marked by $status").formatted(Formatting.GRAY))
            logger.info("${BRIGHT_GRAY}$modId already marked by $status$RESET")
            unlock()
            return
        }

        if (status) {
            context.source.sendMessage(
                Text.empty().append(Text.literal("Project ")).append(Text.literal(modId).formatted(Formatting.GREEN))
                    .append(Text.literal(" added to ignore list"))
            )
            logger.info("Project $modId ${BRIGHT_GREEN}added$RESET to ignore list")
        } else {
            context.source.sendMessage(
                Text.empty().append(Text.literal("Project ")).append(Text.literal(modId).formatted(Formatting.RED))
                    .append(Text.literal(" removed from ignore list"))
            )
            logger.info("Project $modId ${BRIGHT_RED}removed$RESET from ignore list")
        }

        unlock()
    }
}
