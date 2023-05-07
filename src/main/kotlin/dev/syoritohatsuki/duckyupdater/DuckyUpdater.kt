package dev.syoritohatsuki.duckyupdater

import com.google.common.hash.Hashing
import com.google.common.io.Files
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import dev.syoritohatsuki.duckyupdater.dto.LatestVersionsFromHashesBody
import dev.syoritohatsuki.duckyupdater.dto.UpdateVersions
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.metadata.ModOrigin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.channels.Channels
import java.nio.file.Path
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.jvm.optionals.getOrNull

object DuckyUpdater {

    const val MOD_ID = "ducky-updater"

    val logger: Logger = LoggerFactory.getLogger(javaClass.simpleName)

    private val userAgent = "syorito-hatsuki/ducky-updater/${
        FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow {
            RuntimeException("Something went wrong")
        }.metadata.version.friendlyString
    } (syorito-hatsuki.dev)"

    private var _updateVersions: MutableSet<UpdateVersions> = mutableSetOf()
    val updateVersions: Set<UpdateVersions> = _updateVersions

    private val hashes = HashMap<String, ModContainer>()

    fun checkForUpdate() {
        hashMods()

        requestUpdatesFromApi().forEach { (hash, jsonElement) ->
            hashes[hash]?.let { modContainer ->
                val meta = modContainer.metadata
                val oldVersion = meta.version.friendlyString
                val newVersion = jsonElement.asJsonObject.get("version_number").asString
                val commonPrefix = oldVersion.commonPrefixWith(newVersion)

                if (newVersion == oldVersion) return@forEach

                _updateVersions.add(
                    UpdateVersions(
                        meta.id,
                        meta.name,
                        modContainer.origin.paths[0],
                        jsonElement.asJsonObject.get("files").asJsonArray.get(0).asJsonObject.get("filename").asString,
                        jsonElement.asJsonObject.get("files").asJsonArray.get(0).asJsonObject.get("url").asString,
                        jsonElement.asJsonObject.get("changelog").asString,
                        UpdateVersions.Versions(
                            oldVersion.removePrefix(commonPrefix), newVersion.removePrefix(commonPrefix), commonPrefix
                        )
                    )
                )
            }
        }
    }

    fun updateByModId(modId: String): Int {
        updateVersions.first { it.modId == modId }.let {
            return downloadAsync(modId, it.url, it.modPath, it.remoteFileName)
        }
    }

    fun updateAll(): Map<String, Int> = mutableMapOf<String, Int>().apply {
        updateVersions.forEach {
            put(it.modId, downloadAsync(it.modId, it.url, it.modPath.parent, it.remoteFileName))
        }
    }

    private fun downloadAsync(modId: String, url: String, path: Path, fileName: String): Int {
        logger.warn("Downloading: $modId")
        val executor = Executors.newSingleThreadExecutor()
        val future: Future<Int> = executor.submit(Callable {
            try {
                logger.warn("Trying: $modId")
                FileOutputStream(File(path.parent.toFile(), fileName))
                    .channel.transferFrom(Channels.newChannel(URL(url).openStream()), 0, Long.MAX_VALUE)
            } catch (e: Exception) {
                logger.warn("Catcher: $modId")
                File(path.parent.toFile(), fileName).delete()
                logger.warn(e.stackTraceToString())
                return@Callable 0
            }
            if (!path.fileName.endsWith(fileName)) path.toFile().delete()
//                _updateVersions.removeIf { versions -> versions.modId == modId }
            logger.info("$modId updated successful")
            return@Callable 1
        })
        executor.shutdown()
        logger.warn("Executor end: $modId")
        return future.get()
    }

    private fun hashMods() = FabricLoader.getInstance().allMods.forEach { modContainer ->
        getSha512Hash(modContainer)?.let {
            hashes[it] = modContainer
        }
    }

    private fun getSha512Hash(modContainer: ModContainer): String? {
        if (modContainer.containingMod.isEmpty && modContainer.origin.kind == ModOrigin.Kind.PATH) {
            modContainer.origin.paths.stream().filter {
                it.toString().lowercase().endsWith(".jar")
            }.findFirst().getOrNull()?.let {
                val file = it.toFile()
                if (file.isFile) try {
                    return Files.asByteSource(file).hash(Hashing.sha512()).toString()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    private fun requestUpdatesFromApi(): MutableMap<String, JsonElement> = JsonParser.parseString(
        HttpClient.newHttpClient().send(
            HttpRequest.newBuilder().POST(
                HttpRequest.BodyPublishers.ofString(
                    GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
                        .toJson(LatestVersionsFromHashesBody(hashes.keys))
                )
            ).header("User-Agent", userAgent).header("Content-Type", "application/json")
                .uri(URI.create("https://api.modrinth.com/v2/version_files/update")).build(),
            HttpResponse.BodyHandlers.ofString()
        ).body()
    ).asJsonObject.asMap()
}