package dev.syoritohatsuki.duckyupdaterrework.server

import com.mojang.brigadier.arguments.StringArgumentType
import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork.logger
import dev.syoritohatsuki.duckyupdaterrework.core.DuckyUpdaterApi
import dev.syoritohatsuki.duckyupdaterrework.core.command.UpdateCommand
import dev.syoritohatsuki.duckyupdaterrework.core.command.argument.ModsIdsArgumentType
import dev.syoritohatsuki.duckyupdaterrework.util.argument
import dev.syoritohatsuki.duckyupdaterrework.util.literal
import dev.syoritohatsuki.duckyupdaterrework.util.register
import dev.syoritohatsuki.duckyupdaterrework.util.rootLiteral
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback

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
                }
            }
        }

        DuckyUpdaterApi.checkForUpdates()

//        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted {
//            it.commandManager.dispatcher.execute("durw-server update fabric-api yacg", it.commandSource)
//        })
    }
}