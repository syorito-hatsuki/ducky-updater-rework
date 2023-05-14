package dev.syoritohatsuki.duckyupdater

import com.google.common.hash.Hashing
import com.google.common.io.Files
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import dev.syoritohatsuki.duckyupdater.dto.LatestVersionsFromHashesBody
import dev.syoritohatsuki.duckyupdater.dto.UpdateVersions
import dev.syoritohatsuki.duckyupdater.util.ConfigManager
import dev.syoritohatsuki.duckyupdater.util.UpdateList
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.jvm.optionals.getOrNull

object DuckyUpdater {

    const val MOD_ID = "ducky-updater"

    val logger: Logger = LoggerFactory.getLogger(javaClass.simpleName)

    private val userAgent = "syorito-hatsuki/ducky-updater/${
        FabricLoader.getInstance().getModContainer(MOD_ID).getOrNull()!!.metadata.version.friendlyString
            ?: DateTimeFormatter.ofPattern("yyyy.MM").format(LocalDateTime.now())
    } (syorito-hatsuki.dev)"


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

                UpdateList.addAvailableUpdates(
                    meta.id, meta.name,
                    modContainer.origin.paths[0],
                    jsonElement.asJsonObject.get("files").asJsonArray.get(0).asJsonObject.get("filename").asString,
                    jsonElement.asJsonObject.get("files").asJsonArray.get(0).asJsonObject.get("url").asString,
                    jsonElement.asJsonObject.get("changelog").asString,
                    UpdateVersions.Versions(
                        oldVersion.removePrefix(commonPrefix), newVersion.removePrefix(commonPrefix), commonPrefix
                    )
                )
            }
        }
    }

    fun updateByModId(modId: String): Int = UpdateList.getUpdates().first { it.modId == modId }.let {
        return downloadAsync(it)
    }

    fun updateAll(): Map<String, Int> = mutableMapOf<String, Int>().apply {
        UpdateList.getUpdates().forEach {
            if (!ConfigManager.isIgnoredVersion(it.modId, "${it.versions.matched}${it.versions.newVersion}"))
                put(it.modId, downloadAsync(it))
        }
    }

    private fun downloadAsync(updateVersions: UpdateVersions): Int {
        val executor = Executors.newSingleThreadExecutor()
        val future: Future<Int> = executor.submit(Callable {
            try {
                FileOutputStream(File(updateVersions.modPath.parent.toFile(), updateVersions.remoteFileName))
                    .channel.transferFrom(Channels.newChannel(URL(updateVersions.url).openStream()), 0, Long.MAX_VALUE)
            } catch (e: Exception) {
                File(updateVersions.modPath.parent.toFile(), updateVersions.remoteFileName).delete()
                logger.warn(e.stackTraceToString())
                return@Callable 0
            }
            if (!updateVersions.modPath.fileName.endsWith(updateVersions.remoteFileName))
                updateVersions.modPath.toFile().delete()
            UpdateList.markAsUpdated(updateVersions)
            return@Callable 1
        })
        executor.shutdown()
        return future.get()
    }

    private fun hashMods() = FabricLoader.getInstance().allMods.forEach { modContainer ->
        modContainer.getSha512Hash()?.let {
            hashes[it] = modContainer
        }
    }

    private fun ModContainer.getSha512Hash(): String? {
        if (containingMod.isEmpty && origin.kind == ModOrigin.Kind.PATH) origin.paths.stream().filter {
            it.toString().lowercase().endsWith(".jar")
        }.findFirst().getOrNull()?.toFile()?.let {
            if (it.isFile) try {
                return Files.asByteSource(it).hash(Hashing.sha512()).toString()
            } catch (e: IOException) {
                e.printStackTrace()
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