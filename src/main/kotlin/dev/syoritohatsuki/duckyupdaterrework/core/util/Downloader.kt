package dev.syoritohatsuki.duckyupdaterrework.core.util

import dev.syoritohatsuki.duckyupdaterrework.core.util.Downloader.Mode.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.fabricmc.loader.api.FabricLoader
import java.io.File

object Downloader {
    private val mutex = Mutex()
    private val client = HttpClient(CIO) { expectSuccess = true }
    private val modsDirectory = FabricLoader.getInstance().gameDir.resolve("mods")
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
    suspend fun download(
        mode: Mode = PARALLEL,
        urls: Set<String>,
        onEndFailSet: (fails: Set<Fail>) -> Unit,
        onLoaded: (filename: String, count: Int) -> Unit
    ) {
        throwIfLocked()

        var count = 0
        val failed = mutableSetOf<Fail>()

        when (mode) {
            SEQUENTIALLY -> urls.forEach {
                val filename = it.substringAfterLast('/')
                try {
                    val file = downloadFile(it, filename)
                    onLoaded.invoke(file, ++count)
                } catch (e: RuntimeException) {
                    failed.add(Fail(filename, it, e.localizedMessage))
                }
            }

            PARALLEL -> urls.map {
                CoroutineScope(Dispatchers.IO).async {
                    val filename = it.substringAfterLast('/')
                    try {
                        val file = downloadFile(it, filename)
                        mutex.withLock {
                            onLoaded.invoke(file, ++count)
                        }
                    } catch (e: RuntimeException) {
                        failed.add(Fail(filename, it, e.localizedMessage))
                    }
                }
            }.awaitAll()
        }

        state = State.IDLE

        onEndFailSet.invoke(failed)
    }

    @Throws(RuntimeException::class)
    private suspend fun downloadFile(url: String, fileName: String): String {
        try {
            File(modsDirectory.toFile(), fileName).writeBytes(client.get(url).body<ByteArray>())
            return fileName
        } catch (e: Exception) {
            throw RuntimeException("Failed to download file: $fileName. ${e.message}")
        }
    }
}