package dev.syoritohatsuki.duckyupdaterrework.client

import dev.syoritohatsuki.duckyupdaterrework.DuckyUpdaterReWork.logger
import dev.syoritohatsuki.duckyupdaterrework.core.DuckyUpdaterApi
import dev.syoritohatsuki.duckyupdaterrework.core.api.models.Loader
import dev.syoritohatsuki.duckyupdaterrework.core.command.commands
import dev.syoritohatsuki.duckyupdaterrework.core.config.ConfigManager
import dev.syoritohatsuki.duckyupdaterrework.core.dsl.register
import dev.syoritohatsuki.duckyupdaterrework.core.dsl.rootLiteral
import dev.syoritohatsuki.duckyupdaterrework.core.util.TaskManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft
import net.minecraft.world.level.storage.LevelResource

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

        ClientPlayConnectionEvents.JOIN.register(ClientPlayConnectionEvents.Join { _, _, _ ->
            DuckyUpdaterApi.defaultDatapacksDir =
                Minecraft.getInstance().singleplayerServer?.getWorldPath(LevelResource.DATAPACK_DIR)?.toAbsolutePath()
        })

        CoroutineScope(Dispatchers.IO).launch {
            if (ConfigManager.read().checkUpdatesOnBoot) {
                TaskManager.updateProjectsDB(loader = Loader.FABRIC)
            }
        }
    }
}
