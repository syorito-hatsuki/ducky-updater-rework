package dev.syoritohatsuki.duckyupdaterrework.util

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import net.fabricmc.loader.api.FabricLoader
import java.net.URL
import java.nio.file.Files

object Downloader {

    enum class State {
        RUNNING, STOPPED, IDLE
    }

    enum class Status {
        COMPLETE, DOWNLOADING, FAILURE
    }

    enum class Event {
        START, STOP
    }

    private var currentState: State = State.IDLE

    suspend fun testProgressBar() {
        val urls = listOf(
            "https://cdn.modrinth.com/data/3rc31Hgo/versions/Pg5efHEv/spectrum-1.7.4-deeper-down.jar",
            "https://cdn.modrinth.com/data/Gov5Dboq/versions/Qw2Y5gWk/Modern-Industrialization-1.8.0.jar",
            "https://cdn.modrinth.com/data/bAWzYNRd/versions/ncgynsU3/mythicmetals-0.19.2%2B1.20.1.jar",
        )

        coroutineScope {
            val jobs = urls.map { url ->
                async {
                    val fileName = url.substringAfterLast("/")
                    print("\n")
                    downloadFileWithProgress(url, fileName)
                    print("\nDownloaded $fileName")
                    print("\n")
                }
            }

            jobs.awaitAll()
        }
    }

    private suspend fun downloadFileWithProgress(url: String, outputFile: String) {
        val connection = withContext(Dispatchers.IO) {
            URL(url).openConnection()
        }
        val contentLength = connection.contentLengthLong
        if (contentLength == -1L) {
            println("Content length of the file is unknown. Cannot display progress.")
            return
        }

        val channel = Channel<Int>()
        val progressBar = CoroutineScope(Dispatchers.IO).launch {
            val progressIndicator = ProgressBar(contentLength.toInt())
            for (progress in channel) {
                print("\r${outputFile} => [${progressIndicator.getProgressString(progress)}]")
            }
            println()
        }

        val job = CoroutineScope(Dispatchers.IO).launch {
            val inputStream = connection.getInputStream()
            val outputStream =
                Files.newOutputStream(FabricLoader.getInstance().gameDir.resolve("mods").resolve(outputFile))
            val buffer = ByteArray(1024)
            var bytesCount: Long = 0
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                bytesCount += bytesRead
                channel.send(bytesCount.toInt())
            }
            outputStream.close()
            channel.close()
        }

        job.join()
        progressBar.join()
    }

    class ProgressBar(private val total: Int) {
        fun getProgressString(progress: Int): String {
            val currentProgress = (progress.toDouble() / total * 100).toInt()
            return "$currentProgress%"
        }
    }
}