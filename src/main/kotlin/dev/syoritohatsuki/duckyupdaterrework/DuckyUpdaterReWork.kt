package dev.syoritohatsuki.duckyupdaterrework

import dev.syoritohatsuki.duckyupdaterrework.core.command.argument.DownloadModeArgumentType
import dev.syoritohatsuki.duckyupdaterrework.core.command.argument.FileActionArgumentType
import dev.syoritohatsuki.duckyupdaterrework.core.command.argument.ModsIdsArgumentType
import dev.syoritohatsuki.duckyupdaterrework.core.config.ConfigManager
import dev.syoritohatsuki.duckyupdaterrework.core.storage.Database
import dev.syoritohatsuki.duckyupdaterrework.core.util.FileActions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DuckyUpdaterReWork : ModInitializer {

    const val MOD_ID = "ducky-updater-rework"

    val logger: Logger = LogManager.getLogger()
    val modsIds: List<String> = FabricLoader.getInstance().allMods.map { it.metadata.id }
    val modVersion: String = FabricLoader.getInstance().getModContainer(MOD_ID).get().metadata.version.friendlyString
        ?: DateTimeFormatter.ofPattern("yyyy.M").format(LocalDateTime.now())

    override fun onInitialize() {
        logger.info("Loading common-side DURW")

        ConfigManager
        Database

        CoroutineScope(Dispatchers.IO).launch {
            FileActions.run()
        }

        ArgumentTypeRegistry.registerArgumentType(
            Identifier(MOD_ID, "mods_ids"),
            ModsIdsArgumentType::class.java,
            ConstantArgumentSerializer.of(ModsIdsArgumentType::modsIds)
        )

        ArgumentTypeRegistry.registerArgumentType(
            Identifier(MOD_ID, "download_type"),
            DownloadModeArgumentType::class.java,
            ConstantArgumentSerializer.of(DownloadModeArgumentType::downloadType)
        )

        ArgumentTypeRegistry.registerArgumentType(
            Identifier(MOD_ID, "file_action"),
            FileActionArgumentType::class.java,
            ConstantArgumentSerializer.of(FileActionArgumentType::fileAction)
        )
    }
}