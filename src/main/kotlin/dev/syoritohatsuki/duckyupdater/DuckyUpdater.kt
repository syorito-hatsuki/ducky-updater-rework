package dev.syoritohatsuki.duckyupdater

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import dev.syoritohatsuki.duckyupdater.dto.LatestVersionsFromHashesBody
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object DuckyUpdater {

    val logger: Logger = LoggerFactory.getLogger(javaClass.simpleName)

    const val MOD_ID = "ducky-updater"

    fun getUpdates(hashes: Set<String>): String {
        return HttpClient.newHttpClient().send(
            HttpRequest.newBuilder()
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                        GsonBuilder()
                            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                            .create()
                            .toJson(LatestVersionsFromHashesBody(hashes))
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
        ).body()
    }
}
