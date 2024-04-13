package dev.syoritohatsuki.duckyupdaterrework.storage

import com.google.common.collect.ArrayListMultimap
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork
import dev.syoritohatsuki.duckyupdaterrework.core.dto.AdditionalInfo
import dev.syoritohatsuki.duckyupdaterrework.core.dto.Version
import net.fabricmc.loader.api.FabricLoader
import org.intellij.lang.annotations.Language
import java.io.File
import java.sql.ResultSet
import kotlin.jvm.optionals.getOrNull
import kotlin.system.exitProcess

typealias ModId = String
typealias DependencyId = String

@Suppress("SqlSourceToSinkFlow", "SqlNoDataSourceInspection", "SqlResolve")
object Database {
    private const val SUCCESS = 1

    private val sqlLogsDirectory = File("logs", "sql_logs").apply {
        mkdirs()
    }

    private val sqlQueryLogs = File(sqlLogsDirectory, "query_logs.csv").apply {
        if (exists()) delete()
        createNewFile()
        appendText("SQL Command\tException\n")
    }

    private val sqlUpdateLogs = File(sqlLogsDirectory, "update_logs.csv").apply {
        if (exists()) delete()
        createNewFile()
        appendText("SQL Command\tException\n")
    }

    private val dataSource: HikariDataSource by lazy {
        HikariDataSource(HikariConfig().apply {
            jdbcUrl = "jdbc:sqlite:${FabricLoader.getInstance().configDir.toAbsolutePath()}/durw.db"
            maximumPoolSize = Runtime.getRuntime().availableProcessors() + 1
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
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
            sqlQueryLogs.appendText(
                "${
                    sql.replace("\n", "").replace(Regex("^ +| +$|( )+"), " ")
                }\t${it.localizedMessage}\n"
            )
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
            sqlUpdateLogs.appendText(
                "${
                    sql.replace("\n", "").replace(Regex("^ +| +$|( )+"), " ")
                }\t${it.localizedMessage}\n"
            )
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
        modId: String? = null,
        projectId: String? = null,
        name: String? = null,
        changelog: String? = null,
        fileHash: String? = null,
        version: String? = null,
        url: String? = null,
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
            "outdated" to (outdated?.toString() ?: "NULL")
        ).filter { !it.value.isNullOrBlank() }

        update(StringBuilder().apply {
            if (projectExist(projectId)) {
                append("UPDATE projects SET ")
                append(updateValues.entries.joinToString(",") { "${it.key} = '${it.value}'" })
                append(" WHERE projectId = '$projectId'")
            } else {
                append("INSERT INTO projects (")
                append(updateValues.keys.joinToString(","))
                append(") VALUES (")
                append(updateValues.values.joinToString(",") { "'$it'" })
                append(")")
            }
        }.toString())
    }

    private fun projectExist(projectId: String? = null, modId: String? = null): Boolean {
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

    fun outdateModsIds(): ArrayListMultimap<ModId, DependencyId> {
        val modsIds = ArrayListMultimap.create<ModId, DependencyId>()
        query(
            """SELECT p1.projectId AS project_id, COALESCE(p2.projectId, '') AS dependency_id 
                    FROM projects AS p1 
                    LEFT JOIN dependencies AS d ON p1.projectId = d.projectId 
                    LEFT JOIN projects AS p2 ON d.dependencyProjectId = p2.projectId
                    WHERE p1.ignore = false AND p1.outdated = true
            """.trimMargin()
        ) {
            while (it.next()) modsIds.put(it.getString("project_id"), it.getString("dependency_id"))
        }
        return modsIds
    }

    fun additionalInfoByModsIds(modsIds: ArrayListMultimap<ModId, DependencyId>): MutableMap<ModId, AdditionalInfo> {
        val additionalInfos = mutableMapOf<ModId, AdditionalInfo>()
        val projectIds = modsIds.keys().toSet() + modsIds.values().toSet()
        query(
            """SELECT projectId, modId, name, changelog, url, version 
                        FROM projects 
                        WHERE projectId IN(${projectIds.joinToString(prefix = "'", postfix = "'", separator = "','")}) 
                        LIMIT ${projectIds.size}"""
        ) {
            while (it.next()) additionalInfos[it.getString("projectId")] = AdditionalInfo(
                name = it.getString("name") ?: "",
                changeLog = it.getString("changelog") ?: "",
                url = it.getString("url") ?: "",
                version = Version(
                    currentVersion = FabricLoader.getInstance().getModContainer(it.getString("modId"))
                        .getOrNull()?.metadata?.version?.friendlyString,
                    newVersion = it.getString("version"),
                )
            )
        }
        return additionalInfos
    }
}