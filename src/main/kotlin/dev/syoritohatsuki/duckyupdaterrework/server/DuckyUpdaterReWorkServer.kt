package dev.syoritohatsuki.duckyupdaterrework.server

import com.google.common.collect.ArrayListMultimap
import com.mojang.brigadier.arguments.StringArgumentType
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork.logger
import dev.syoritohatsuki.duckyupdaterrework.core.DuckyUpdaterApi
import dev.syoritohatsuki.duckyupdaterrework.core.command.UpdateCommand
import dev.syoritohatsuki.duckyupdaterrework.core.command.argument.ModsIdsArgumentType
import dev.syoritohatsuki.duckyupdaterrework.core.dao.AdditionalInfo
import dev.syoritohatsuki.duckyupdaterrework.core.dao.Version
import dev.syoritohatsuki.duckyupdaterrework.storage.Database
import dev.syoritohatsuki.duckyupdaterrework.util.*
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import kotlin.jvm.optionals.getOrNull

object DuckyUpdaterReWorkServer : DedicatedServerModInitializer {
    override fun onInitializeServer() {
        logger.info("Loading server-side DURW")

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register {
                rootLiteral("durw-server") {
                    literal("update") {
                        argument("modsIds", ModsIdsArgumentType.modsIds()) {
                            executes(UpdateCommand::update)
                        }
                        argument("modsIdsString", StringArgumentType.string()) {
                            executes(UpdateCommand::update)
                        }
                        literal("all") {
                            executes(UpdateCommand::updateAll)
                        }
                    }
                    literal("list") {
                        executes {
                            val modsIds = ArrayListMultimap.create<String, String>()
                            Database.query("SELECT p1.projectId AS project_id, COALESCE(p2.projectId, '') AS dependency_id  FROM projects AS p1  LEFT JOIN dependencies AS d ON p1.projectId = d.projectId  LEFT JOIN projects AS p2 ON d.dependencyProjectId = p2.projectId") {
                                while (it.next()) modsIds.put(
                                    it.getString("project_id"), it.getString("dependency_id")
                                )
                            }

                            val additionalInfos = mutableMapOf<String, AdditionalInfo>()
                            val projectIds = modsIds.keys().toSet() + modsIds.values().toSet()
                            Database.query(
                                "SELECT projectId, modId, name, changelog, url, version FROM projects WHERE projectId IN(${
                                    projectIds.joinToString(
                                        prefix = "'", postfix = "'", separator = "','"
                                    )
                                }) LIMIT ${projectIds.size}"
                            ) {
                                while (it.next()) additionalInfos[it.getString("projectId")] = AdditionalInfo(
                                    name = it.getString("name") ?: "",
                                    changeLog = it.getString("changelog") ?: "",
                                    url = it.getString("url") ?: "",
                                    version = Version(
                                        currentVersion = FabricLoader.getInstance()
                                            .getModContainer(it.getString("modId"))
                                            .getOrNull()?.metadata?.version?.friendlyString,
                                        newVersion = it.getString("version"),
                                    )
                                )
                            }

                            printModsTree(modsIds, additionalInfos)
                            1
                        }
                    }
                }
            }
        }

        DuckyUpdaterApi.checkForUpdates()

        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted {
            it.commandManager.dispatcher.execute("durw-server list", it.commandSource)
        })
    }
}