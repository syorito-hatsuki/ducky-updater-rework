package dev.syoritohatsuki.duckyupdater

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import dev.syoritohatsuki.duckyupdater.dto.LatestVersionsFromHashesBody
import dev.syoritohatsuki.duckyupdater.dto.UpdateVersions
import dev.syoritohatsuki.duckyupdater.util.getSha512Hash
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

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
            hashes[hash]?.metadata?.let { meta ->
                val oldVersion = meta.version.friendlyString
                val newVersion = jsonElement.asJsonObject.get("version_number").asString
                logger.info(oldVersion)
                logger.info(newVersion)
                val commonPrefix = oldVersion.commonPrefixWith(newVersion)

                if (newVersion == oldVersion) return@forEach

                _updateVersions.add(
                    UpdateVersions(
                        meta.id,
                        meta.name,
                        jsonElement.asJsonObject.get("files").asJsonArray.get(0).asJsonObject.get("url").asString,
                        jsonElement.asJsonObject.get("changelog").asString,
                        UpdateVersions.Versions(
                            oldVersion.removePrefix(commonPrefix),
                            newVersion.removePrefix(commonPrefix),
                            commonPrefix
                        )
                    )
                )
            }
        }
    }

    private fun hashMods() = FabricLoader.getInstance().allMods.forEach { modContainer ->
        getSha512Hash(modContainer)?.let {
            hashes[it] = modContainer
        }
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
