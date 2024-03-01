package dev.syoritohatsuki.duckyupdaterrework.storage

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork
import net.fabricmc.loader.api.FabricLoader
import java.sql.ResultSet

object Database {
    const val SUCCESS = 1

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
                                modId TEXT, 
                                projectId TEXT PRIMARY KEY, 
                                fileHash TEXT, 
                                version TEXT, 
                                url TEXT,
                                ignore BOOLEAN DEFAULT FALSE
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
        }.onFailure { println(it.localizedMessage) }
    }

    fun update(sql: String): Int {
        runCatching {
            dataStore().connection.use { connection ->
                connection.createStatement().use { statement ->
                    return statement.executeUpdate(sql)
                }
            }
        }.onFailure { println(it.localizedMessage) }
        return -1
    }
}