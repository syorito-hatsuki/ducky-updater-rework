package dev.syoritohatsuki.duckyupdaterrework.core.util

import com.mojang.brigadier.context.CommandContext
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork
import dev.syoritohatsuki.duckyupdaterrework.core.config.ConfigManager
import dev.syoritohatsuki.duckyupdaterrework.core.storage.Database
import dev.syoritohatsuki.duckyupdaterrework.core.storage.Filename
import dev.syoritohatsuki.duckyupdaterrework.core.storage.ProjectId
import dev.syoritohatsuki.duckyupdaterrework.core.storage.Url
import dev.syoritohatsuki.duckyupdaterrework.core.util.Downloader.Mode.*
import dev.syoritohatsuki.duckyupdaterrework.server.message.BRIGHT_GREEN
import dev.syoritohatsuki.duckyupdaterrework.server.message.BRIGHT_RED
import dev.syoritohatsuki.duckyupdaterrework.server.message.RESET
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.command.CommandSource
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.io.*
import java.net.URLDecoder

object Downloader {
    private val mutex = Mutex()
    private val client = HttpClient(CIO) {
        expectSuccess = true
    }

    private var state: State = State.IDLE

    private enum class State {
        IDLE, RUNNING
    }

    enum class Mode {
        SEQUENTIALLY, PARALLEL
    }

    data class Fail(
        val filename: String, val url: String, val reason: String
    )

    @Throws(IllegalStateException::class)
    private fun throwIfLocked() = when {
        state == State.IDLE -> state = State.RUNNING
        else -> throw IllegalStateException("Another download is already in progress.")
    }

    @Throws(IllegalStateException::class)
    fun download(
        context: CommandContext<out CommandSource>,
        mapOfUrlAndOldFile: Map<ProjectId, Pair<Url, Filename>>,
    ) {
        throwIfLocked()

        if (mapOfUrlAndOldFile.isEmpty()) {
            context.source.sendMessageWithLog("No updates available")
            return
        }

        val failed = mutableSetOf<Fail>()
        CoroutineScope(Dispatchers.IO).launch {
            val mode = ConfigManager.read().downloadMode

            context.source.sendMessage(Text.literal("Download starting...").formatted(Formatting.GREEN))
            DuckyUpdaterReWork.logger.info("${BRIGHT_GREEN}Download starting...$RESET")
            val directoriesByProjectId = Database.getDirectoriesByProjectId(mapOfUrlAndOldFile.keys)

            when (mode) {
                SEQUENTIALLY -> mapOfUrlAndOldFile.forEach { (projectId, data) ->
                    val (url, oldFile) = data
                    val filename = URLDecoder.decode(url.substringAfterLast('/'), Charsets.UTF_8)
                    try {
                        if (downloadFile(url, filename, directoriesByProjectId[projectId])) {
                            context.source.sendMessageWithLog("Downloaded: $filename")
                            Database.markProjectAsUpdated(projectId)
                            if (filename == oldFile) return@forEach
                            FileActions.prepareAction(oldFile, directoriesByProjectId[projectId])
                        } else {
                            context.source.sendMessageWithLog("Phantom error with: $filename in $mode mode")
                        }
                    } catch (e: Exception) {
                        DuckyUpdaterReWork.logger.error(e)
                        failed.add(Fail(filename, url, e.stackTraceToString()))
                    }
                }

                PARALLEL -> mapOfUrlAndOldFile.map { (projectId, data) ->
                    CoroutineScope(Dispatchers.IO).async {
                        val (url, oldFile) = data
                        val filename = URLDecoder.decode(url.substringAfterLast('/'), Charsets.UTF_8)
                        try {
                            if (downloadFile(url, filename, directoriesByProjectId[projectId])) {
                                context.source.sendMessageWithLog("Downloaded: $filename")
                                Database.markProjectAsUpdated(projectId)
                                if (filename == oldFile) return@async
                                mutex.withLock {
                                    FileActions.prepareAction(oldFile, directoriesByProjectId[projectId])
                                }
                            } else {
                                context.source.sendMessageWithLog("Phantom error with: $filename in $mode mode")
                            }
                        } catch (e: RuntimeException) {
                            DuckyUpdaterReWork.logger.error(e)
                            failed.add(Fail(filename, url, e.stackTraceToString()))
                        }
                    }
                }.awaitAll()
            }

            if (failed.size > 0) {
                context.source.sendMessage(
                    Text.literal("Failed to download next mods: ").formatted(Formatting.RED),
                    *failed.map {
                        Text.literal("- ${it.filename}").styled { style ->
                            style.withColor(Formatting.RED)
                                .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(it.reason)))
                                .withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, it.url))
                        }
                    }.toTypedArray()
                )

                DuckyUpdaterReWork.logger.warn("${BRIGHT_RED}Failed to download next mods:$RESET")
                failed.map {
                    DuckyUpdaterReWork.logger.warn("$BRIGHT_RED - ${it.filename} | ${it.reason}$RESET")
                }
            } else {
                context.source.sendMessage(Text.literal("Download completed").formatted(Formatting.GREEN))
                context.source.sendMessage(
                    Text.literal(
                        "${
                            ConfigManager.read().fileAction.name.lowercase().replaceFirstChar { it.uppercaseChar() }
                        } will start after restart ${FabricLoader.getInstance().environmentType.name.lowercase()}"
                    ).formatted(Formatting.RED)
                )
                DuckyUpdaterReWork.logger.info("${BRIGHT_GREEN}Download completed$RESET")
                DuckyUpdaterReWork.logger.info(
                    "${BRIGHT_RED}${
                        ConfigManager.read().fileAction.name.lowercase().replaceFirstChar { it.uppercaseChar() }
                    } will start after restart ${FabricLoader.getInstance().environmentType.name.lowercase()}$RESET"
                )
            }

            state = State.IDLE
        }
    }

    @Throws(RuntimeException::class)
    private suspend fun downloadFile(url: String, fileName: String, modDirectory: String?): Boolean = try {
        modDirectory ?: DuckyUpdaterReWork.rootModsDir

        File(modDirectory, fileName).writeBytes(client.get(url).body<ByteArray>())
        true
    } catch (e: Exception) {
        DuckyUpdaterReWork.logger.warn("-----[ Permission Denied ]-----")
        DuckyUpdaterReWork.logger.warn("Mod dir: $modDirectory")
        DuckyUpdaterReWork.logger.warn("Mod file: $fileName")
        DuckyUpdaterReWork.logger.warn("Path: ${File(modDirectory, fileName).absolutePath}")
        DuckyUpdaterReWork.logger.warn("-------------------------------")

        throw RuntimeException("Failed to download file: $fileName. ${e.stackTraceToString()}")
    }
}
