package dev.syoritohatsuki.duckyupdaterrework.core.storage

import com.google.common.collect.ArrayListMultimap
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork
import dev.syoritohatsuki.duckyupdaterrework.core.dto.modrinth.AdditionalInfo
import dev.syoritohatsuki.duckyupdaterrework.core.dto.modrinth.Version
import dev.syoritohatsuki.duckyupdaterrework.core.util.toInt
import net.fabricmc.loader.api.FabricLoader
import org.intellij.lang.annotations.Language
import java.sql.ResultSet
import kotlin.io.path.absolutePathString
import kotlin.jvm.optionals.getOrNull
import kotlin.system.exitProcess

typealias ProjectId = String
typealias DependencyId = String
typealias ModId = String
typealias Url = String
typealias FilePath = String
typealias Filename = String

@Suppress("SqlSourceToSinkFlow", "SqlNoDataSourceInspection", "SqlResolve", "LoggingSimilarMessage")
object Database {

    val DB_PATH: String = "${FabricLoader.getInstance().configDir.toAbsolutePath()}/durw/cache.db"

    private val dataSource: HikariDataSource by lazy {
        HikariDataSource(HikariConfig().apply {
            jdbcUrl = "jdbc:sqlite:$DB_PATH"
            maximumPoolSize = 1
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            maxLifetime = 60000
            idleTimeout = 45000
            validate()
        })
    }

    private fun dataStore(): HikariDataSource = dataSource

