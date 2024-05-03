package dev.syoritohatsuki.duckyupdaterrework.core.util

import com.mojang.brigadier.context.CommandContext
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork
import dev.syoritohatsuki.duckyupdaterrework.core.config.ConfigManager
import dev.syoritohatsuki.duckyupdaterrework.core.storage.Database
import dev.syoritohatsuki.duckyupdaterrework.core.storage.Filename
import dev.syoritohatsuki.duckyupdaterrework.core.storage.ProjectId
import dev.syoritohatsuki.duckyupdaterrework.core.storage.Url
import dev.syoritohatsuki.duckyupdaterrework.core.util.Downloader.Mode.*
import dev.syoritohatsuki.duckyupdaterrework.core.util.FileActions.archiveOldMods
import dev.syoritohatsuki.duckyupdaterrework.core.util.FileActions.deleteOldMod
import dev.syoritohatsuki.duckyupdaterrework.core.util.FileActions.disableOldMod
import dev.syoritohatsuki.duckyupdaterrework.server.message.BRIGHT_GREEN
import dev.syoritohatsuki.duckyupdaterrework.server.message.BRIGHT_RED
import dev.syoritohatsuki.duckyupdaterrework.server.message.RESET
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Downloader {
    private val modsDirectory = FabricLoader.getInstance().gameDir.resolve("mods").toFile()

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
            val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            val mode = ConfigManager.read().downloadMode
            val action = ConfigManager.read().fileAction

            context.source.sendMessage(Text.literal("Download starting...").formatted(Formatting.GREEN))
            DuckyUpdaterReWork.logger.info("${BRIGHT_GREEN}Download starting...$RESET")

            when (mode) {
                SEQUENTIALLY -> mapOfUrlAndOldFile.forEach { (projectId, data) ->
                    val (url, oldFile) = data
                    val filename = url.substringAfterLast('/')
                    try {
                        if (downloadFile(url, filename)) {
                            context.source.sendMessageWithLog("Downloaded: $filename")
                            Database.markProjectAsUpdated(projectId)
                            if (filename == oldFile) return@forEach
                            when (action) {
                                FileActions.FileAction.DELETE -> deleteOldMod(oldFile)
                                FileActions.FileAction.DISABLE -> disableOldMod(oldFile)
                                FileActions.FileAction.ARCHIVE -> archiveOldMods(oldFile, date)
                            }
                        } else {
                            context.source.sendMessageWithLog("Phantom error with: $filename in $mode mode and $action action")
                        }
                    } catch (e: Exception) {
                        DuckyUpdaterReWork.logger.error(e)
                        failed.add(Fail(filename, url, e.localizedMessage))
                    }
                }

                PARALLEL -> mapOfUrlAndOldFile.map { (projectId, data) ->
                    CoroutineScope(Dispatchers.IO).async {
                        val (url, oldFile) = data
                        val filename = url.substringAfterLast('/')
                        try {
                            if (downloadFile(url, filename)) {
                                context.source.sendMessageWithLog("Downloaded: $filename")
                                Database.markProjectAsUpdated(projectId)
                                if (filename == oldFile) return@async
                                mutex.withLock {
                                    when (action) {
                                        FileActions.FileAction.DELETE -> deleteOldMod(oldFile)
                                        FileActions.FileAction.DISABLE -> disableOldMod(oldFile)
                                        FileActions.FileAction.ARCHIVE -> archiveOldMods(oldFile, date)
                                    }
                                }
                            } else {
                                context.source.sendMessageWithLog("Phantom error with: $filename in $mode mode and $action action")
                            }
                        } catch (e: RuntimeException) {
                            DuckyUpdaterReWork.logger.error(e)
                            failed.add(Fail(filename, url, e.localizedMessage))
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
                DuckyUpdaterReWork.logger.info("${BRIGHT_GREEN}Download completed$RESET")
            }

            state = State.IDLE
        }
    }

    @Throws(RuntimeException::class)
    private suspend fun downloadFile(url: String, fileName: String): Boolean = try {
        File(modsDirectory, fileName).writeBytes(client.get(url).body<ByteArray>())
        true
    } catch (e: Exception) {
        throw RuntimeException("Failed to download file: $fileName. ${e.message}")
    }
}