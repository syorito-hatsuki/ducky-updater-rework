package dev.syoritohatsuki.duckyupdater;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import dev.syoritohatsuki.duckyupdater.dto.LatestVersionsFromHashesBody;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate
import java.util.function.Supplier
import kotlin.jvm.optionals.getOrNull

object DuckyUpdater {

    const val MOD_ID = "ducky-updater";

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
