package dev.syoritohatsuki.duckyupdaterrework.storage

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.sql.ResultSet

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

    init {
        kotlin.runCatching {
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
        }
    }

    fun dataStore(): HikariDataSource = dataSource

    fun query(sql: String, resultSet: (ResultSet) -> Unit) {
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

    fun update(sql: String): Int {
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

    private fun projectExist(projectId: String? = "", modId: String? = ""): Boolean {
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
}