    fun query(@Language("SQLite") sql: String, resultSet: (ResultSet) -> Unit) {
        runCatching {
            dataStore().connection.use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeQuery(sql).use { resultSet ->
                        resultSet(resultSet)
                    }
                }
            }
        }.onFailure {
            DuckyUpdaterReWork.logger.debug("")
            DuckyUpdaterReWork.logger.debug("Error: ${it.message}")
            DuckyUpdaterReWork.logger.debug("SQL Query Command: $sql")
            DuckyUpdaterReWork.logger.debug("")
        }
    }

    fun update(@Language("SQLite") sql: String): Int {
        runCatching {
            dataStore().connection.use { connection ->
                connection.createStatement().use { statement ->
                    return statement.executeUpdate(sql)
                }
            }
        }.onFailure {
            DuckyUpdaterReWork.logger.debug("")
            DuckyUpdaterReWork.logger.debug("Error: ${it.message}")
            DuckyUpdaterReWork.logger.debug("SQL Update Command: $sql")
            DuckyUpdaterReWork.logger.debug("")
        }
        return -1
    }

    init {
        runCatching {
            dataStore().connection.use { connection ->
                connection.createStatement().use { statement ->
                    statement.execute(
                        """CREATE TABLE IF NOT EXISTS projects (
                                projectId TEXT PRIMARY KEY, 
                                modId TEXT, 
                                name TEXT,
                                changelog TEXT,
                                fileHash TEXT, 
                                version TEXT, 
                                url TEXT,
                                filePath TEXT,
                                filename TEXT,
                                ignore BOOLEAN DEFAULT FALSE,
                                outdated BOOLEAN DEFAULT FALSE
                            )""".trimIndent()
                    )
                    statement.execute(
                        """CREATE TABLE IF NOT EXISTS dependencies (
                                projectId TEXT, 
                                dependencyProjectId TEXT, 
                                PRIMARY KEY (projectId, dependencyProjectId),
                                FOREIGN KEY (projectId) REFERENCES projects(projectId) ON DELETE CASCADE, 
                                FOREIGN KEY (dependencyProjectId) REFERENCES projects(projectId) ON DELETE CASCADE
                            )""".trimMargin()
                    )
                    DuckyUpdaterReWork.logger.info("Database initialized")
                }
            }
        }.onFailure {
            DuckyUpdaterReWork.logger.error("Failed to create database for Ducky Updater: ReWork, mod can't work without it :(")
            DuckyUpdaterReWork.logger.error(it)
            exitProcess(0)
        }
    }

    /*   Project   */
    fun insertOrUpdateProject(
        modId: ModId? = null,
        projectId: ProjectId? = null,
        name: String? = null,
        changelog: String? = null,
        fileHash: String? = null,
        version: String? = null,
        url: Url? = null,
        filePath: FilePath? = null,
        fileName: Filename? = null,
        outdated: Boolean? = null,
    ) {
        val updateValues = mapOf(
            "modId" to modId,
            "projectId" to projectId,
            "name" to name,
            "changelog" to changelog,
            "fileHash" to fileHash,
            "version" to version,
            "url" to url,
            "filePath" to filePath,
            "filename" to fileName,
            "outdated" to (outdated?.toInt()?.toString() ?: "NULL")
        ).filter { !it.value.isNullOrBlank() }

        update(
            when {
                isProjectExist(projectId) -> """UPDATE projects 
                            SET ${updateValues.entries.joinToString(",") { "'${it.key}' = '${it.value}'" }} 
                            WHERE projectId = '$projectId'
                        """.trimIndent()

                else -> """INSERT INTO projects (
                                ${updateValues.keys.joinToString(",")}) 
                            VALUES (
                                ${updateValues.values.joinToString(",") { "'$it'" }}
                        )""".trimIndent()
            }
        )
    }

    fun markProjectAsUpdated(projectId: ProjectId) {
        update("UPDATE projects SET outdated = 0 WHERE projectId = '$projectId'")
    }

    fun setIgnore(modId: String? = null, projectId: String? = null, status: Boolean): Int = when {
        projectId != null -> update("UPDATE projects SET ignore = '${status.toInt()}' WHERE projectId IS '$projectId'")
        modId != null -> update("UPDATE projects SET ignore = '${status.toInt()}' WHERE modId IS '$modId'")
        else -> -1
    }

    private fun isProjectExist(projectId: ProjectId? = null, modId: ModId? = null): Boolean {
        if (projectId == null && modId == null) return false

        var projectExist = false

        query(
            when {
                projectId?.isNotBlank() == true -> "SELECT projectId FROM projects WHERE projectId = '${projectId}' LIMIT 1"
                modId?.isNotBlank() == true -> "SELECT modId FROM projects WHERE modId = '${modId}' LIMIT 1"
                else -> return false
            }
        ) {
            projectExist = it.next()
        }

        return projectExist
    }

    fun getOutdatedProjectIds(): ArrayListMultimap<ProjectId, DependencyId> {
        val modsIds = ArrayListMultimap.create<ProjectId, DependencyId>()
        query(
            """SELECT p1.projectId AS project_id, COALESCE(p2.projectId, '') AS dependency_id 
                FROM projects AS p1 
                LEFT JOIN dependencies AS d ON p1.projectId = d.projectId 
                LEFT JOIN projects AS p2 ON d.dependencyProjectId = p2.projectId
                WHERE p1.ignore = FALSE AND p1.outdated = TRUE
            """.trimMargin()
        ) {
            while (it.next()) modsIds.put(it.getString("project_id"), it.getString("dependency_id"))
        }
        return modsIds
    }

    fun getAdditionalInfoByProjectIds(projectIdsMultimap: ArrayListMultimap<ProjectId, DependencyId>): MutableMap<ProjectId, AdditionalInfo> {
        val additionalInfos = mutableMapOf<ProjectId, AdditionalInfo>()
        val projectIds = projectIdsMultimap.keys().toSet() + projectIdsMultimap.values().toSet()
        query(
            """SELECT projectId, modId, name, changelog, url, version 
                FROM projects 
                WHERE projectId IN(${projectIds.joinToString(prefix = "'", postfix = "'", separator = "','")}) 
                LIMIT ${projectIds.size}
            """.trimIndent()
        ) {
            while (it.next()) additionalInfos[it.getString("projectId")] = AdditionalInfo(
                name = it.getString("name") ?: "",
                changeLog = it.getString("changelog") ?: "",
                url = it.getString("url") ?: "",
                version = Version(
                    currentVersion = FabricLoader.getInstance().getModContainer(it.getString("modId"))
                        .getOrNull()?.metadata?.version?.friendlyString,
                    newVersion = it.getString("version") ?: "",
                )
            )
        }
        return additionalInfos
    }

    fun getAllDownloadingData() = mutableMapOf<ProjectId, Pair<Url, Filename>>().apply {
        query(
            """SELECT projects.projectId, projects.filename, projects.url 
                FROM projects
                WHERE ignore = FALSE
                AND outdated = TRUE
                """.trimIndent()
        ) {
            while (it.next()) put(
                it.getString("projectId"), Pair(
                    it.getString("url") ?: continue, it.getString("filename") ?: ""
                )
            )
        }
    }

    fun getDownloadingDataByModIds(modIds: Set<ModId>) = mutableMapOf<ProjectId, Pair<Url, Filename>>().apply {
        query(
            """SELECT projects.projectId, projects.filename, projects.url 
                FROM projects 
                WHERE modId IN(${modIds.joinToString(prefix = "'", postfix = "'", separator = "','")}) 
                AND ignore = FALSE 
                AND outdated = TRUE
                LIMIT ${modIds.size}
                """.trimIndent()
        ) {
            while (it.next()) put(
                it.getString("projectId"), Pair(
                    it.getString("url") ?: continue, it.getString("filename") ?: ""
                )
            )
        }
    }

    fun getDownloadingDataByProjectIds(projectIds: Set<ProjectId>) =
        mutableMapOf<ProjectId, Pair<Url, Filename>>().apply {
            query(
                """SELECT projects.projectId, projects.filename, projects.url 
                    FROM projects 
                    WHERE projectId IN(${projectIds.joinToString(prefix = "'", postfix = "'", separator = "','")})
                    AND ignore = FALSE 
                    AND outdated = TRUE
                        LIMIT ${projectIds.size}
                """.trimIndent()
            ) {
                while (it.next()) put(
                    it.getString("projectId"), Pair(
                        it.getString("url") ?: continue, it.getString("filename") ?: ""
                    )
                )
            }
        }

    fun getDirectoriesByProjectId(projectIds: Set<ProjectId>) = mutableMapOf<ProjectId, FilePath>().apply {
        query(
            """SELECT projects.projectId, projects.filePath 
                FROM projects
                WHERE projects.projectId IN(${projectIds.joinToString(prefix = "'", postfix = "'", separator = "','")})
                LIMIT ${projectIds.size}
             """.trimIndent()
        ) {
            while (it.next()) put(
                it.getString("projectId"),
                it.getString("filePath") ?: DuckyUpdaterReWork.rootModsDir.absolutePathString()
            )
        }
    }

    fun getListOfIgnoredProjects() = mutableMapOf<String, String>().apply {
        query(
            """SELECT projects.name, projects.modId
                FROM projects 
                WHERE projects.ignore = TRUE
            """.trimIndent()
        ) {
            while (it.next()) put(it.getString("name"), it.getString("modId"))
        }
    }
}
