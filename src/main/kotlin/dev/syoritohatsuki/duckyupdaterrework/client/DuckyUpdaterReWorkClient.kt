package dev.syoritohatsuki.duckyupdaterrework.client

import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork.logger
import dev.syoritohatsuki.duckyupdaterrework.core.command.commands
import dev.syoritohatsuki.duckyupdaterrework.util.register
import dev.syoritohatsuki.duckyupdaterrework.util.rootLiteral
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

object DuckyUpdaterReWorkClient : ClientModInitializer {
    override fun onInitializeClient() {

        logger.info("Loading client-side DURW")

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register {
                rootLiteral("durw-client") {
                    commands()
                }
            }
        }
    }
}