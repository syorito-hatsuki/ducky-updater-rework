package dev.syoritohatsuki.duckyupdaterrework.core.command

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork
import dev.syoritohatsuki.duckyupdaterrework.core.config.ConfigManager
import dev.syoritohatsuki.duckyupdaterrework.core.config.ConfigManager.write
import dev.syoritohatsuki.duckyupdaterrework.core.dsl.bool
import dev.syoritohatsuki.duckyupdaterrework.core.dsl.greedyString
import dev.syoritohatsuki.duckyupdaterrework.core.dsl.literal
import dev.syoritohatsuki.duckyupdaterrework.core.dsl.word
import dev.syoritohatsuki.duckyupdaterrework.core.lang.TaskLockedException
import dev.syoritohatsuki.duckyupdaterrework.core.util.Downloader
import dev.syoritohatsuki.duckyupdaterrework.core.util.FileActions
import dev.syoritohatsuki.duckyupdaterrework.core.util.TaskManager
import dev.syoritohatsuki.duckyupdaterrework.core.util.sendMessage
import dev.syoritohatsuki.duckyupdaterrework.server.message.BRIGHT_CYAN
import dev.syoritohatsuki.duckyupdaterrework.server.message.BRIGHT_GREEN
import dev.syoritohatsuki.duckyupdaterrework.server.message.BRIGHT_RED
import dev.syoritohatsuki.duckyupdaterrework.server.message.RESET
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.command.CommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting

fun LiteralArgumentBuilder<out CommandSource>.commands() {

    /*   Config commands :)   */
    literal("config") {
        literal("download-mode") {
            Downloader.Mode.values().forEach { mode ->
                literal(mode.name) {
                    executes {
                        ConfigManager.read().copy(downloadMode = mode).write()
                        it.source.sendMessage(Text.literal("Download mode: ").apply {
                            append(
                                when (mode) {
                                    Downloader.Mode.SEQUENTIALLY -> Text.literal("Sequentially")
                                        .formatted(Formatting.AQUA)

                                    Downloader.Mode.PARALLEL -> Text.literal("Parallel").formatted(Formatting.AQUA)
                                }
                            )
                        })
                        DuckyUpdaterReWork.logger.info(
                            "Download mode: " + when (mode) {
                                Downloader.Mode.SEQUENTIALLY -> "${BRIGHT_CYAN}Sequentially$RESET"
                                Downloader.Mode.PARALLEL -> "${BRIGHT_CYAN}Parallel$RESET"
                            }
                        )
                        1
                    }
                }
            }
        }
        literal("file-action") {
            FileActions.FileAction.values().forEach { action ->
                literal(action.name) {
                    executes {
                        ConfigManager.read().copy(fileAction = action).write()
                        it.source.sendMessage(Text.literal("File Action after download: ").apply {
                            append(
                                when (action) {
                                    FileActions.FileAction.DELETE -> Text.literal("Delete").formatted(Formatting.AQUA)
                                    FileActions.FileAction.DISABLE -> Text.literal("Disable").formatted(Formatting.AQUA)
                                    FileActions.FileAction.ARCHIVE -> Text.literal("Archive").formatted(Formatting.AQUA)
                                }
                            )
                        })
                        DuckyUpdaterReWork.logger.info(
                            "File Action after download: " + when (action) {
                                FileActions.FileAction.DELETE -> "${BRIGHT_CYAN}Delete$RESET"
                                FileActions.FileAction.DISABLE -> "${BRIGHT_CYAN}Disable$RESET"
                                FileActions.FileAction.ARCHIVE -> "${BRIGHT_CYAN}Archive$RESET"
                            }
                        )
                        1
                    }
                }
            }
        }
        literal("check-update-on-boot") {
            bool("check") {
                executes {
                    val check = BoolArgumentType.getBool(it, "check")
                    ConfigManager.read().copy(checkUpdatesOnBoot = check).write()
                    it.source.sendMessage(Text.literal("Updated checking on boot: ").apply {
                        append(
                            when (check) {
                                true -> Text.literal("Enabled").formatted(Formatting.GREEN)
                                false -> Text.literal("Disabled").formatted(Formatting.RED)
                            }
                        )
                    })
                    DuckyUpdaterReWork.logger.info(
                        "Updated checking on boot: " + when (check) {
                            true -> "${BRIGHT_GREEN}Enabled$RESET"
                            false -> "${BRIGHT_RED}Disabled$RESET"
                        }
                    )
                    1
                }
            }
        }
    }

    literal("clear-cache") {
        executes {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    TaskManager.clearCache(it)
                }
            } catch (e: TaskLockedException) {
                it.source.sendMessage(e.getMinecraftText())
                DuckyUpdaterReWork.logger.error(e.message)
            }
            0
        }
    }

    /*   Update checking :D  */
    literal("check") {
        executes {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    TaskManager.getAvailableUpdatesFromDB(it)
                }
            } catch (e: TaskLockedException) {
                it.source.sendMessage(e.getMinecraftText())
                DuckyUpdaterReWork.logger.error(e.message)
            }
            0
        }
    }

    /*   Ignoring -_-   */
    literal("ignore") {
        literal("list") {
            executes {
                try {
                    TaskManager.listIgnoredMods(it)
                } catch (e: TaskLockedException) {
                    it.source.sendMessage(e.getMinecraftText())
                    DuckyUpdaterReWork.logger.error(e.message)
                }
                0
            }
        }
        literal("by") {
            literal("mod-id") {
                word("modId") {
                    bool("ignore") {
                        executes {
                            try {
                                TaskManager.addToIgnoreByModId(
                                    it,
                                    StringArgumentType.getString(it, "modId"),
                                    BoolArgumentType.getBool(it, "ignore")
                                )
                            } catch (e: TaskLockedException) {
                                it.source.sendMessage(e.getMinecraftText())
                                DuckyUpdaterReWork.logger.error(e.message)
                            }
                            0
                        }
                    }
                }
            }
            literal("project-id") {
                word("projectId") {
                    bool("ignore") {
                        executes {
                            try {
                                TaskManager.addToIgnoreByProjectId(
                                    it,
                                    StringArgumentType.getString(it, "projectId"),
                                    BoolArgumentType.getBool(it, "ignore")
                                )
                            } catch (e: TaskLockedException) {
                                it.source.sendMessage(e.getMinecraftText())
                                DuckyUpdaterReWork.logger.error(e.message)
                            }
                            0
                        }
                    }
                }
            }
        }
    }

    /*   Updating :3   */
    literal("update") {
        literal("by") {
            literal("mod-ids") {
                greedyString("modIds") {
                    executes {
                        try {
                            TaskManager.updateSpecificModsByModIds(
                                it, *StringArgumentType.getString(it, "modsIds").split(" ").toTypedArray()
                            )
                        } catch (e: TaskLockedException) {
                            it.source.sendMessage(e.getMinecraftText())
                            DuckyUpdaterReWork.logger.error(e.message)
                        }
                        0
                    }
                }
            }
            literal("project-ids") {
                greedyString("projectIds") {
                    executes {
                        try {
                            TaskManager.updateSpecificModsByProjectIds(
                                it, *StringArgumentType.getString(it, "projectIds").split(" ").toTypedArray()
                            )
                        } catch (e: TaskLockedException) {
                            it.source.sendMessage(e.getMinecraftText())
                            DuckyUpdaterReWork.logger.error(e.message)
                        }
                        0
                    }
                }
            }
        }
        literal("all") {
            executes {
                try {
                    TaskManager.updateAllMods(it)
                } catch (e: TaskLockedException) {
                    it.source.sendMessage(e.getMinecraftText())
                    DuckyUpdaterReWork.logger.error(e.message)
                }
                0
            }
        }
    }
}
