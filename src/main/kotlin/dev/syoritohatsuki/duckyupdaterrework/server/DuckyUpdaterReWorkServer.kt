package dev.syoritohatsuki.duckyupdaterrework.server

import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork.logger
import dev.syoritohatsuki.duckyupdaterrework.core.DuckyUpdaterApi
import dev.syoritohatsuki.duckyupdaterrework.core.command.commands
import dev.syoritohatsuki.duckyupdaterrework.core.dsl.register
import dev.syoritohatsuki.duckyupdaterrework.core.dsl.rootLiteral
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.WorldSavePath

object DuckyUpdaterReWorkServer : DedicatedServerModInitializer {
    override fun onInitializeServer() {
        logger.info("Loading server-side DURW")

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register {
                rootLiteral("durw-server") {
                    requires { it is ServerCommandSource }.commands()
                }
            }
        }

        ServerLifecycleEvents.SERVER_STARTING.register {
            DuckyUpdaterApi.defaultDatapacksDir = it.getSavePath(WorldSavePath.DATAPACKS)
        }
    }
}
