package dev.syoritohatsuki.duckyupdaterrework.util

import kotlinx.coroutines.*
import java.io.File
import java.net.URL

object Downloader {
    fun start() {
        CoroutineScope(Dispatchers.IO).launch {
            val urls = listOf(
                "https://cdn.modrinth.com/data/3rc31Hgo/versions/Pg5efHEv/spectrum-1.7.4-deeper-down.jar",
                "https://cdn.modrinth.com/data/Gov5Dboq/versions/Qw2Y5gWk/Modern-Industrialization-1.8.0.jar",
                "https://cdn.modrinth.com/data/bAWzYNRd/versions/ncgynsU3/mythicmetals-0.19.2%2B1.20.1.jar",
            )

            val destinationFolder = "downloads/"

            File(destinationFolder).mkdirs()

            val downloadJobs = urls.map { url ->
                async(Dispatchers.IO) {
                    val fileName = url.substringAfterLast("/")
                    val destination = "$destinationFolder$fileName"
                    downloadFile(url, destination) { bytesRead, totalSize ->
                        val progress = (bytesRead.toDouble() / totalSize * 100).toInt()
                        println("\rDownloading $url: $progress% completed")
                    }
                    println("Downloaded $url to $destination")
                }
            }

            downloadJobs.awaitAll()
        }
    }

    private suspend fun downloadFile(url: String, destination: String, progressCallback: (Long, Long) -> Unit) {
        val connection = withContext(Dispatchers.IO) {
            URL(url).openConnection()
        }
        val totalSize = connection.contentLengthLong
        var bytesRead = 0L

        withContext(Dispatchers.IO) {
            connection.getInputStream()
        }.use { input ->
            File(destination).outputStream().use { output ->
                val buffer = ByteArray(100 * 1024)
                var bytesReadInRound: Int

                while (input.read(buffer).also { bytesReadInRound = it } > 0) {
                    output.write(buffer, 0, bytesReadInRound)
                    bytesRead += bytesReadInRound
                    progressCallback.invoke(bytesRead, totalSize)
                }
            }
        }
    }
}