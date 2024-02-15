package dev.syoritohatsuki.duckyupdaterrework.client

import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork.logger
import dev.syoritohatsuki.duckyupdaterrework.core.command.UpdateCommand
import dev.syoritohatsuki.duckyupdaterrework.core.command.argument.ModsIdsArgumentType
import dev.syoritohatsuki.duckyupdaterrework.util.argument
import dev.syoritohatsuki.duckyupdaterrework.util.literal
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
                    literal("update") {
                        argument("modsIds", ModsIdsArgumentType.modsIds()) {
                            executes(UpdateCommand::update)
                        }
                        literal("all") {
                            executes(UpdateCommand::updateAll)
                        }
                    }
                }
            }
        }
    }
}