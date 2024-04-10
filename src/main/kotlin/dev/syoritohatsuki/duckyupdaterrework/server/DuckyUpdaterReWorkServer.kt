package dev.syoritohatsuki.duckyupdaterrework.server

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
                    literal("check") {
                        executes {
                            val modsIds = Database.modsIds()
                            val additionalInfos = Database.additionalInfoByModsIds(modsIds)
                            buildModsTree(modsIds, additionalInfos).forEach {
                                logger.warn(
                                    "${it.prefix}$GRAY[${
                                        if (it.currentUnMatchVersion.isNotBlank()) "$BRIGHT_GRAY${it.matchedVersion}$BRIGHT_RED${it.currentUnMatchVersion}$GRAY -> "
                                        else ""
                                    }$BRIGHT_GRAY${it.matchedVersion}$BRIGHT_GREEN${it.newUnMatchVersion}$GRAY]$RESET"
                                )
                            }
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