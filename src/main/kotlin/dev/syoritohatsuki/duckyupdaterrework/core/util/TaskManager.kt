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
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component

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
    fun listIgnoredMods(context: CommandContext<out SharedSuggestionProvider>, loader: Loader) {
        throwIfLocked("Fetching list of ignored projects")

        context.source.sendMessage(Component.literal("Next projects are ignored"))
        logger.info("Next projects are ignored")

        Database.getListOfIgnoredProjects(loader).onEach {
            context.source.sendMessage(
                Component.literal(" - ${it.key} ").append(Component.literal("(${it.value})").withStyle(ChatFormatting.DARK_GRAY))
            )
            logger.info(" - ${it.key} $GRAY(${it.value})$RESET")
        }

        unlock()
    }

    @Throws(TaskLockedException::class)
    suspend fun updateProjectsDB(context: CommandContext<out SharedSuggestionProvider>? = null, loader: Loader) {
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
    fun clearCache(context: CommandContext<out SharedSuggestionProvider>? = null) {
        throwIfLocked("Clearing cache")
        context?.source?.sendMessageWithLog("Deleting cache...")
        Database.drop()
        context?.source?.sendMessageWithLog("Cache deleted!")
        unlock()
    }

    @Throws(TaskLockedException::class)
    suspend fun getAvailableUpdatesFromDB(context: CommandContext<out SharedSuggestionProvider>, loader: Loader) {
        updateProjectsDB(context, loader)

        throwIfLocked("Getting updates from DB for [${loader.name.replaceFirstChar { it.uppercaseChar() }}]")

        val modsIds = Database.getOutdatedProjectIds(loader)
        val additionalInfos = Database.getAdditionalInfoByProjectIds(modsIds, loader)

        var updatesCount: Int

        buildModsTree(modsIds, additionalInfos).onEach { printer ->
            context.source.sendMessage(updateAvailable(context.source, printer, additionalInfos, loader))
            if (context.source is CommandSourceStack) logger.updateAvailable(printer)
        }.also {
            updatesCount = it.map { printer -> printer.projectId }.toHashSet().size
        }

        if (updatesCount > 0) {
            context.source.sendMessage(Component.empty(), updatesCount(updatesCount, loader))
            logger.updatesCount(updatesCount)
        } else context.source.sendMessageWithLog("No updates available")

        unlock()
    }

    @Throws(TaskLockedException::class)
    fun updateAllMods(context: CommandContext<out SharedSuggestionProvider>, loader: Loader) {
        throwIfLocked("Updating all mods")
        Downloader.download(context, Database.getAllDownloadingData(loader), loader)
        unlock()
    }

    @Throws(TaskLockedException::class)
    fun updateSpecificModsByModIds(context: CommandContext<out SharedSuggestionProvider>, vararg mods: String) {
        throwIfLocked("Updating specific mods (${mods.joinToString()})")
        context.source.sendMessage(Component.literal("Getting date from DB...").withStyle(ChatFormatting.GREEN))
        logger.info("${BRIGHT_GREEN}Getting date from DB...$RESET")
        Downloader.download(context, Database.getDownloadingDataByModIds(mods.toSet()), Loader.FABRIC)
        unlock()
    }

    @Throws(TaskLockedException::class)
    fun updateSpecificModsByProjectIds(
        context: CommandContext<out SharedSuggestionProvider>, loader: Loader, vararg projectIds: String
    ) {
        throwIfLocked("Updating specific mods by ids (${projectIds.joinToString()})")
        context.source.sendMessage(Component.literal("Getting date from DB...").withStyle(ChatFormatting.GREEN))
        logger.info("${BRIGHT_GREEN}Getting date from DB...$RESET")
        Downloader.download(context, Database.getDownloadingDataByProjectIds(projectIds.toSet(), loader), loader)
        unlock()
    }

    @Throws(TaskLockedException::class)
    fun addToIgnoreByProjectId(
        context: CommandContext<out SharedSuggestionProvider>, projectId: String, status: Boolean, loader: Loader
    ) {
        throwIfLocked("Adding $projectId to ignore")

        val result = Database.setIgnore(projectId = projectId, status = status, loader = loader)

        if (result == -1) {
            context.source.sendMessage(Component.literal("Unexpected error...").withStyle(ChatFormatting.RED))
            logger.info("${BRIGHT_RED}Unexpected error...$RESET")
            unlock()
            return
        }

        if (result == -2) {
            context.source.sendMessage(Component.literal("Project not exist").withStyle(ChatFormatting.GRAY))
            logger.info("${BRIGHT_GRAY}Project not exist$RESET")
            unlock()
            return
        }

        if (result == -3) {
            context.source.sendMessage(Component.literal("Project already marked by $status").withStyle(ChatFormatting.GRAY))
            logger.info("${BRIGHT_GRAY}Project already marked by $status$RESET")
            unlock()
            return
        }

        when (status) {
            true -> {
                context.source.sendMessage(
                    Component.empty().append(Component.literal("Project "))
                        .append(Component.literal(projectId).withStyle(ChatFormatting.GREEN))
                        .append(Component.literal(" added to ignore list"))
                )
                logger.info("Project $projectId ${BRIGHT_GREEN}added$RESET to ignore list")
            }

            false -> {
                context.source.sendMessage(
                    Component.empty().append(Component.literal("Project "))
                        .append(Component.literal(projectId).withStyle(ChatFormatting.RED))
                        .append(Component.literal(" removed from ignore list"))
                )
                logger.info("Project $projectId ${BRIGHT_RED}removed$RESET from ignore list")
            }
        }
        unlock()
    }

    @Throws(TaskLockedException::class)
    fun addToIgnoreByModId(context: CommandContext<out SharedSuggestionProvider>, modId: String, status: Boolean) {
        throwIfLocked("Adding $modId to ignore")

        val result = Database.setIgnore(modId = modId, status = status, loader = Loader.FABRIC)

        if (result == -1) {
            context.source.sendMessage(Component.literal("Unexpected error...").withStyle(ChatFormatting.RED))
            logger.info("${BRIGHT_RED}Unexpected error...$RESET")
            unlock()
            return
        }

        if (result == -2) {
            context.source.sendMessage(Component.literal("Mod not exist").withStyle(ChatFormatting.GRAY))
            logger.info("${BRIGHT_GRAY}Mod not exist$RESET")
            unlock()
            return
        }

        if (result == -3) {
            context.source.sendMessage(Component.literal("$modId already marked by $status").withStyle(ChatFormatting.GRAY))
            logger.info("${BRIGHT_GRAY}$modId already marked by $status$RESET")
            unlock()
            return
        }

        if (status) {
            context.source.sendMessage(
                Component.empty().append(Component.literal("Project ")).append(Component.literal(modId).withStyle(ChatFormatting.GREEN))
                    .append(Component.literal(" added to ignore list"))
            )
            logger.info("Project $modId ${BRIGHT_GREEN}added$RESET to ignore list")
        } else {
            context.source.sendMessage(
                Component.empty().append(Component.literal("Project ")).append(Component.literal(modId).withStyle(ChatFormatting.RED))
                    .append(Component.literal(" removed from ignore list"))
            )
            logger.info("Project $modId ${BRIGHT_RED}removed$RESET from ignore list")
        }

        unlock()
    }
}
