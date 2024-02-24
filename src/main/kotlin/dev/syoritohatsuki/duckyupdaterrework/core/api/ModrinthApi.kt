package dev.syoritohatsuki.duckyupdaterrework.core.api

import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork
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
import net.minecraft.SharedConstants

object ModrinthApi {
    private val httpClient = HttpClient(CIO) {
        install(DefaultRequest) {
            url("https://api.modrinth.com/v2/")
            contentType(ContentType.Application.Json)
        }

        install(UserAgent) {
            agent = "User-Agent: syorito-hatsuki/$MOD_ID/${DuckyUpdaterReWork.modVersion} (github.com/syorito-hatsuki)"
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
        }.body() ?: emptyMap()

    suspend fun getProjectVersions(projectId: String): List<Version> = httpClient.get("project/${projectId}/version") {
        parameter("game_versions", "[\"${SharedConstants.getGameVersion().name}\"]")
        parameter("loaders", "[\"fabric\"]")
        parameter("featured", false)
    }.body() ?: listOf()

    suspend fun getMultiplyVersions(versions: Set<String>): List<Version> = httpClient.get("versions") {
        parameter("ids", versions.joinToString(prefix = "[\"", postfix = "\"]", separator = "\", \""))
    }.body() ?: listOf()
}