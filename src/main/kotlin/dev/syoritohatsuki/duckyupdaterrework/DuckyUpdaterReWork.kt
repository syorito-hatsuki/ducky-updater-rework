package dev.syoritohatsuki.duckyupdaterrework

import dev.syoritohatsuki.duckyupdaterrework.core.command.argument.ModsIdsArgumentType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.nio.file.Paths

object DuckyUpdaterReWork : ModInitializer {

    const val MOD_ID = "ducky-updater-rework"

    val logger: Logger = LogManager.getLogger()
    val configDir: File = Paths.get("", "config", MOD_ID).toFile()
    val modsIds: List<String> = FabricLoader.getInstance().allMods.map { it.metadata.id }

    override fun onInitialize() {
        logger.info("Loading common-side DURW")

        ArgumentTypeRegistry.registerArgumentType(
            Identifier(MOD_ID, "mods_ids"),
            ModsIdsArgumentType::class.java,
            ConstantArgumentSerializer.of(ModsIdsArgumentType::modsIds)
        )
    }
}