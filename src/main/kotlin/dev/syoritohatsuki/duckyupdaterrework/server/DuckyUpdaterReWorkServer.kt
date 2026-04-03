package dev.syoritohatsuki.duckyupdaterrework.server

import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork.logger
import dev.syoritohatsuki.duckyupdaterrework.core.DuckyUpdaterApi
import dev.syoritohatsuki.duckyupdaterrework.core.command.commands
import dev.syoritohatsuki.duckyupdaterrework.core.config.ConfigManager
import dev.syoritohatsuki.duckyupdaterrework.core.dsl.register
import dev.syoritohatsuki.duckyupdaterrework.core.dsl.rootLiteral
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.commands.Commands
import net.minecraft.server.permissions.PermissionCheck
import net.minecraft.server.permissions.Permissions
import net.minecraft.world.level.storage.LevelResource

object DuckyUpdaterReWorkServer : DedicatedServerModInitializer {
    val PERMISSION_CHECK = PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER)

    override fun onInitializeServer() {
        logger.info("Loading server-side DURW")

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register {
                rootLiteral("durw-server") {
                    requires(Commands.hasPermission(PERMISSION_CHECK))
                        .commands()
                }
            }
        }

        ServerLifecycleEvents.SERVER_STARTING.register {
            DuckyUpdaterApi.defaultDatapacksDir = it.getWorldPath(LevelResource.DATAPACK_DIR)
        }

        ServerLifecycleEvents.SERVER_STARTED.register {
            if (ConfigManager.read().checkUpdatesOnBoot) {
                it.commands.dispatcher.execute("durw-server check fabric", it.createCommandSourceStack())
            }
        }
    }
}
