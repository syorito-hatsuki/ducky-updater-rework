package dev.syoritohatsuki.duckyupdater

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import dev.syoritohatsuki.duckyupdater.dto.LatestVersionsFromHashesBody
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

    val logger: Logger = LoggerFactory.getLogger(javaClass.simpleName)

    const val MOD_ID = "ducky-updater"
    private lateinit var updateData: MutableMap<String, JsonElement>

    val hashes = HashMap<String, ModContainer>()

    fun checkForUpdate() {
        FabricLoader.getInstance().allMods.forEach { modContainer ->
            getSha512Hash(modContainer)?.let {
                hashes[it] = modContainer
            }
        }

        updateData = JsonParser.parseString(HttpClient.newHttpClient().send(
            HttpRequest.newBuilder()
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                        GsonBuilder()
                            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                            .create()
                            .toJson(LatestVersionsFromHashesBody(hashes.keys))
                    )
                )
                .header("User-Agent", "syorito-hatsuki/ducky-updater/${
                    FabricLoader
                        .getInstance()
                        .getModContainer(MOD_ID)
                        .orElseThrow {
                            RuntimeException("Something went wrong")
                        }
                        .metadata
                        .version
                        .friendlyString
                } (syorito-hatsuki.dev)")
                .header("Content-Type", "application/json")
                .uri(URI.create("https://api.modrinth.com/v2/version_files/update"))
                .build(), HttpResponse.BodyHandlers.ofString()
        ).body()).asJsonObject.asMap()

        logger.info("new data collected")
    }

    fun getUpdates(): MutableMap<String, JsonElement> = updateData
}
