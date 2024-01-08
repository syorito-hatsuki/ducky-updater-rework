package dev.syoritohatsuki.duckyupdaterrework.legacy

import com.google.common.hash.Hashing
import com.google.common.io.Files
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import dev.syoritohatsuki.duckyupdaterrework.legacy.dto.LatestVersionsFromHashesBody
import dev.syoritohatsuki.duckyupdaterrework.legacy.dto.UpdateVersions
import dev.syoritohatsuki.duckyupdaterrework.legacy.util.ConfigManager
import dev.syoritohatsuki.duckyupdaterrework.legacy.util.UpdateList
import dev.syoritohatsuki.duckyupdaterrework.legacy.util.updateStatusChatMessage
import dev.syoritohatsuki.duckyupdaterrework.legacy.util.updateStatusCliMessage
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.metadata.ModOrigin
import net.minecraft.server.command.ServerCommandSource
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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.jvm.optionals.getOrNull

object DuckyUpdaterReWork {

    const val MOD_ID = "ducky-updater-rework"

    val logger: Logger = LoggerFactory.getLogger(javaClass.simpleName)
    private val executor: ExecutorService = Executors.newFixedThreadPool(ConfigManager.getThreadCount())

    private val userAgent = "syorito-hatsuki/ducky-updater-rework/${
        FabricLoader.getInstance().getModContainer(MOD_ID).getOrNull()!!.metadata.version.friendlyString
            ?: DateTimeFormatter.ofPattern("yyyy.M").format(LocalDateTime.now())
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

    fun updateByModId(modId: String, source: ServerCommandSource) =
        UpdateList.getUpdates().first { it.modId == modId }.let {
            downloadAsync(it, source)
        }

    fun updateAll(source: ServerCommandSource?) {
        UpdateList.getUpdates().forEach {
            if (!ConfigManager.isIgnoredVersion(it.modId, "${it.versions.matched}${it.versions.newVersion}"))
                downloadAsync(it, source)
        }
    }

    private fun downloadAsync(updateVersions: UpdateVersions, source: ServerCommandSource?) {
        executor.submit {
            var status = 1

            try {
                FileOutputStream(File(updateVersions.modPath.parent.toFile(), updateVersions.remoteFileName))
                    .channel.transferFrom(Channels.newChannel(URL(updateVersions.url).openStream()), 0, Long.MAX_VALUE)
            } catch (e: Exception) {
                File(updateVersions.modPath.parent.toFile(), updateVersions.remoteFileName).delete()
                logger.warn(e.stackTraceToString())
                status = 0
            }

            if (status == 1) {
                if (!updateVersions.modPath.fileName.endsWith(updateVersions.remoteFileName))
                    updateVersions.modPath.toFile().delete()
                UpdateList.markAsUpdated(updateVersions)
            }

            if (source == null || source.player == null) updateStatusCliMessage(updateVersions.modId, status)
            else source.sendFeedback({
                updateStatusChatMessage(updateVersions.modId, status)
            }, false)
        }
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

    private fun requestUpdatesFromApi(): Map<String, JsonElement> = Gson().fromJson(
        HttpClient.newHttpClient().send(
            HttpRequest.newBuilder().POST(
                HttpRequest.BodyPublishers.ofString(
                    GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
                        .toJson(LatestVersionsFromHashesBody(hashes.keys))
                )
            ).header("User-Agent", userAgent).header("Content-Type", "application/json")
                .uri(URI.create("https://api.modrinth.com/v2/version_files/update")).build(),
            HttpResponse.BodyHandlers.ofString()
        ).body(), object : TypeToken<Map<String, JsonElement>>() {}.type
    )
}