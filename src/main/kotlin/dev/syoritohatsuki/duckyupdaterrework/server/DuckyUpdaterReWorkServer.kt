package dev.syoritohatsuki.duckyupdaterrework.server

import com.google.common.collect.ArrayListMultimap
import com.mojang.brigadier.arguments.StringArgumentType
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork.logger
import dev.syoritohatsuki.duckyupdaterrework.core.DuckyUpdaterApi
import dev.syoritohatsuki.duckyupdaterrework.core.command.UpdateCommand
import dev.syoritohatsuki.duckyupdaterrework.core.command.argument.ModsIdsArgumentType
import dev.syoritohatsuki.duckyupdaterrework.storage.Database
import dev.syoritohatsuki.duckyupdaterrework.util.*
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents

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
                            Database.query("SELECT p1.name AS project_name, p2.name AS dependency_name FROM projects AS p1 JOIN dependencies AS d ON p1.projectId = d.projectId JOIN projects AS p2 ON d.dependencyProjectId = p2.projectId") {
                                while (it.next()) modsIds.put(
                                    it.getString("project_name"), it.getString("dependency_name")
                                )
                            }
                            printModsTree(modsIds)
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