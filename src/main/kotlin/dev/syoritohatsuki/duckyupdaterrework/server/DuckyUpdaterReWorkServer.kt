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
import net.minecraft.command.DefaultPermissions
import net.minecraft.command.permission.PermissionCheck
import net.minecraft.server.command.CommandManager
import net.minecraft.util.WorldSavePath

object DuckyUpdaterReWorkServer : DedicatedServerModInitializer {
    val PERMISSION_CHECK = PermissionCheck.Require(DefaultPermissions.GAMEMASTERS)

    override fun onInitializeServer() {
        logger.info("Loading server-side DURW")

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register {
                rootLiteral("durw-server") {
                    requires(CommandManager.requirePermissionLevel(PERMISSION_CHECK))
                        .commands()
                }
            }
        }

        ServerLifecycleEvents.SERVER_STARTING.register {
            DuckyUpdaterApi.defaultDatapacksDir = it.getSavePath(WorldSavePath.DATAPACKS)
        }

        ServerLifecycleEvents.SERVER_STARTED.register {
            if (ConfigManager.read().checkUpdatesOnBoot) {
                it.commandManager.dispatcher.execute("durw-server check fabric", it.commandSource)
            }
        }
    }
}
