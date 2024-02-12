package dev.syoritohatsuki.duckyupdaterrework

import dev.syoritohatsuki.duckyupdaterrework.core.command.UpdateCommand
import dev.syoritohatsuki.duckyupdaterrework.core.command.argument.ModsIdsArgumentType
import dev.syoritohatsuki.duckyupdaterrework.util.argument
import dev.syoritohatsuki.duckyupdaterrework.util.literal
import dev.syoritohatsuki.duckyupdaterrework.util.register
import dev.syoritohatsuki.duckyupdaterrework.util.rootLiteral
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
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

    override fun onInitialize() {
        logger.info("Loading common-side DURW")

        ArgumentTypeRegistry.registerArgumentType(
            Identifier(MOD_ID, "mods_ids"),
            ModsIdsArgumentType::class.java,
            ConstantArgumentSerializer.of(ModsIdsArgumentType::modsIds)
        )

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register {
                rootLiteral("durw") {
                    literal("update") {
                        argument("modsIds", ModsIdsArgumentType.modsIds()) {
                            executes(UpdateCommand::run)
                        }
                    }
                }
            }
        }
    }
}