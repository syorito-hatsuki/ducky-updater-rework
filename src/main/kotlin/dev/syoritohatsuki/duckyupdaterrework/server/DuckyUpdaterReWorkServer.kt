package dev.syoritohatsuki.duckyupdaterrework.server

import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork.logger
import dev.syoritohatsuki.duckyupdaterrework.core.command.commands
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
                    commands()
                }
            }
        }
    }
}