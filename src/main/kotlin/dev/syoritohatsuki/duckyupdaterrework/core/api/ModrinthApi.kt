package dev.syoritohatsuki.duckyupdaterrework.core.api

import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork.MOD_ID
import dev.syoritohatsuki.duckyupdaterrework.core.api.body.LatestVersionByHash
import dev.syoritohatsuki.duckyupdaterrework.core.api.models.Version
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.jvm.optionals.getOrNull

object ModrinthApi {
    private val modVersion: String =
        FabricLoader.getInstance().getModContainer(MOD_ID).getOrNull()!!.metadata.version.friendlyString
            ?: DateTimeFormatter.ofPattern("yyyy.M").format(LocalDateTime.now())
    private val userAgent = "User-Agent: syorito-hatsuki/$MOD_ID/$modVersion (github.com/syorito-hatsuki)"

    private val httpClient = HttpClient(CIO) {
        install(DefaultRequest) {
            url("https://api.modrinth.com/v2/")
            contentType(ContentType.Application.Json)
        }

        install(UserAgent) {
            agent = userAgent
        }

        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getLatestVersionFromHash(hash: String): Version = httpClient.post("version_file/${hash}/update") {
        parameter("algorithm", "sha512")
        setBody(LatestVersionByHash())
    }.body()

    suspend fun getLatestVersionsFromHashes(hashes: List<String>): Map<String, Version> =
        httpClient.post("version_files/update") {
            setBody(LatestVersionByHash(hashes))
    }.body()
}